package com.youcii.buildsrc.base

import org.objectweb.asm.*
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

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
                    handleJarInput(jarInput)
                    val dest = outputProvider.getContentLocation(jarInput.file.absolutePath, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    FileUtils.copyFile(jarInput.file, dest)
                }
            }
            // 多线程处理源码
            input.directoryInputs.forEach { directoryInput ->
                waitableExecutor.execute {
                    handleDirectoryInput(directoryInput.file)
                    val dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                    FileUtils.copyDirectory(directoryInput.file, dest)
                }
            }
            // 以上写法仅适用于各Task互不依赖的场景
        }
        // 保证所有任务全部执行完毕再执行后续transform, 传参true表示: 如果其中一个Task抛出异常时终止其他task
        waitableExecutor.waitForTasksWithQuickFail<Any>(true)
    }

    abstract fun handleJarInput(jarInput: JarInput)
    abstract fun handleDirectoryInput(inputFile: File)

    fun handleFileBytes(oldBytes: ByteArray): ByteArray {
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