package com.youcii.advanced

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textview.text = MyApplication.aptContent
    }

    private fun showToast() {
        Toast.makeText(this, "invoke from asm", Toast.LENGTH_LONG).show()
    }
}