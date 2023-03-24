package com.kotlin.practice.util

import android.os.Handler
import android.os.Looper

/**
 * 描述:线程切换提供帮助类
 * 功能介绍:切换到主线程处理事务，避免线程的问题导致异常
 * 创建者:翁益亨
 * 创建日期:2023/2/8 10:03
 */

val mainThreadHandler by lazy {
    Handler(Looper.getMainLooper())
}

fun mainThread(block:()->Unit){
    if(Looper.myLooper()!= Looper.getMainLooper()){
        mainThreadHandler.post(block)
    }else{
        block()
    }
}

fun mainThread(delayMillis:Long,block: () -> Unit) = mainThreadHandler.postDelayed(block,delayMillis)