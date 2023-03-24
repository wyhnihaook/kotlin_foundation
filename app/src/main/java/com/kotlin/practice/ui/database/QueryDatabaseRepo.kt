package com.kotlin.practice.ui.database

import com.kotlin.practice.base.BaseRepository
import com.kotlin.practice.db.AppDatabase
import com.kotlin.practice.db.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * 描述:数据库页面展示的数据
 * 功能介绍:数据库页面展示的数据
 * 创建者:翁益亨
 * 创建日期:2023/2/15 17:07
 */
class QueryDatabaseRepo:BaseRepository() {

    val db by lazy {
        AppDatabase.getInstance()
    }

    //获取数据库中添加的用户信息
    // 通过 flowOn 将上游（所有或者另一个flowOn之间）执行的切换到 对应 线程（这里切换到IO）
    // 下游（后续执行链上）的所有模块运行在整个flow原本运行的线程上，也就是包裹运行这个方法的线程上
    fun getUserList(): Flow<List<User>> = db.userDao().getAll().flowOn(Dispatchers.IO)
}