package com.youcii.advanced

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.appbar.AppBarLayout

class MyMotion @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr), AppBarLayout.OnOffsetChangedListener {

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        val total = appBarLayout?.totalScrollRange?.toFloat()!!

        // if (verticalOffset >= -8.dp) {
        // } else {
        //     progress = (-verticalOffset - 8.dp) / total
        //     Log.e("MyMotion", "progress: " + progress)
        // }

        progress = -verticalOffset / total
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? AppBarLayout)?.addOnOffsetChangedListener(this)
    }

}