package com.youcii.advanced

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.textview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("Log", "MainActivity-beforeCoroutine")
        coroutineFunction()
        Log.d("Log", "MainActivity-afterCoroutine")

        viewModel.observeText(this, Observer {
            textview.text = it
        })

        textview.setOnClickListener {
            viewModel.coroutineFunction()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("Log", "MainActivity-onResume")
    }

    /**
     * 协程
     */
    private fun coroutineFunction() { // 与LifecycleOwner(Activity)生命周期绑定
        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            Log.d("Log", "当前进程:" + Thread.currentThread())
            lifecycle.addObserver(viewModel)
        }
    }

}