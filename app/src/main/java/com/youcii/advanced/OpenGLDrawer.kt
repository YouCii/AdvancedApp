package com.youcii.advanced

import android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Created by jingdongwei on 2020/07/14.
 */
class OpenGLDrawer {

    companion object {
        /**
         * 顶点着色器
         */
        private const val VERTEX_SHADER = "" +
                //顶点坐标
                "attribute vec4 aPosition;\n" +
                //纹理矩阵
                "uniform mat4 uTextureMatrix;\n" +
                //自己定义的纹理坐标
                "attribute vec4 aTextureCoordinate;\n" +
                //传给片段着色器的纹理坐标
                "varying vec2 vTextureCoord;\n" +
                "void main()\n" +
                "{\n" +
                //根据自己定义的纹理坐标和纹理矩阵求取传给片段着色器的纹理坐标
                "  vTextureCoord = (uTextureMatrix * aTextureCoordinate).xy;\n" +
                "  gl_Position = aPosition;\n" +
                "}\n"

        /**
         * 片段着色器
         */
        private const val FRAGMENT_SHADER = "" +  //使用外部纹理必须支持此扩展
                "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +  //外部纹理采样器
                "uniform samplerExternalOES uTextureSampler;\n" +
                "varying vec2 vTextureCoord;\n" +
                "void main() \n" +
                "{\n" +  //获取此纹理（预览图像）对应坐标的颜色值
                "  vec4 vCameraColor = texture2D(uTextureSampler, vTextureCoord);\n" +  //求此颜色的灰度值
                "  float fGrayColor = (0.3*vCameraColor.r + 0.59*vCameraColor.g + 0.11*vCameraColor.b);\n" +  //将此灰度值作为输出颜色的RGB值，这样就会变成黑白滤镜
                "  gl_FragColor = vec4(fGrayColor, fGrayColor, fGrayColor, 1.0);\n" +
                "}\n"
    }

    /**
     * 顶点和纹理坐标
     * 每行前两个值为顶点坐标，后两个为纹理坐标
     */
    private val vertexData: FloatArray = floatArrayOf(
        1f, 1f, 1f, 1f,
        -1f, 1f, 0f, 1f,
        -1f, -1f, 0f, 0f,
        1f, 1f, 1f, 1f,
        -1f, -1f, 0f, 0f,
        1f, -1f, 1f, 0f
    )

    private val mDataBuffer = createBuffer(vertexData)

    init {
        buildShaderAndLinkProgram()
    }

    fun onDrawFrame(mOESTextureId: Int, transformMatrix: FloatArray) {
        //获取Shader中定义的变量在program中的位置
        val aPositionLocation = GLES20.glGetAttribLocation(mShaderProgram, "aPosition")
        val aTextureCoordinateLocation = GLES20.glGetAttribLocation(mShaderProgram, "aTextureCoordinate")
        val uTextureMatrixLocation = GLES20.glGetUniformLocation(mShaderProgram, "uTextureMatrix")
        val uTextureSamplerLocation = GLES20.glGetUniformLocation(mShaderProgram, "uTextureSampler")

        //激活纹理单元0
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //绑定外部纹理到纹理单元0
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mOESTextureId)
        //将此纹理单元床位片段着色器的uTextureSampler外部纹理采样器
        GLES20.glUniform1i(uTextureSamplerLocation, 0)

        //将纹理矩阵传给片段着色器
        GLES20.glUniformMatrix4fv(uTextureMatrixLocation, 1, false, transformMatrix, 0)

        //将顶点和纹理坐标传给顶点着色器
        if (mDataBuffer != null) {
            //顶点坐标从位置0开始读取
            mDataBuffer.position(0)
            //使能顶点属性
            GLES20.glEnableVertexAttribArray(aPositionLocation)
            //顶点坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
            GLES20.glVertexAttribPointer(
                aPositionLocation,
                2,
                GLES20.GL_FLOAT,
                false,
                16,
                mDataBuffer
            )

            //纹理坐标从位置2开始读取
            mDataBuffer.position(2)
            GLES20.glEnableVertexAttribArray(aTextureCoordinateLocation)
            //纹理坐标每次读取两个顶点值，之后间隔16（每行4个值 * 4个字节）的字节继续读取两个顶点值
            GLES20.glVertexAttribPointer(
                aTextureCoordinateLocation,
                2,
                GLES20.GL_FLOAT,
                false,
                16,
                mDataBuffer
            )
        }

        //绘制两个三角形（6个顶点）
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6)
    }

    /**
     * 将顶点和纹理坐标数据使用FloatBuffer来存储，防止内存回收
     */
    private fun createBuffer(vertexData: FloatArray): FloatBuffer? {
        val buffer: FloatBuffer = ByteBuffer.allocateDirect(vertexData.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        buffer.put(vertexData, 0, vertexData.size).position(0)
        return buffer
    }

    private var mShaderProgram: Int = 0

    /**
     * 编译Shader和链接program
     */
    private fun buildShaderAndLinkProgram() {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        mShaderProgram = linkProgram(vertexShader, fragmentShader)
    }

    //加载着色器，GL_VERTEX_SHADER代表生成顶点着色器，GL_FRAGMENT_SHADER代表生成片段着色器
    private fun loadShader(type: Int, shaderSource: String?): Int {
        //创建Shader
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) {
            throw RuntimeException("Create Shader Failed!" + GLES20.glGetError())
        }
        //加载Shader代码
        GLES20.glShaderSource(shader, shaderSource)
        //编译Shader
        GLES20.glCompileShader(shader)
        return shader
    }

    //将两个Shader链接至program中
    private fun linkProgram(verShader: Int, fragShader: Int): Int {
        //创建program
        val program = GLES20.glCreateProgram()
        if (program == 0) {
            throw RuntimeException("Create Program Failed!" + GLES20.glGetError())
        }
        //附着顶点和片段着色器
        GLES20.glAttachShader(program, verShader)
        GLES20.glAttachShader(program, fragShader)
        //链接program
        GLES20.glLinkProgram(program)
        //告诉OpenGL ES使用此program
        GLES20.glUseProgram(program)
        return program
    }
}