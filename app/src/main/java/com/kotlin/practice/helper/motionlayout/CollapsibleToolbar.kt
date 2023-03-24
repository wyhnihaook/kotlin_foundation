package com.kotlin.practice.helper.motionlayout

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.appbar.AppBarLayout

/**
 * 描述:滑动过程中自动开启设定动画效果。作为AppBarLayout的子控件时使用，滚动过程中不断设置对应的动画进度
 * 功能介绍:实现竖直方向滑动的过程中执行motionLayout设定的动画效果
 * 创建者:翁益亨
 * 创建日期:2023/2/17 10:23
 */
class CollapsibleToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr), AppBarLayout.OnOffsetChangedListener {

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        progress = -verticalOffset / appBarLayout?.totalScrollRange?.toFloat()!!
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? AppBarLayout)?.addOnOffsetChangedListener(this)
    }
}