package com.kotlin.practice.helper.recyclerview

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * 描述:网格分割处理
 * 功能介绍:网格分割处理
 * 创建者:翁益亨
 * 创建日期:2023/2/10 15:48
 */
class LinearItemDividerDecoration(val drawable: Drawable) :RecyclerView.ItemDecoration() {
    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount

        val rect = Rect()

        rect.left = parent.paddingLeft
        rect.right = parent.width - parent.paddingRight

        for(i in 0 until  childCount){
            val childView = parent.getChildAt(i)
            rect.top = childView.bottom
            rect.bottom = rect.top + drawable.intrinsicHeight

            drawable.setBounds(rect.left,rect.top,rect.right,rect.bottom)

            drawable.draw(c)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom+=drawable.intrinsicHeight
    }
}