package com.youcii.advanced

import android.app.Application
import android.content.Context

/**
 * Created by jingdongwei on 2020/07/20.
 */
class MyApplication : Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }

}