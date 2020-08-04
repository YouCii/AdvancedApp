package com.youcii.advanced

import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Created by jingdongwei on 2020/07/14.
 */
class MyRenderer(private val glSurfaceView: GLSurfaceView?) : GLSurfaceView.Renderer {
    private val mProjectMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val transformMatrix = FloatArray(16)

    private var openGLDrawer: OpenGLDrawer? = null

    private var mSurfaceTexture: SurfaceTexture? = null
    private var mCamera: Camera? = null

    // 当Surface创建后会调用此方法
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 清空屏幕的颜色为纯色
        GLES20.glClearColor(1.0f, 0.6f, 0.4f, 0.0f)

        openGLDrawer = OpenGLDrawer()

        initCamera()
        createTexture()
        initSurfaceTexture()
    }

    // 当Surface创建成功或尺寸改变时都调用此方法
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        // 计算宽高比
        val ratio = height.toFloat() / width
        // 设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -1F, 1F, -ratio, ratio, 3F, 7F)
        // 计算变换矩阵
        Matrix.multiplyMM(transformMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    // 每绘制一帧都会调用此方法
    override fun onDrawFrame(gl: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // 获取外部纹理的矩阵，用来确定纹理的采样位置，没有此矩阵可能导致图像翻转/不能展示图像等问题
        mSurfaceTexture?.getTransformMatrix(transformMatrix)
        // 绘制每一帧
        openGLDrawer?.onDrawFrame(mTextureId, transformMatrix)
        // 更新纹理图像
        mSurfaceTexture?.updateTexImage()
    }

    fun release() {
        mCamera?.release()
    }

    private fun initCamera() {
        val mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        mCamera = Camera.open(mCameraId)
        val parameters = mCamera?.parameters
        parameters?.set("orientation", "portrait")
        parameters?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
        parameters?.setPreviewSize(1280, 720)
        mCamera?.setDisplayOrientation(90)
        mCamera?.parameters = parameters
    }

    private var mTextureId: Int = 0

    private fun createTexture() {
        val tex = IntArray(1)
        // 生成一个纹理
        GLES20.glGenTextures(1, tex, 0)
        // // 将此纹理绑定到外部纹理上
        // GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0])
        // // 设置纹理过滤参数
        // GLES20.glTexParameterf(
        //     GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
        //     GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST.toFloat()
        // )
        // GLES20.glTexParameterf(
        //     GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
        //     GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        // )
        // GLES20.glTexParameterf(
        //     GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
        //     GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE.toFloat()
        // )
        // GLES20.glTexParameterf(
        //     GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
        //     GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE.toFloat()
        // )
        // // 解除纹理绑定
        // GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
        mTextureId = tex[0]
    }

    private fun initSurfaceTexture() {
        // 创建一个关联外部纹理ID的SurfaceTexture
        mSurfaceTexture = SurfaceTexture(mTextureId)
        // 获取到一帧数据时请求渲染
        mSurfaceTexture?.setOnFrameAvailableListener {
            glSurfaceView?.requestRender()
        }
        mCamera?.setPreviewTexture(mSurfaceTexture)
        mCamera?.startPreview()
    }
}