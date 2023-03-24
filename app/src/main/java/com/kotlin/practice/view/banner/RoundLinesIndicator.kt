package com.kotlin.practice.view.banner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import com.kotlin.practice.R
import com.youth.banner.indicator.BaseIndicator

/**
 * 描述:指导器展示
 * 功能介绍:自定义指导信息
 * 创建者:翁益亨
 * 创建日期:2023/2/3 16:45
 */

class RoundLinesIndicator @JvmOverloads constructor(context:Context,attrs:AttributeSet?,defStyleAttr:Int = 0) :BaseIndicator(context,attrs,defStyleAttr) {

    private val oval : RectF = RectF()
    private val rectF : RectF = RectF()

    private val size = resources.getDimensionPixelOffset(R.dimen.dp_3)
    private val singleWidth = resources.getDimensionPixelOffset(R.dimen.dp_10)
    private val spacing = resources.getDimensionPixelOffset(R.dimen.dp_6)

    private val mPaintNormal = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPaintSelect = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        mPaintNormal.style =  Paint.Style.FILL
        mPaintNormal.color = Color.parseColor("#FFFF0000")
        mPaintSelect.style =  Paint.Style.FILL
        mPaintSelect.color = Color.parseColor("#FF7C75FF")
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val count = config.indicatorSize
        if(count<=1)return

        setMeasuredDimension(size * (count - 1) + spacing * (count - 1) + singleWidth,size)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val count = config.indicatorSize
        if(count<=1)return

        config.apply {
            size
        }

        //遍历角标，并且在对应位置正确绘制正确的形状
        //只考虑左边即可
        for(i in 0 until count){
            var leftPosition = (size * i + spacing * i)*1f

            if(config.currentPosition == i){

                rectF.apply {
                    left = leftPosition
                    top = 0f
                    right = leftPosition + singleWidth
                    bottom = size.toFloat()
                }
                canvas?.drawRoundRect(rectF, config.radius.toFloat(), config.radius.toFloat(), mPaintSelect)
            }else{
                //当前的圆角位置
                var leftPosition =
                if(config.currentPosition>i){
                    //当前左边距+圆的尺寸即可，绝对不是最后一个
                    leftPosition
                }else{
                    leftPosition - size + singleWidth
                }

                oval.apply {
                    left = leftPosition
                    top = 0f
                    right =leftPosition + size.toFloat()
                    bottom = size.toFloat()
                }

                canvas?.drawRoundRect(oval,config.radius.toFloat(),config.radius.toFloat(),mPaintNormal)
            }
        }


    }

}