package com.kotlin.practice.util

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.kotlin.practice.db.theme.AppTheme

/**
 * 描述:切换主题工具类
 * 功能介绍:深色/亮色模式处理
 * 创建者:翁益亨
 * 创建日期:2023/2/8 9:29
 */
object ThemeUtils {

    /**判断系统是否开启了深色模式*/
    fun isDarkTheme(context: Context):Boolean{
        val flag = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK

        return flag == Configuration.UI_MODE_NIGHT_YES
    }

    /**！！！！以下执行的操作必须在主线程工作！！！！*/

    /**打开深色模式*/
    fun openDarkTheme(block:()->Unit){
        mainThread{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            block.invoke()
        }
    }

    /**关闭深色模式*/
    fun closeDarkTheme(block:()->Unit){
        mainThread {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            block()
        }
    }

    /**跟随系统设置主题模式*/
    fun systemTheme(block:()->Unit){
        mainThread {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            block.invoke()
        }
    }
}