package com.youcii.advanced

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * 代理未生效
 */
class MyLifecycleObserver : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Log.d("Log", "MainViewModel-onCreate")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("Log", "MainViewModel-onStart")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Log.d("Log", "MainViewModel-onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Log.d("Log", "MainViewModel-onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d("Log", "MainViewModel-onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Log.d("Log", "MainViewModel-onDestroy")
    }

}