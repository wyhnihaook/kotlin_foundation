package com.kotlin.practice.base

import android.app.Activity
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import kotlinx.coroutines.launch

/**
 * 描述:基础ViewModel
 * 功能介绍:管理通用的业务/数据代码
 * 由于调整亮色/暗色（深色）模式会导致页面整体被回收。所以数据类都要放到ViewModel中进行存储
 * 创建者:翁益亨
 * 创建日期:2023/1/5 13:47
 */
open class BaseViewModel : ViewModel(), IBaseViewModel {
    //默认适配的标题内容
    open val title:String = ""

    //避免页面中初始化数据因为界面重新渲染执行生命周期回调而重置的问题
    private var isExecute = false

    /**
     * 同步到页面初始化的initData方法中进行数据初始化
     * @param block 执行数据初始化的逻辑函数（赋值、网络请求等）
     * 通过标识，再页面重复初始化的时候，避免多次重置
     *
     * @return 是否执行初始化的状态
     * */
    fun executeInitData(block:()->Unit):Boolean{
        if(!isExecute){
            block.invoke()
            isExecute = true
            return true
        }

        return false
    }

    /**提供view点击的返回操作，要使用当前的方法*/
    fun back(any: Any){
        if(any is View){
            try {
                any.findNavController().navigateUp()
            }catch (e:Exception){
                //普通关闭
                if(any.context is Activity){
                    (any.context as Activity).finish()
                }
            }
        }
    }

    //提供异步执行的协程体
    protected fun <T> launch(block: suspend () -> T) {
        viewModelScope.launch {
            //输出当前的上下文：Dispatchers.Main.immediate
            //try、catch封装，并且返回数据处于当前上下文下，不是执行请求的IO线程
            runCatching {
                //通过 Result.success将返回结果后的线程切回到main线程
                block()
            }.onFailure {
                //异常情况处理
                it.printStackTrace()
            }
        }
    }

}