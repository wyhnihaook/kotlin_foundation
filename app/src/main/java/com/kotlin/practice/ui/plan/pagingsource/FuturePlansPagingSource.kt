package com.kotlin.practice.ui.plan.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.kotlin.practice.http.ApiService
import com.kotlin.practice.ui.mine.MineUserData
import com.kotlin.practice.ui.plan.bean.FuturePlansBean
import com.kotlin.practice.util.logError
import retrofit2.HttpException
import java.io.IOException

/**
 * 描述:未来计划网络数据获取
 * 功能介绍:未来计划网络数据获取
 * 创建者:翁益亨
 * 创建日期:2023/2/21 14:38
 */
class FuturePlansPagingSource(
    //网络请求的api实例，通过实例发送网络请求，进行数据获取
    val api: ApiService
)  :PagingSource<Int, FuturePlansBean>(){
    /**
     *其中前者是当PagingSource失效后得到用以创建一个新PagingSource的key（这里就是分页的页码）
     *回调情况：1. PagingAdapter 调用了 refresh()  2. PagingSource 调用了 invalidate()
     * 该方法接受 PagingState 对象作为参数，并且当数据在初始加载后刷新或失效时，
     * 该方法会返回要传递给 load() 方法的键。在后续刷新数据时，Paging 库会自动调用此方法
     *
     * 实现必须定义如何从已加载分页数据的中间恢复刷新,使用 state.anchorPosition 作为最近访问的索引来映射正确的初始键。
     * */
    override fun getRefreshKey(state: PagingState<Int, FuturePlansBean>): Int? {
        //可以返回null
        //以下为官方标准写法，返回当前下一页的key
        logError(
            "getRefreshKey Page:加载数据 ：${
                state.anchorPosition?.let { anchorPosition ->
                    val anchorPage = state.closestPageToPosition(anchorPosition)
                    anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
                }
            }"
        )
        //refresh将从当前页码进行请求
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage = state.closestPageToPosition(anchorPosition)
//            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
//        }
        //每次refresh都从第一页加载
        return null
    }

    /**
     * @param params LoadParams 对象包含有关要执行的加载操作的信息，其中包括要加载的键和要加载的项数。
     * @return LoadResult 对象包含加载操作的结果。
     * LoadResult 是一个密封的类，根据 load() 调用是否成功，采用如下两种形式之一：
     *如果加载成功，则返回 LoadResult.Page 对象。
     *如果加载失败，则返回 LoadResult.Error 对象。
     * */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FuturePlansBean> {
        try {

            //正常进行数据异步请求获取当前列表数据，开始请求第一页的数据（如果LoadResult.Page中存在nextKey的赋值的时候，那么当前的number就是上一次网络请求返回的页码）
            val nextPageNumber = params.key ?: 1
            logError("load nextPageNumber:$nextPageNumber")

            //模拟构建列表返回的数据列表
            val response = api.getFuturePlan("热歌榜", "json").data<FuturePlansBean>()

            val listData = mutableListOf<FuturePlansBean>()
            val responseName = response.name
            for (i in 0..20) {
                val futurePlansBean = FuturePlansBean().apply {
                    name = "$i:$responseName"
                    id = i
                    artistsname = response.artistsname
                    picurl = response.picurl
                    url = response.url
                }
                listData.add(futurePlansBean)
            }

            //完成之后添加底部占位操作
//            if(nextPageNumber==10)listData.add(null)

            return LoadResult.Page(
                data = listData,
                prevKey = null, // Only paging forward.
                //目前直接通过当前长度自增即可;如果设置为null的话说明没有数据了。模拟所有数据都已经加载完毕，在第十页的时候进行展示没有更多数据
                nextKey = if(nextPageNumber==10)null else nextPageNumber + 1//请求当前列表成功，下一次要请求的页码。response.nextPageNumber。这里的分页的页码最好和后端协商返回下一页的关联，这样就可以保证当前处理的页码只处理一次
            )
        } catch (e: Exception) {
            //捕获网络请求过程中的异常
            when (e) {
                is IOException -> {
                    // IOException for network failures.
                }
                is HttpException -> {
                    // HttpException for any non-2xx HTTP status codes.
                }
            }
            logError("network ERROR：$e")
            return LoadResult.Error(e)
        }
    }
}