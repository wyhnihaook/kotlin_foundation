package com.kotlin.practice.db.book

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * 描述:查询书本信息
 * 功能介绍:用户下的书本查询
 * 创建者:翁益亨
 * 创建日期:2023/1/6 15:30
 */
@Dao
interface BookDao {

    //书本归属插入，相同主键如果存在就替换（如果不添加该策略，相同主键存在时会抛出异常）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg books: Book)


    //批量删除数据
    @Query("delete from book where user_id in (:userIdList)")
    fun deleteUserBookList(userIdList:IntArray)

}