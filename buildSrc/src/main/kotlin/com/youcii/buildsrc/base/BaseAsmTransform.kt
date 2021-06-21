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
 * Created by jingdongwei on 2021/06/16.
 * 基于ASM的字节码修改
 */
abstract class BaseAsmTransform : BaseTransform() {

    override fun handleFileBytes(oldBytes: ByteArray, className: String): ByteArray {
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