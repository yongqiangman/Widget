package com.yqman.wdiget;

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout

public class HorizontalDotView : LinearLayout {

    companion object {
        private const val TAG = "HorizontalScrollPage"
    }

    private var mLength : Int = 0
    private val mDotSize : Int
    private val mDotBackground : Int
    private val mLeftMargin : Int

    constructor(context : Context) : this(context, null)

    constructor(context: Context, attributes: AttributeSet?) :
            super(context, attributes) {
        val array = context.obtainStyledAttributes(attributes, R.styleable.HorizontalDotView)
        gravity = Gravity.CENTER
        mDotSize = array.getDimensionPixelSize(R.styleable.HorizontalDotView_yqman_widget_dotSize, 20)
        mDotBackground = array.getResourceId(R.styleable.HorizontalDotView_yqman_widget_dotBackgroundSelector,
                R.drawable.yqman_widget_default_dot_selector)
        mLeftMargin = array.getDimensionPixelSize(R.styleable.HorizontalDotView_yqman_widget_dotMargin, 10);
        array.recycle()
    }

    public fun updateLength(length : Int) {
        if (length == mLength) {
            return
        }
        removeAllViews()
        mLength = if (length > 0) length else 0
        for (index in 0 until mLength) {
            val dot = View(context)
            dot.setBackgroundResource(mDotBackground)
            val params = LinearLayout.LayoutParams(mDotSize, mDotSize) // 圆点大小
            params.leftMargin = mLeftMargin
            dot.layoutParams = params
            dot.isEnabled = false
            addView(dot)
        }
    }

    public fun updatePos(prePos : Int, currentPos : Int, size : Int) {
        updateLength(size)
        getChildAt(prePos)?.isEnabled = false
        getChildAt(currentPos)?.isEnabled = true
    }
}
