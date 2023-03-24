package com.kotlin.practice.base

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.kotlin.practice.util.logError

/**
 * 描述:实现生命周期的管理
 * 功能介绍:业务代码实现会经常在对应的生命周期处理
 * 这里依赖于DefaultLifecycleObserver，内部
 * 创建者:翁益亨
 * 创建日期:2023/1/5 14:07
 */
interface IBaseViewModel : DefaultLifecycleObserver {

    //进行耗时操作可能会阻塞释放时，可以通过 if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {}判断当前的状态是否需要继续执行

    override fun onCreate(owner: LifecycleOwner) {
        logError("onCreate")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        logError("onDestroy")
    }
}