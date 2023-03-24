package com.kotlin.practice.helper.recyclerview

import androidx.annotation.Keep
import java.io.Serializable

/**
 * 描述:底部状态管理类
 * 功能介绍:设置初始化数据
 * 创建者:翁益亨
 * 创建日期:2023/2/22 14:55
 */
@Keep
class FooterBean :Serializable{
    /**是否正在加载中*/
    var isLoading = false

    /**没有更多数据*/
    var noMore = false

    /**当前请求出现错误*/
    var isError = false
}