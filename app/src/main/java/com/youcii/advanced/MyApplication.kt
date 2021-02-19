package com.youcii.advanced

import android.app.Application
import android.content.Context
import com.youcii.methodrecord.RouterProcessor

/**
 * Created by jingdongwei on 2020/07/20.
 */
class MyApplication : Application() {

    companion object {
        lateinit var context: Context
        var aptContent = "apt失败"
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        initRouter()
    }

    private fun initRouter() {
        try {
            val routeClass = Class.forName(RouterProcessor.ROUTER_CLASS_NAME)
            val routeField = routeClass.getDeclaredField(RouterProcessor.ROUTER_FIELD_NAME)
            aptContent = routeField.get(routeClass)?.toString() ?: "apt失败"
        } catch (ignore: Exception) {
        }
    }

}