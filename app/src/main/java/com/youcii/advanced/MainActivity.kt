package com.youcii.advanced

import android.content.pm.PackageInfo
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.textview
import java.io.File

class MainActivity : AppCompatActivity() {

    private var text = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appendMsg("getFilesDir: $filesDir")

        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        val sourceDir = packageInfo.applicationInfo.sourceDir
        appendMsg("PackageInfo.sourceDir: $sourceDir")

        appendMsg("getExternalCacheDir: $externalCacheDir")

        appendMsg("getExternalFilesDir(Environment.DIRECTORY_PICTURES): ${getExternalFilesDir(Environment.DIRECTORY_PICTURES)}")
        appendMsg("getExternalFilesDir(File.separator): ${getExternalFilesDir(File.separator)}")
        appendMsg("getExternalFilesDir(null): ${getExternalFilesDir(null)}")

        appendMsg("Environment.getExternalStorageDirectory(): ${Environment.getExternalStorageDirectory()}")
        appendMsg("mkdir是否成功:${File(Environment.getExternalStorageDirectory().path + "/asd").mkdir()}")

        appendMsg(
            "Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES): ${
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )
            }"
        )
        appendMsg("mkdir是否成功:${File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path}/asd").mkdir()}")

        val path = Environment.getExternalStorageDirectory().path + File.separator + packageName
        appendMsg("Environment.getExternalStorageDirectory()/packageName: $path")

        appendMsg("Environment.getRootDirectory().listSize: " + Environment.getRootDirectory().listFiles()?.size)

        // /storage/self/primary 就是SD卡
        appendMsg("File(\"/storage/self/primary\").list: " + File("/storage/self/primary").listFiles()?.size)
        appendMsg("File(\"/storage/self/primary/ASD\").mkdir(): " + File("/storage/self/primary/ASD").mkdir())


        appendMsg("getExternalFilesDir(\"/robust/patch\"): " + getExternalFilesDir("/robust/patch")?.absoluteFile)

        textview.postDelayed({
            val asd = Toast(this)
            asd.view = TextView(this).apply { text = "asdasdasd" }
            asd.show()
        }, 3000)
    }

    private fun appendMsg(msg: String) {
        text += "$msg\n\n"
    }

    override fun onResume() {
        super.onResume()
        textview.text = text
    }

    override fun onDestroy() {
        super.onDestroy()
        val mHandler = Handler(Looper.getMainLooper())
        mHandler.removeCallbacks(null)
    }

}