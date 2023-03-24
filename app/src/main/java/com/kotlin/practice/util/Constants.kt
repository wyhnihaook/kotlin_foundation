package com.kotlin.practice.util

/**
 * 描述:常量合集
 * 功能介绍:常量合集
 * 创建者:翁益亨
 * 创建日期:2023/2/7 15:54
 */
typealias defaultFunction = ()->Unit

//作为唯一name进行数据库AppTheme表查询和插入
const val AppThemeKey = "AppThemeKey"

/**这里定义枚举，用来处理具体的主题类型*/
enum class ThemeMode(var mode:Int,var themeName:String){
    UNKNOWN(-1,"未知"),//未知的状态，一般用于初始化
    SYSTEM(0,"跟随系统"),//跟随系统
    NIGHT(1,"深色模式"),//固定显示深色
    LIGHT(2,"高亮模式")//固定显示亮色
}