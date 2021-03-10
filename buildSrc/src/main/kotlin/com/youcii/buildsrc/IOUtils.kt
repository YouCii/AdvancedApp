package com.youcii.buildsrc

import org.apache.commons.io.IOUtils
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by jingdongwei on 2021/03/05.
 */
class IOUtils {
    companion object {

        fun readBytes(input: InputStream): ByteArray {
            val buffer = ByteArrayOutputStream(maxOf(DEFAULT_BUFFER_SIZE, input.available()))
            copy(input, buffer)
            return buffer.toByteArray()
        }

        fun copy(input: InputStream, output: OutputStream) {
            IOUtils.copy(input, output)
        }

    }
}