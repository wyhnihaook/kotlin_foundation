package com.kotlin.practice.base

import android.content.Context
import android.util.Log
import androidx.startup.Initializer
import com.kotlin.practice.BuildConfig
import com.kotlin.practice.R
import com.kotlin.practice.contentprovider.initContentProviderContext
import com.kotlin.practice.db.AppDatabase
import com.kotlin.practice.util.*
import com.scwang.smart.refresh.header.BezierRadarHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 描述:Application的onCreate执行的初始化内容迁移到这里
 * 功能介绍:启动本地方法/三方sdk初始化入口
 * 创建者:翁益亨
 * 创建日期:2023/2/15 14:58
 */
class AppInitializer : Initializer<Unit> {

    init {
        initLogger(BuildConfig.DEBUG, object : LoggerPrinter {
            override fun log(level: LogLevel, tag: String, message: String, thr: Throwable?) {
                when (level) {
                    LogLevel.VERBOSE -> if (thr == null) Log.v(tag, message) else Log.v(
                        tag,
                        message,
                        thr
                    )
                    LogLevel.DEBUG -> if (thr == null) Log.d(tag, message) else Log.d(
                        tag,
                        message,
                        thr
                    )
                    LogLevel.INFO -> if (thr == null) Log.i(tag, message) else Log.i(
                        tag,
                        message,
                        thr
                    )
                    LogLevel.WARN -> if (thr == null) Log.w(tag, message) else Log.w(
                        tag,
                        message,
                        thr
                    )
                    LogLevel.ERROR -> if (thr == null) Log.e(tag, message) else Log.e(
                        tag,
                        message,
                        thr
                    )
                }
            }

            override fun logWtf(tag: String, message: String, thr: Throwable?) {
                if (thr == null) Log.wtf(tag, message) else Log.wtf(tag, message, thr)
            }
        })


        //设置全局的Header构建器.不适应深色亮色模式
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, layout ->
            layout.apply {
                setPrimaryColorsId(R.color.black, R.color.white)//全局设置主题颜色。第一个是背景色，第二个是主题色
            }
            BezierRadarHeader(context)
        }
    }

    //1.初始化操作，返回的初始化结果会被缓存，不会重复执行
    override fun create(context: Context) {
        logError("执行初始化信息")

        //注意：在清单文件移除ContentProvider的初始化，需要要将将原有的ContentProvider初始化context的方法放到这里
        initContentProviderContext(context)

        initThemeMode(context)
    }

    //2.当前的初始化是否还依赖于其他的Initializer，如果有的话，就在这里进行配置，App Startup会保证先初始化依赖的Initializer，然后才会初始化当前，这样就可以设置初始化顺序了
    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()


    /**初始化主题信息，主题适配时，不能在xml添加配置监听uiMode，不然会在修改之后再去查询系统的配置，导致部分页面会跟随系统而不是本地配置*/
    private fun initThemeMode(context: Context) {
        //需要判断当前是否有存在的深色/亮色模式
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                var db = AppDatabase.getInstance()
                val mode = db.appThemeDao().queryThemeMode(AppThemeKey)
                mode?.let {
                    //存在数据库主题的情况
                    //获取系统当前展示的内容
                    val isSystemDark = ThemeUtils.isDarkTheme(context)

                    when {
                        it.themeMode == ThemeMode.NIGHT.mode && !isSystemDark -> ThemeUtils.openDarkTheme { }
                        it.themeMode == ThemeMode.LIGHT.mode && isSystemDark -> ThemeUtils.closeDarkTheme { }
                    }

                }
            }
        }
    }
}


//jetpack的App StartUp出现原因解析
//1.明确设置初始化顺序
//2.共享单个ContentProvider（部分第三方库会利用ContentProvider在应用启动时初始化的特性，来获取Context。启动过多的 ContentProvider 会增加应用的启动时间）

//对于ContentProvider解析文章参考：https://www.jianshu.com/p/8f7ba9e88cd0
//ContentProvider 通常的用法是为当前进程 / 远程进程提供内容服务，【它们会在应用启动的时候初始化】，正因如此，我们可以利用 ContentProvider 来获得 Context
//在ActivityThread中实例化Context之后，会执行attachBaseContext回调。然后会对程序中所有的ContentProvider进行初始化
// （检索所有library的AndroidManifest.xml,获取其中注册的provider标签，并进行对于contentProvider的初始化，将context同步给对于的library）/占用主线程的初始化时间
//！！！！ContentProvider无法进行懒加载！！！！

//AppStartup 的做法是：合并所有用于初始化的ContentProvider ，减少创建 ContentProvider，并提供全局管理
//App Startup 合并所有用于初始化的 ContentProvider，合并后的 ContentProvider 就是 InitializationProvider（清单文件中注册）