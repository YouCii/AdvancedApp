package com.youcii.advanced

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by jingdongwei on 2021.10.20
 *
 * by代理未生效
 */
class MainViewModel(application: Application) : AndroidViewModel(application), DefaultLifecycleObserver by MyLifecycleObserver() {

    private val text by lazy { MutableLiveData<String>() }
    fun observeText(owner: LifecycleOwner, observer: Observer<String>) {
        text.observe(owner, observer)
    }

    /**
     * 协程
     */
    fun coroutineFunction() {
        // 与ViewModel生命周期绑定
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            Log.d("Log", "当前进程:" + Thread.currentThread())
            withContext(Dispatchers.Main) {
                // 必须主线程
                refreshText()
            }
        }
    }

    private fun refreshText() {
        text.value = text.value + "啊"
    }

}