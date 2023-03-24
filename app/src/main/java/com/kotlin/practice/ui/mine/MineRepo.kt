package com.kotlin.practice.ui.mine

import com.kotlin.practice.base.BaseRepository
import com.kotlin.practice.http.ApiService
import com.kotlin.practice.http.RetrofitManager


/**
 * 描述:Repository 主要用来获取数据并且做完数据的转化
 * 功能介绍:数据处理类
 * 创建者:翁益亨
 * 创建日期:2023/1/10 17:17
 */
class MineRepo : BaseRepository() {

    private var page = 1

    suspend fun getUserInfoSpare() = withIO {
        RetrofitManager.getService(ApiService::class.java)
            .getRankMusic("热歌榜", "json")
            .data<MineUserData>()
    }

    //这里可以优化emit的结果，将内容都封装成成功和失败的结果集合体，外部调用统一处理
    suspend fun getUserInfo() = flowIO{
        RetrofitManager.getService(ApiService::class.java)
            .getUserInfo("热歌榜", "json").data<MineUserData>()
    }

}