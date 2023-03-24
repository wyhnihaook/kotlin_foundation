package com.kotlin.practice.view.horizontal

import android.content.Context
import android.view.MotionEvent
import android.widget.HorizontalScrollView

/**
 * 描述:监听滚动功能
 * 功能介绍:提供滑动监听的能力
 * 创建者:翁益亨
 * 创建日期:2023/2/2 16:49
 */
class AdaptationScrollView(context: Context):HorizontalScrollView(context) {

    //滑动接口监听
    interface OnAdaptationScrollChanged{
        fun onScroll(left:Int,top:Int,oldLeft:Int,oldTop:Int)
    }

    var onAdaptationScrollChanged:OnAdaptationScrollChanged? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        onAdaptationScrollChanged?.apply {
            onScroll(l,t,oldl,oldt)
        }
    }

}