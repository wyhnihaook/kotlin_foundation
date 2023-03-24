package com.kotlin.practice.db.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.kotlin.practice.db.book.Book
import com.kotlin.practice.db.user.User

/**
 * 描述:用户和书本关联
 * 功能介绍:用户和书本关联
 * 创建者:翁益亨
 * 创建日期:2023/1/6 15:50
 */
data class UserAndBook(
    @Embedded val user: User,//标识当前是一个对应N个（N范围是1-多个）

    //查询两个表中匹配的数据，用于过滤后续关联的数据
    @Relation(
        parentColumn = "uid",
        entityColumn = "user_id"
    )//这里直接跟随关联的数据结构
    val bookList: List<Book>

)
