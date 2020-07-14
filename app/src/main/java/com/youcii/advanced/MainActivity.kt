package com.youcii.advanced

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var myRenderer: MyRenderer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //检查设备是否支持OpenGL ES 2.0
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        val supportES2 = configurationInfo.reqGlEsVersion >= 0x00020000

        //配置OpenGL ES，主要是版本设置和设置Renderer，Renderer用于执行OpenGL的绘制
        if (supportES2) {
            glSurfaceView?.setEGLContextClientVersion(2)
            myRenderer = MyRenderer(glSurfaceView)
            glSurfaceView?.setRenderer(myRenderer)
        } else {
            Toast.makeText(this, "不支持OpenGL ES 2.0版本", Toast.LENGTH_SHORT).show()
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myRenderer?.release()
    }

}