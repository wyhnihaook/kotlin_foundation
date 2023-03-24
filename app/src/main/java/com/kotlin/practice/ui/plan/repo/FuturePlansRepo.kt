package com.kotlin.practice.ui.plan.repo

import androidx.paging.Pager
import androidx.paging.filter
import androidx.paging.map
import com.kotlin.practice.base.BaseRepository
import com.kotlin.practice.http.ApiService
import com.kotlin.practice.http.PagingFactory
import com.kotlin.practice.http.RetrofitManager
import com.kotlin.practice.ui.plan.pagingsource.FuturePlansPagingSource
import kotlinx.coroutines.flow.map

/**
 * 描述:数据提供类
 * 功能介绍:提供需要获取的数据
 * 创建者:翁益亨
 * 创建日期:2023/2/21 14:45
 */
class FuturePlansRepo:BaseRepository() {


    //*PagingSource：每个PagingSource对象都定义了数据源，以及如何从该数据源中检索数据。（网络/本地）
    //*RemoteMediator：处理来自分层的数据源（本地数据库缓存的网络数据）的分页

    //这里可以优化emit的结果，将内容都封装成成功和失败的结果集合体，外部调用统一处理

    //1.首先要定义api返回的数据结构，使用PagingSource<key:Int,value:T>对象处理。其中key是页码，value就是获取的每一个item对象
    //1.1要自定义PagingSource继承类，复写对应方法，load方法中通过api获取结果之后，构建成Page数据返回（中间层转化）

    //请求方式一：使用对象持久化挂起获取数据结果

    val futureListData = Pager(PagingFactory.pagingConfig){
        FuturePlansPagingSource(RetrofitManager.getService(ApiService::class.java))
    }.flow

    //方式二：使用对象监听，变化后，刷新到适配器中
    val futureListData2 = Pager(PagingFactory.pagingConfig) {
        FuturePlansPagingSource(RetrofitManager.getService(ApiService::class.java))
    }.flow.map {
        //数据中间层处理（数据处理返回）
            pageData ->

        pageData
            .filter {
                //过滤符合要求的数据.这里id不为0的数据都提取出来
                it.id != 0
            }
            .map { mineUserData ->
                mineUserData.name =  "中间层数据转化-${mineUserData.name}"
                //                这里一定要返回页面构建所需的对象
                mineUserData
            }
    }


}