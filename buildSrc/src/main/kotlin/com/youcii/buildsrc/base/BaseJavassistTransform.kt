package com.youcii.buildsrc.base

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.gradle.BaseExtension
import com.android.builder.model.AndroidProject
import javassist.ClassPool
import javassist.CtClass
import javassist.NotFoundException
import org.gradle.api.Project
import java.io.File

/**
 * Created by jingdongwei on 2021/06/16.
 * 基于Javassist进行修改
 */
abstract class BaseJavassistTransform(private val extension: BaseExtension) : BaseTransform() {

    protected val classPool: ClassPool = ClassPool.getDefault().apply {
        // 重点
        appendClassPath(extension.bootClasspath[0].absolutePath)
    }

    override fun forEachJarInput(jarInput: JarInput) {
        super.forEachJarInput(jarInput)
        // 重点
        classPool.appendClassPath(jarInput.file.absolutePath)
    }

    override fun forEachDirectoryInput(directoryInput: DirectoryInput) {
        super.forEachDirectoryInput(directoryInput)
        // 重点
        classPool.appendClassPath(directoryInput.file.absolutePath)
    }

    /**
     * 处理完成后还是通过ByteArray写回transform的输出路径
     */
    override fun handleFileBytes(oldBytes: ByteArray, className: String): ByteArray {
        if (!available(className)) {
            return oldBytes
        }
        val targetClass = try {
            classPool.get(className)
        } catch (e: NotFoundException) {
            println("$name.handleFileBytes NotFoundException: $className")
            return oldBytes
        }
        handleClass(targetClass)
        return targetClass.toBytecode()
    }

    /**
     * 判断是否需要处理
     */
    abstract fun available(className: String): Boolean

    /**
     * 处理class
     */
    abstract fun handleClass(targetClass: CtClass)

}