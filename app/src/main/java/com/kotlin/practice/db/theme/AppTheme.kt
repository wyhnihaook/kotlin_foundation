package com.kotlin.practice.db.theme

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.kotlin.practice.util.AppThemeKey

/**
 * 描述:App内中的模式记录
 * 功能介绍:App内中的模式记录
 * 创建者:翁益亨
 * 创建日期:2023/2/7 15:38
 */
@Entity(tableName = "AppTheme", primaryKeys = ["name"])
data class AppTheme(

    //当前挂钩的用户信息，等同于用户的唯一标识uid,每次添加直接覆盖原有数据即可
    @ColumnInfo(name = "name") //不写则使用变量名做为列名
    var name:String = AppThemeKey,

    @ColumnInfo(name = "theme_mode")
    var themeMode :Int ,
)
