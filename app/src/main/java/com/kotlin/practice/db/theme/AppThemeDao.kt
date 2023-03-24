package com.kotlin.practice.db.theme

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * 描述:操作数据库样式表
 * 功能介绍:查询、插入操作
 * 创建者:翁益亨
 * 创建日期:2023/2/7 15:44
 */
@Dao
interface AppThemeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertThemeMode(vararg themeMode: AppTheme)

    @Query("select * from APPTHEME where name like (:name) limit 1")
    fun queryThemeMode(name:String):AppTheme?
}