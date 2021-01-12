package com.youcii.advanced

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var bitmapFromCreate: Bitmap? = null
    private var bitmapFromDecode: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnCreate.setOnClickListener(this)
        btnDraw.setOnClickListener(this)
        btnDecode.setOnClickListener(this)
        btnGc.setOnClickListener(this)

        appendMessage("初始内存: " + Runtime.getRuntime().totalMemory())
        appendMessage("最大可用内存: " + Runtime.getRuntime().maxMemory())
        appendMessage("当前可用内存: " + Runtime.getRuntime().freeMemory())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val memoryInfo = Debug.MemoryInfo()
            Debug.getMemoryInfo(memoryInfo)
            appendMessage("memoryInfo(kb): " + JSONObject(memoryInfo.memoryStats as Map<*, *>).toString() + "\n")
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            btnCreate -> {
                val increase = calculateMemoryIncrease(Runnable {
                    bitmapFromCreate = Bitmap.createBitmap(1000, 1000, Bitmap.Config.ARGB_8888)
                })
                appendMessage(btnCreate.text.toString() + "后内存占用增长: " + increase)
            }
            btnDraw -> {
                if (bitmapFromCreate == null) {
                    Toast.makeText(this, "依赖上一步创建bitmap", Toast.LENGTH_SHORT).show()
                } else {
                    val increase = calculateMemoryIncrease(Runnable {
                        Canvas(bitmapFromCreate!!).drawARGB(255, 255, 127, 0)
                    })
                    appendMessage(btnDraw.text.toString() + "后内存占用增长: " + increase)
                }
            }
            btnDecode -> {
                val increase = calculateMemoryIncrease(Runnable {
                    bitmapFromDecode = BitmapFactory.decodeResource(resources, R.mipmap.bitmaptest)
                })
                appendMessage(btnDecode.text.toString() + "后内存占用增长: " + increase)
            }
            btnGc -> {
                val lastMemory = Runtime.getRuntime().freeMemory()
                System.gc()
                btnGc.postDelayed({
                    val increase = lastMemory - Runtime.getRuntime().freeMemory()
                    appendMessage(btnGc.text.toString() + "后释放内存: " + increase)
                }, 5000)
            }
        }
    }

    private fun appendMessage(msg: String) {
        llMessage.addView(TextView(this).apply { text = msg }, 0)
    }

    private fun calculateMemoryIncrease(runnable: Runnable): Long {
        val lastMemory = Runtime.getRuntime().freeMemory()
        runnable.run()
        val currentMemory = Runtime.getRuntime().freeMemory()
        return lastMemory - currentMemory
    }

}