package com.kotlin.practice.http


/**
 * 描述:网络请求管理类
 * 功能介绍:管理所有的服务信息
 * 创建者:翁益亨
 * 创建日期:2023/1/10 18:03
 */
class RetrofitManager {

    companion object{
        /**
         * 维护多个ApiService
         * */
        private val map = mutableMapOf<Class<*>,Any>()

        /**
         * 静态方法，调用时生成对应的唯一实例
         * */
        private val retrofit = RetrofitFactory.createFactory()


        /**
         * 根据类，获取存储的实例
         * */
        fun <T:Any> getService(apiClass:Class<T>):T{
            return if(map[apiClass] == null){
                synchronized(RetrofitManager::class.java){
                    val t = retrofit.create(apiClass)
                    if(map[apiClass] == null){
                        map[apiClass] = t
                    }
                    t
                }
            }else{
                map[apiClass] as T
            }
        }
    }

}