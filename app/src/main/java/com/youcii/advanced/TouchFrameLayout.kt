package com.youcii.advanced

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout

/**
 * Created by jingdongwei on 2021/08/19.
 */
class TouchFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> Log.d("TouchEvent", "FrameLayout:DOWN")
            MotionEvent.ACTION_MOVE -> Log.d("TouchEvent", "FrameLayout:MOVE")
            MotionEvent.ACTION_UP -> Log.d("TouchEvent", "FrameLayout:UP")
            else -> {
            }
        }
        return super.onTouchEvent(event)
    }

}