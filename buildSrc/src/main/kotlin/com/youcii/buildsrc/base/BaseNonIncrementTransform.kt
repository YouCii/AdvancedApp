package com.youcii.buildsrc.base

import com.android.build.api.transform.JarInput
import com.youcii.buildsrc.IOUtils
import java.io.*
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by jingdongwei on 2021/02/19.
 * 非增量编辑基类Transform
 */
abstract class BaseNonIncrementTransform : BaseTransform() {

    /**
     * 不支持增量编译处理
     */
    override fun isIncremental(): Boolean {
        return false
    }

    /**
     * 两种方式
     * 1. 解压缩, 修改完后再重新压缩
     * 2. 直接通过JarFile进行遍历, 先写入一个新文件中, 再替换原jar
     */
    final override fun handleJarInput(jarInput: JarInput) {
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
     * 对于类的修改, 可以把 new bytes 直接写回原文件
     * 注意: 必须递归到file, 不能处理路径
     */
    final override fun handleDirectoryInput(inputFile: File) {
        if (inputFile.isDirectory) {
            inputFile.listFiles()?.forEach {
                handleDirectoryInput(it)
            }
        } else if (inputFile.absolutePath.contains("com/youcii")) {
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

}