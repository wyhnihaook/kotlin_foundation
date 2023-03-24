package com.kotlin.practice.ui

import androidx.databinding.ObservableField
import com.kotlin.practice.base.BaseViewModel


/**
 * 描述:首页的业务管理类
 * 功能介绍:数据和业务处理类
 * 创建者:翁益亨
 * 创建日期:2023/1/5 13:46
 */
class MainViewModel : BaseViewModel(){

    //ObservableField只有在数据变化UI才会接收到通知，LiveData在postValue/setValue调用时，UI都会响应
    //LiveData能感知生命周期的变化，在关联的生命周期销毁后，自我清理，避免内存泄漏
    var name:ObservableField<String> = ObservableField("hello")

}