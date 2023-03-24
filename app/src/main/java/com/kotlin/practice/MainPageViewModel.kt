package com.kotlin.practice

import androidx.lifecycle.MutableLiveData
import com.kotlin.practice.base.BaseViewModel

/**
 * 描述:主页贡献数据适配器
 * 功能介绍:主页贡献数据适配器
 * 创建者:翁益亨
 * 创建日期:2023/1/10 16:36
 */
class MainPageViewModel:BaseViewModel() {

    var name = MutableLiveData("ssh")
}