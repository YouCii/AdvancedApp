package com.youcii.advanced

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val OVERLAY_PERMISSION_REQ_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        (textview.parent as View).setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23) {
                if (Settings.canDrawOverlays(this)) {
                    //有悬浮窗权限开启服务绑定 绑定权限
                    val intent = Intent(this, MyService::class.java)
                    startService(intent)
                } else {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                        startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                // 默认有悬浮窗权限  但是 华为, 小米,oppo 等手机会有自己的一套 Android6.0 以下
                // 会有自己的一套悬浮窗权限管理 也需要做适配
                val intent = Intent(this, MyService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != OVERLAY_PERMISSION_REQ_CODE) {
            return
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(this)) {
                //有悬浮窗权限开启服务绑定 绑定权限
                val intent = Intent(this, MyService::class.java)
                startService(intent)
            } else {
                Toast.makeText(this, "获取失败", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = Intent(this, MyService::class.java)
        stopService(intent)
    }
}