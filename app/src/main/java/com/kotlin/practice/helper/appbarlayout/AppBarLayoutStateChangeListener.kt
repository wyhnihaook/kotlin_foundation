package com.kotlin.practice.helper.appbarlayout

import com.google.android.material.appbar.AppBarLayout
import kotlin.math.abs

/**
 * 描述:返回监听的状态
 * 功能介绍:是否折叠信息
 * 创建者:翁益亨
 * 创建日期:2023/2/6 17:31
 */
abstract class AppBarLayoutStateChangeListener : AppBarLayout.OnOffsetChangedListener{

    enum class State{
        EXPANDED,//展开
        COLLAPSED,//折叠
        INTERMEDIATE//中间状态
    }

    private var mCurrentState = State.INTERMEDIATE

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        appBarLayout?.let {

            if(verticalOffset == 0){
                if(mCurrentState!=State.EXPANDED){
                    onStateChanged(appBarLayout,State.EXPANDED)
                }
                onStateOffset(0)

                mCurrentState = State.EXPANDED
            }else if(abs(verticalOffset)>=appBarLayout!!.totalScrollRange){
                if(mCurrentState!=State.COLLAPSED){
                    onStateChanged(appBarLayout,State.COLLAPSED)
                }
                onStateOffset(abs(verticalOffset))

                mCurrentState = State.COLLAPSED
            }else{
                if(mCurrentState!=State.INTERMEDIATE){
                    onStateChanged(appBarLayout,State.INTERMEDIATE)
                }
                onStateOffset(abs(verticalOffset))

                mCurrentState = State.INTERMEDIATE
            }
        }
    }



    /**复写方法*/
    abstract fun onStateChanged(appBarLayout:AppBarLayout?,state:State)
    abstract fun onStateOffset(offset :Int)
}