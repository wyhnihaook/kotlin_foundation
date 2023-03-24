package com.kotlin.practice.view.vary

import android.content.Context
import android.view.View

/**
 * 描述:占位替换接口
 * 功能介绍:实现内容
 * 创建者:翁益亨
 * 创建日期:2023/1/4 18:00
 */
interface IVaryViewHelper {

    //获取当前展示在界面上的View信息
    fun  getCurrentLayout():View

    //从占位内容中恢复到原有的内容
    fun restoreView()

    //将原有内容设置为占位内容
    fun showLayout(view:View)

    //根据布局id实现占位组件的初始化
    fun inflate(layoutId:Int):View

    //从同步过来的需要被替换的布局获取对应的上下文内容
    fun getContext(): Context

    //获取当前原布局中需要被替换的初始view
    fun getView():View

}