package com.kotlin.practice.ui.theme


import androidx.lifecycle.MutableLiveData
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.db.AppDatabase
import com.kotlin.practice.db.theme.AppTheme
import com.kotlin.practice.util.AppThemeKey
import com.kotlin.practice.util.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 描述:主题数据处理类
 * 功能介绍:处理主题切换，保存当前主题信息
 * 创建者:翁益亨
 * 创建日期:2023/2/7 15:26
 */

class ThemeOperateViewModel:BaseViewModel() {

    data class ThemeData(var isCheck: Boolean, var themeName: String,var themeMode:Int)


    override val title: String
        get() = BaseApp.getContext().getString(R.string.theme_page)

    var themeList = mutableListOf<ThemeData>()
    
    /**默认是跟随系统的状态*/
    var themeMode = MutableLiveData(ThemeMode.UNKNOWN.mode)

    //每次操作需要数据库存储当前主题模式，下次进入依然生效
    fun queryThemeMode(block:()->Unit){
        launch {
            withContext(Dispatchers.IO){
                var db = AppDatabase.getInstance()
                val mode = db.appThemeDao().queryThemeMode(AppThemeKey)
                mode?.let {
                    themeMode.postValue(mode.themeMode)
                }

            }

            block.invoke()
        }
    }

    /**更新存储的状态*/
    fun updateThemeMode(appTheme: AppTheme){
        launch {
            withContext(Dispatchers.IO){
                var db = AppDatabase.getInstance()
                db.appThemeDao().insertThemeMode(appTheme)
                themeMode.postValue(appTheme.themeMode)
            }
        }
    }



}