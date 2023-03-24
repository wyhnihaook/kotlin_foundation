package com.kotlin.practice.base

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import com.kotlin.practice.BuildConfig
import com.kotlin.practice.R
import com.kotlin.practice.db.AppDatabase
import com.kotlin.practice.util.*
import com.scwang.smart.refresh.header.BezierRadarHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 描述:应用启动时初始化类
 * 功能介绍:初始化应用配置的数据（生命周期等同于应用的生命周期）
 * 创建者:翁益亨
 * 创建日期:2023/1/5 16:50
 */
lateinit var application:Context
    internal set

class BaseApp : Application(), CameraXConfig.Provider {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        application = this
        baseApplication = this
    }

    companion object {
        private lateinit var baseApplication: BaseApp

        fun getContext(): Context {
            return baseApplication
        }
    }


    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig())
            .setMinimumLoggingLevel(
                Log.ERROR
            ).build()
    }



}