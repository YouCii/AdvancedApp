package com.youcii.advanced

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton

/**
 * Created by jingdongwei on 2021/08/19.
 */
class TouchButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatButton(context, attrs) {

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> Log.d("TouchEvent", "Button:DOWN")
            MotionEvent.ACTION_MOVE -> Log.d("TouchEvent", "Button:MOVE")
            MotionEvent.ACTION_UP -> Log.d("TouchEvent", "Button:UP")
            else -> {
            }
        }
        return super.onTouchEvent(event)
    }

}