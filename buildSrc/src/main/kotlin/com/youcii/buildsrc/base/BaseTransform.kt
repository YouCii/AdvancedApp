package com.youcii.buildsrc.base

import org.objectweb.asm.*
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.youcii.buildsrc.IOUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by jingdongwei on 2021/02/20.
 */
abstract class BaseTransform : Transform() {

    /**
     * 多线程并发处理线程池
     */
    private val waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()

    /**
     * 当前Transform在列表中存储的名称
     */
    override fun getName(): String {
        return javaClass.name
    }

    /**
     * 过滤维度一: 输入类型
     * CLASSES--代码
     * RESOURCES--既不是代码也不是android项目中的res资源，而是asset目录下的资源
     *
     * 其实上面两个只是暴露给我们的, 另外还有仅AndroidPlugin可用的类型:
     * DEX, NATIVE_LIBS, CLASSES_ENHANCED, DATA_BINDING, DEX_ARCHIVE, DATA_BINDING_BASE_CLASS_LOG
     */
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 过滤维度二: 要处理的.class文件的范围. 如果仅仅只读的话需要在此方法中返回空, 使用getReferencedScopes指定读取的对象
     *
     * 标准作用域:
     * <code>
     *     enum Scope implements ScopeType {
     *         /** 仅最外层主工程 */
     *         PROJECT(0x01),
     *         /** 主工程下的各个module */
     *         SUB_PROJECTS(0x04),
     *         /** lib中引用的jar, implement引入的三方库 */
     *         EXTERNAL_LIBRARIES(0x10),
     *         /** Code that is being tested by the current variant, including dependencies */
     *         TESTED_CODE(0x20),
     *         /** Local or remote dependencies that are provided-only */
     *         PROVIDED_ONLY(0x40),
     *     }
     * </code>
     *
     * 额外作用域:
     * <code>
     *     public enum InternalScope implements QualifiedContent.ScopeType {
     *         /** Scope to package classes.dex files in the main split APK in InstantRun mode. All other classes.dex will be packaged in other split APKs. */
     *         MAIN_SPLIT(0x10000),
     *         /** Only the project's local dependencies (local jars). This is to be used by the library plugin, only (and only when building the AAR). */
     *         LOCAL_DEPS(0x20000),
     *         /** 包括dynamic-feature modules */
     *         FEATURES(0x40000),
     *     }
     * </code>
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    /**
     * 支持增量编译处理, 需要判断Input.status
     * 参考: https://juejin.cn/post/6844904150925312008
     */
    override fun isIncremental(): Boolean {
        return true
    }

    /**
     * 1. 如果消费了getInputs()的输入，则transform后必须再输出给下一级
     * 2. 如果不希望做任何修改, 应该使用getReferencedScopes指定读取的对象, 并在getScopes中返回空。
     * 3. 是否增量编译要以transformInvocation.isIncremental()为准, 如果isIncremental==false则Input#getStatus()极可能不准确
     */
    @Throws(TransformException::class, InterruptedException::class, IOException::class)
    final override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        // 非增量编译必须先清除之前所有的输出, 否则 transformDexArchiveWithDexMergerForDebug
        if (!transformInvocation.isIncremental) {
            transformInvocation.outputProvider.deleteAll()
        }

        val outputProvider = transformInvocation.outputProvider
        transformInvocation.inputs.forEach { input ->
            // 多线程处理Jar
            input.jarInputs.forEach { jarInput ->
                waitableExecutor.execute {
                    val dest = outputProvider.getContentLocation(jarInput.file.absolutePath, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    if (transformInvocation.isIncremental) {
                        handleIncrementalJarInput(jarInput, dest)
                    } else {
                        handleNonIncrementalJarInput(jarInput)
                        FileUtils.copyFile(jarInput.file, dest)
                    }
                }
            }
            // 多线程处理源码
            input.directoryInputs.forEach { directoryInput ->
                waitableExecutor.execute {
                    val dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                    if (transformInvocation.isIncremental) {
                        handleIncrementalDirectoryInput(directoryInput, dest)
                    } else {
                        handleNonIncrementalDirectoryInput(directoryInput.file)
                        FileUtils.copyDirectory(directoryInput.file, dest)
                    }
                }
            }
            // 以上写法仅适用于各Task互不依赖的场景
        }
        // 保证所有任务全部执行完毕再执行后续transform, 传参true表示: 如果其中一个Task抛出异常时终止其他task
        waitableExecutor.waitForTasksWithQuickFail<Any>(true)
    }

