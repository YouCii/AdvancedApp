package com.youcii.advanced

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatTextView

/**
 * Created by jingdongwei on 2021/08/19.
 */
class TouchTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : AppCompatTextView(context, attrs) {

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> Log.d("TouchEvent", "TextView:DOWN")
            MotionEvent.ACTION_MOVE -> Log.d("TouchEvent", "TextView:MOVE")
            MotionEvent.ACTION_UP -> Log.d("TouchEvent", "TextView:UP")
            else -> {
            }
        }
        return super.onTouchEvent(event)
    }

}