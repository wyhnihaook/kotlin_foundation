package com.kotlin.practice.ui.rank

import com.kotlin.practice.base.BaseRepository
import com.kotlin.practice.http.ApiService
import com.kotlin.practice.http.RetrofitManager

/**
 * 描述:Repository 主要用来获取数据并且做完数据的转化
 * 功能介绍:数据处理类
 * 创建者:翁益亨
 * 创建日期:2023/1/10 17:17
 */
class RankRepo : BaseRepository() {

    private var page = 1


    suspend fun getRankMusic() = withIO {
        RetrofitManager.getService(ApiService::class.java)
            .getRankMusic("热歌榜","json")
            .data<MusicBean>()
    }

    suspend fun getMusic() = withIO {
        RetrofitManager.getService(ApiService::class.java)
                //这里直接通过getMusic方法调用，能同步返回结果的原因是由于Retrofit框架代码中识别suspend并对其进行额外操作
                //不使用suspend的情况，需要额外调用api中的异步方法enqueue或者同步方法execute发起网络请求并接收返回值。结合suspend之后，直接调用接口实现类的方法，会自动执行异步方法


                //添加suspend关键字会在方法最后添加一个completion的Continuation（用于恢复因异步请求而挂起的线程），用于完成方法之后的回调
                //！！！！Retrofit的create是识别suspend的起点！！！！
                //RequestFactory类（识别suspend）中isKotlinSuspendFunction字段判断是否是配合协程使用（通过识别最后一个参数是否是Continuation类型即可判断）
                //HttpServiceMethod类（处理suspend）在被ServiceMethod类调用时，会根据当前的接口方法构造不同类型的HttpServiceMethod
                //Retrofit的create最后会调用到invoke方法，调用到HttpServiceMethod子类型的adapt（suspend关键字返回的是SuspendForResponse对象）
                //SuspendForResponse子类的adapt方法中,最终返回的是一个《协程》KotlinExtensions.awaitResponse(call, continuation)
                //返回的suspendCancellableCoroutine协程，awaitResponse方法中又执行了enqueue方法

                //调用suspend的方法，例如：block() 直接执行上述返回的suspendCancellableCoroutine协程体，执行enqueue方法-->挂起等待结果返回恢复执行

            .getMusic("热歌榜","json")
            .data<MusicBean>()
    }
}