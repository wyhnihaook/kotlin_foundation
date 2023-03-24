package com.kotlin.practice.ui.plan

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.ui.plan.bean.FuturePlansBean
import com.kotlin.practice.ui.plan.repo.FuturePlansRepo

/**
 * 描述:未来计划数据存储类
 * 功能介绍:未来计划数据存储类
 * 创建者:翁益亨
 * 创建日期:2023/2/21 14:27
 */
class FuturePlansViewModel :BaseViewModel() {

    override val title: String
        get() = BaseApp.getContext().getString(R.string.future_plans_page)


    //1.在获取数据的类中，使用 PagingSource 或 RemoteMediator进行获取数据的包装
    private val futurePlansRepo by lazy {
        FuturePlansRepo()
    }

    //2.ViewModel 通过repository中网络/本地数据库请求获取|-Flow<PagingData>-|再同步到 UI上PagingDataAdapter

    //2.1 ViewModel中联系到UI的PagingData对象。PagingData是用于存放分页快照的容器，会查询PagingSource对象并储存结果


    //3.UI的Adapter层需要使用PagingDataAdapter，处理存在分页信息的RecyclerView适配器
    //***可使用随附的AsyncPagingDataDiffer组件构建自定义的适配器***


    //cachedIn() 运算符使数据流可共享，并使用提供的 CoroutineScope 缓存加载的数据
    //cachedIn() 是 Flow<PagingData> 的扩展方法，用于将服务器返回的数据在viewModelScope这个作用域内进行缓存
    //cachedIn() 应该是 ViewModel 中的最后一次调用
    //假如手机横竖屏发生了旋转导致Activity重新创建，Paging 3就可以直接读取缓存中的数据，而不用重新发起网络请求了


    val flow = futurePlansRepo.futureListData.cachedIn(viewModelScope)

    val flow2: LiveData<PagingData<FuturePlansBean>> = futurePlansRepo.futureListData2.cachedIn(viewModelScope).asLiveData()
}