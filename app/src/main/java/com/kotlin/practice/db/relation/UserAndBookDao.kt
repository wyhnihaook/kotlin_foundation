package com.kotlin.practice.db.relation

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import com.kotlin.practice.db.user.User
import kotlinx.coroutines.flow.Flow

/**
 * 描述:根据当前的用户查询书本的SQL
 * 功能介绍:过滤出所有匹配的内容
 * 创建者:翁益亨
 * 创建日期:2023/1/6 15:55
 */
@Dao
interface UserAndBookDao {

    //需要多次执行查询保证其原子性
    //查询所有用户以及关联的书本信息
    @Transaction
    @Query("SELECT * FROM USER")
    fun getUsersWithBookList():Flow<List<UserAndBook>>

}