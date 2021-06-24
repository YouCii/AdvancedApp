package com.youcii.advanced

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import java.lang.reflect.Field
import java.lang.reflect.Proxy

/**
 * Created by jingdongwei on 2020/07/20.
 */
class MyApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this

        // 集中监听所有Activity创建
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                Log.d("LifecycleCallbacks", "${activity.componentName}.onActivityCreated")
                replaceAllImage(activity)
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        })
    }

    // 集中配置某Activity内所有xml中定义ImageView的默认显示图片
    private fun replaceAllImage(context: Context) {
        try {
            val layoutInflater = LayoutInflater.from(context)
            val mFactory2: Field = LayoutInflater::class.java.getDeclaredField("mFactory2")
            mFactory2.isAccessible = true
            val oldField = mFactory2.get(layoutInflater)
            val hookFactory2 = Proxy.newProxyInstance(
                context.javaClass.classLoader,
                arrayOf<Class<*>>(LayoutInflater.Factory2::class.java)
            ) { _, method, args ->
                val result = method.invoke(oldField, *args)
                configDefault(result)
                return@newProxyInstance result
            }
            mFactory2.set(layoutInflater, hookFactory2)
        } catch (exception: Exception) {
            Toast.makeText(this, "hook失败: $exception", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configDefault(result: Any) {
        if (result is ImageView) {
            result.setBackgroundColor(Color.CYAN)
        } else if (result is OtherImageView) {
            result.setBackgroundColor(Color.RED)
        }
    }


}