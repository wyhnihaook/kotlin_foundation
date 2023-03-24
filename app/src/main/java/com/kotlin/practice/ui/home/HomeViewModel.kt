package com.kotlin.practice.ui.home

import com.kotlin.practice.base.BaseViewModel

/**
 * 描述:首页数据存储
 * 功能介绍:网络请求，回显数据相关存储
 * 创建者:翁益亨
 * 创建日期:2023/2/1 17:27
 */
class HomeViewModel :BaseViewModel(){

    private val homeRepo by lazy { HomeRepo() }

    //存储数据内容的对象
    val functionBlockList = mutableListOf<HomeFragment.HomeData>()

    var homeRecommendDataList = mutableListOf<HomeRecommendData>()

    fun getRecommendData(block:()->Unit){
        launch {
            val homeRecommendData = homeRepo.getMusic()
            //模拟获取列表数据
            for(i in 1..20){
                homeRecommendDataList.add(homeRecommendData)
            }
            block()
        }
    }
}