    /**
     * 增量处理JarInput
     */
    private fun handleIncrementalJarInput(jarInput: JarInput, dest: File) {
        when (jarInput.status) {
            Status.NOTCHANGED -> {
            }
            Status.ADDED -> {
                handleNonIncrementalJarInput(jarInput)
            }
            Status.CHANGED -> {
                // 如果状态是改变, 说明有历史缓存, 应该先删掉, 再写入我们本次生成的
                if (dest.exists()) {
                    FileUtils.forceDelete(dest)
                }
                handleNonIncrementalJarInput(jarInput)
            }
            Status.REMOVED -> {
                if (dest.exists()) {
                    FileUtils.forceDelete(dest)
                }
            }
            else -> {
            }
        }
    }

    /**
     * 非增量处理JarInput
     * 两种方式
     * 1. 解压缩, 修改完后再重新压缩
     * 2. 直接通过JarFile进行遍历, 先写入一个新文件中, 再替换原jar
     */
    private fun handleNonIncrementalJarInput(jarInput: JarInput) {
        val oldPath = jarInput.file.absolutePath
        val oldJarFile = JarFile(jarInput.file)

        val newPath = oldPath.substring(0, oldPath.lastIndexOf(".")) + ".bak"
        val newFile = File(newPath)
        val newJarOutputStream = JarOutputStream(FileOutputStream(newFile))

        oldJarFile.entries().iterator().forEach {
            newJarOutputStream.putNextEntry(ZipEntry(it.name))
            val inputStream = oldJarFile.getInputStream(it)
            // 修改逻辑
            if (it.name.startsWith("com")) {
                val oldBytes = IOUtils.readBytes(inputStream)
                newJarOutputStream.write(handleFileBytes(oldBytes))
            }
            // 不做改动, 原版复制
            else {
                IOUtils.copy(inputStream, newJarOutputStream)
            }
            newJarOutputStream.closeEntry()
            inputStream.close()
        }

        newJarOutputStream.close()
        oldJarFile.close()

        jarInput.file.delete()
        newFile.renameTo(jarInput.file)
    }

    /**
     * 增量处理类修改
     */
    private fun handleIncrementalDirectoryInput(directoryInput: DirectoryInput, dest: File) {
        val srcDirPath = directoryInput.file.absolutePath
        val destDirPath = dest.absolutePath
        directoryInput.changedFiles.forEach { (inputFile, status) ->
            val destFilePath = inputFile.absolutePath.replace(srcDirPath, destDirPath)
            val destFile = File(destFilePath)
            when (status) {
                Status.NOTCHANGED -> {
                }
                Status.ADDED -> {
                    handleNonIncrementalDirectoryInput(inputFile)
                    FileUtils.copyFile(inputFile, destFile)
                }
                Status.CHANGED -> {
                    // 如果状态是改变, 说明有历史缓存, 应该先删掉, 再写入我们本次生成的
                    if (dest.exists()) {
                        FileUtils.forceDelete(dest)
                    }
                    handleNonIncrementalDirectoryInput(inputFile)
                    FileUtils.copyFile(inputFile, destFile)
                }
                Status.REMOVED -> {
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                }
                else -> {
                }
            }
        }
    }

    /**
     * 非增量处理类修改, 可以把 new bytes 直接写回原文件
     * 注意: 必须递归到file, 不能处理路径
     */
    private fun handleNonIncrementalDirectoryInput(inputFile: File) {
        if (inputFile.isDirectory) {
            inputFile.listFiles()?.forEach {
                handleNonIncrementalDirectoryInput(it)
            }
        } else {
            handleSingleFile(inputFile)
        }
    }

    /**
     * 处理单个路径下的单个文件
     */
    private fun handleSingleFile(inputFile: File) {
        if (inputFile.absolutePath.contains("com/youcii")) {
            val inputStream = FileInputStream(inputFile)
            val oldBytes = IOUtils.readBytes(inputStream)
            inputStream.close()

            val newBytes = handleFileBytes(oldBytes)
            // 注意!! 实例化FileOutputStream时会清除掉原文件内容!!!!
            val outputStream = FileOutputStream(inputFile)
            outputStream.write(newBytes)
            outputStream.close()
        }
    }

    private fun handleFileBytes(oldBytes: ByteArray): ByteArray {
        return try {
            val classReader = ClassReader(oldBytes)
            val classWriter = ClassWriter(ClassWriter.COMPUTE_MAXS)
            val classVisitor = getClassVisitor(classWriter)
            classReader.accept(classVisitor, Opcodes.ASM5)
            classWriter.toByteArray()
        } catch (e: ArrayIndexOutOfBoundsException) {
            oldBytes
        } catch (e: IllegalArgumentException) {
            oldBytes
        }
    }

    abstract fun getClassVisitor(classWriter: ClassWriter): ClassVisitor
}