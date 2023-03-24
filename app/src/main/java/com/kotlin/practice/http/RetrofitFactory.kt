package com.kotlin.practice.http

import com.kotlin.practice.util.logError
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 描述:网络请求工厂类
 * 功能介绍:网络请求对象创建
 * 保持唯一性，直接使用单例即可
 * 创建者:翁益亨
 * 创建日期:2023/1/11 15:26
 */
object RetrofitFactory {

    private const val baseUrl = "http://api.uomg.com"

    private const val readTimeout = 5000L

    private const val connectTimeout = 5000L

    //okHttp实例获取
    private val okHttpClient: OkHttpClient
    get() = OkHttpClient.Builder().retryOnConnectionFailure(false)
        .readTimeout(
            readTimeout,
            TimeUnit.MILLISECONDS
        )
        .connectTimeout(
            connectTimeout,
            TimeUnit.MILLISECONDS
        )
        .addInterceptor(getInterceptor())
        .addNetworkInterceptor(getLogInterceptor())
        .build()

    fun createFactory(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
                //获取结果后直接转化成对应需求的实体类
            .addConverterFactory(GsonConverterFactory.create())
                //添加网络请求okhttp实例
            .client(okHttpClient)
            .build()
    }

    //网络请求发起前，对请求进行二次处理
    private fun getInterceptor(): Interceptor{
        val interceptor = Interceptor{
            chain ->
            val original = chain.request()
            var request = original.newBuilder()
//                .addHeader("content-type","application/json")
//                .addHeader("token","")//用户唯一标识符
                .build()

            chain.proceed(request)
        }

        return interceptor
    }

    //打印网络请求结果发起和接收内容
    private fun getLogInterceptor(): HttpLoggingInterceptor{
         val logInterceptor = HttpLoggingInterceptor{
            message->
            logError( message)
        }

        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

        return logInterceptor
    }
}