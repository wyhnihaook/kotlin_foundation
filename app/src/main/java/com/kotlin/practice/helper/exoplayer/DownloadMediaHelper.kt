package com.kotlin.practice.helper.exoplayer

import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.DefaultDatabaseProvider
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.NoOpCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.kotlin.practice.base.application
import java.io.File
import java.util.concurrent.Executors

/**
 * 描述:下载帮助类
 * 功能介绍:下载媒体类的帮助类
 * 创建者:翁益亨
 * 创建日期:2023/2/28 14:01
 */
object DownloadMediaHelper {


    /**
     * Whether the demo application uses Cronet for networking. Note that Cronet does not provide
     * automatic support for cookies (https://github.com/google/ExoPlayer/issues/5975).
     *
     * <p>If set to false, the platform's default network stack is used with a {@link CookieManager}
     * configured in {@link #getHttpDataSourceFactory}.
     */
    private val USE_CRONET_FOR_NETWORKING = true

    private val DOWNLOAD_CONTENT_DIRECTORY = "downloads";

    private val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel";

    private var dataSourceFactory: DataSource.Factory? = null
    private lateinit var httpDataSourceFactory: DataSource.Factory

    private lateinit var databaseProvider: DatabaseProvider

    private var downloadDirectory: File? = null

    private var downloadCache: Cache? = null

    private var downloadManager: DownloadManager? = null
//    private lateinit var downloadTracker:DownloadTracker

    private var downloadNotificationHelper: DownloadNotificationHelper? = null

    //返回是否应使用扩展渲染器
    fun useExtensionRenderers(): Boolean = true

    fun buildRenderersFactory(preferExtensionRenderer: Boolean): RenderersFactory {
        var extensionRendererMode =
            if (useExtensionRenderers()) if (preferExtensionRenderer) DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON else DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF
        return DefaultRenderersFactory(application).setExtensionRendererMode(extensionRendererMode)
    }


//    @Synchronized
//    fun getHttpDataSourceFactory():DataSource.Factory{
//        if(httpDataSourceFactory == null){
//            HttpDataSource
//            if(USE_CRONET_FOR_NETWORKING){
//                var cronetEngine = Cron
//            }
//        }
//    }


    @Synchronized
    fun getDownloadCache(): Cache {
        if (downloadCache == null) {
            val downloadContentDirectory = File(getDownloadDirectory(), DOWNLOAD_CONTENT_DIRECTORY)
            downloadCache = SimpleCache(
                downloadContentDirectory, NoOpCacheEvictor(),
                getDatabaseProvider()
            )
        }

        return downloadCache!!
    }


//    @Synchronized
//    fun getDataSourceFactory():DataSource.Factory{
//        if(dataSourceFactory == null){
//            val upstreamFactory = DefaultDataSource(application, httpDataSourceFactory)
////            dataSourceFactory = buildReadOnlyCacheDataSource()
//        }
//    }

    @Synchronized
    fun getDownloadNotificationHelper(): DownloadNotificationHelper {
        if (downloadNotificationHelper == null) {
            downloadNotificationHelper =
                DownloadNotificationHelper(application, DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        }
        return downloadNotificationHelper!!
    }


    @Synchronized
    fun getDownloadManager(): DownloadManager {
//        ensureDownloadManagerInitialized()
        return downloadManager!!
    }


//    @Synchronized
//    fun getDownloadTracker():DownloadTracker{
//
//    }


//    @Synchronized
//    fun ensureDownloadManagerInitialized() {
//        if (downloadManager == null) {
//            downloadManager = DownloadManager(
//                application,
//                getDatabaseProvider(),
//                getDownloadCache(),
//                getHttpDataSourceFactory(),
//                Executors.newFixedThreadPool(/* nThreads= */ 6)
//            )
//
////            downloadTracker = todo
//        }
//    }


    @Synchronized
    fun getDatabaseProvider(): DatabaseProvider {

        if (databaseProvider == null) {
            databaseProvider = ExoDatabaseProvider(application)
        }
        return databaseProvider
    }


    @Synchronized
    fun getDownloadDirectory(): File {
        if (downloadDirectory == null) {
            downloadDirectory = application.getExternalFilesDir(null)
            if (downloadDirectory == null) {
                downloadDirectory = application.filesDir
            }
        }
        return downloadDirectory!!
    }


    fun buildReadOnlyCacheDataSource(
        upstreamFactory: DataSource.Factory,
        cache: Cache
    ): CacheDataSource.Factory =
        CacheDataSource.Factory().setCache(cache).setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(null).setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
}