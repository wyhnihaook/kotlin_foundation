package com.kotlin.practice.base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * 描述:基础数据层
 * 功能介绍:提供数据获取的帮助方法
 * 创建者:翁益亨
 * 创建日期:2023/1/10 17:19
 */
open class BaseRepository {

    /**
     * 方式一：在协程作用域中切换到IO线程
     * */
    protected suspend fun <T> withIO(block:suspend ()->T):T{
        return withContext(Dispatchers.IO){
            block.invoke()
        }
    }

    /**
     * 方式二：构建flow进行链式处理
     * */
    protected suspend fun <T> flowIO(block:suspend ()->T) = flow {
        try {
            emit(block.invoke())
        } catch (e: Exception) {
            emit(null)
        }

    }.flowOn(Dispatchers.IO)


}