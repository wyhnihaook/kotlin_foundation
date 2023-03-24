package com.kotlin.practice.db.book

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * 描述:用户关联的书本信息
 * 功能介绍:一个用户持有多个书本实例
 * 创建者:翁益亨
 * 创建日期:2023/1/6 15:27
 */
@Entity(tableName = "Book", primaryKeys = ["user_id","book_name","book_author"])
data class Book(
    //以书名和作者设置唯一标识来做处理


    //当前挂钩的用户信息，等同于用户的唯一标识uid
    @ColumnInfo(name = "user_id")
    var userId :Int ,

    @ColumnInfo(name = "book_name")
    var bookName:String,

    @ColumnInfo(name = "book_author")
    var bookAuthor:String,

    @Ignore
    var price:Int?=0
){
    constructor():this(0,"","",0)
}
