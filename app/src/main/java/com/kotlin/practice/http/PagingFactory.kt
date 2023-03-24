package com.kotlin.practice.http

import androidx.paging.PagingConfig

/**
 * 描述:分页加载静态配置
 * 功能介绍:分页加载静态配置
 * 创建者:翁益亨
 * 创建日期:2023/2/17 14:48
 */
object PagingFactory {

    val pagingConfig = PagingConfig(
        // 每页显示的数据的大小
        pageSize = 20,

        // 开启占位符
        enablePlaceholders = true,

        // 预刷新的距离，距离最后一个 item 多远时加载数据
        // 默认为 pageSize
        prefetchDistance = 1,

        /**
         * 初始化加载数量，默认为 pageSize * 3
         *
         * internal const val DEFAULT_INITIAL_PAGE_MULTIPLIER = 3
         * val initialLoadSize: Int = pageSize * DEFAULT_INITIAL_PAGE_MULTIPLIER
         */
        initialLoadSize = 20
    )
}