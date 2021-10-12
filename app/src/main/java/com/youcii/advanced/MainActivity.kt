package com.youcii.advanced

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testCoroutines()
    }

    private fun testCoroutines() {

    }

    /**
     * suspend 即挂起函数, 仅挂起协程, 不影响线程
     */
    private suspend fun fun1() {
        delay(1000L)
    }

}