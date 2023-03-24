package com.kotlin.practice.util

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.IntentFilter
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.net.toFile
import com.kotlin.practice.base.BaseApp
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService


/**
 * 描述:下载文件工具类
 * 功能介绍:下载文件工具类（单任务处理）
 * 创建者:翁益亨
 * 创建日期:2023/1/17 15:46
 */

/**
 * 下载资源的方法
 * @param block 用来拓展当前的方法
 * */
fun download(url: String,block:DownloadRequestBuilder.()->Unit):Long = DownloadRequestBuilder(url).apply(block).build()

//moveToFirst的值是true或者false，用来确定查询结果是否为空。在这里判定是否有存在下载的文件
inline fun <R> DownloadManager.query(downloadId: Long, block: (Cursor) -> R): R? =
    query(DownloadManager.Query().setFilterById(downloadId))?.use { cursor ->
        if (cursor.moveToFirst()) block(cursor) else null
    }

/**
 * 创建请求的实例内容
 * */
class DownloadRequestBuilder internal constructor(url:String){

    private val application = BaseApp.getContext()

    //1.构造根据url发起的请求对象
    private val request = DownloadManager.Request(Uri.parse(url))

    //2.获取系统级服务，用于管理启动
    private val downloadManager = application.getSystemService<DownloadManager>()

    //3.基于线程池创建的任务类，在线程里执行对应任务
    private var scheduleExecutor:ScheduledExecutorService? = null

    //完成下载的回调
    private var onComplete:((Uri)->Unit)? = null

    //下载状态变更回调
    private var onChange:((downloadedSize:Int,totalSize:Int,status:Int)->Unit)? = null

    //下载进度监听
    private var progressObserver : ContentObserver? = null

    //下载的Id信息记录
    private var downloadId : Long = -1L

    private val query = DownloadManager.Query()

    //4.配置网络请求相关
    fun addHeader(header:String,value:String){
        request.addRequestHeader(header,value)
    }

    //5.配置下载到本地的目标地址（私有/共有的内存地址）
    //默认下载的地址是getExternalFilesDir获取的根目录,所以不用特别进行路径设置。对应data/应用包名/files下
    //会自动创建存储的文件目录（！！！！不需要权限！！！！）
    fun destinationInExternalFilesDir(dirType:String,subPath:String){
        request.setDestinationInExternalFilesDir(application,dirType,subPath)
    }

    //下载的目录是SD卡目录，只能是 Environment中支持已存在的公共目录！！！不能通过新建存储数据
    //对应目录信息： DIRECTORY_MUSIC, DIRECTORY_PODCASTS, DIRECTORY_RINGTONES, DIRECTORY_ALARMS, DIRECTORY_NOTIFICATIONS, DIRECTORY_PICTURES, DIRECTORY_MOVIES, DIRECTORY_DOWNLOADS, DIRECTORY_DCIM, or DIRECTORY_DOCUMENTS.
    //路径：/storage/emulated/0/Music
    fun destinationInExternalPublicDir(dirType:String,subPath:String){
        request.setDestinationInExternalPublicDir(dirType,subPath)
    }

    //6.配置回调内容
    fun onComplete(block:(Uri)->Unit){
        onComplete = block
    }

    fun onChange(block:(downloadedSize:Int,totalSize:Int,status:Int)->Unit){
        onChange = block
    }

    //7.开始执行请求下载文件
    internal fun build():Long{
        downloadId = downloadManager?.enqueue(request)?:-1
        query.setFilterById(downloadId)

        //注意：下载管理器的内容uri应该是content://downloads/my_downloads，我们可以监视这个数据库的变化。就是DownloadManager中的Downloads.Impl.CONTENT_URI。所有的内容提供者业务操作都是基于此
        //当您使用下载ID开始下载时，将会创建一行content://downloads/my_downloads/{downloadId}。我们可以检查这个指针来知道这个任务是否被取消。如果返回的游标为空或空，则在数据库中找不到记录，此下载任务将被用户取消
        progressObserver = onChange?.let { DownloadProgressObserver() }?.also {
            application.contentResolver.registerContentObserver(Uri.parse("content://downloads/my_downloads"), true, it)
        }
        //注册一个广播，完成下载时的通知接收
        application.registerReceiver(DownloadCompleteReceiver(), IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        return downloadId
    }




    /**
     * 注册下载进度监听器
     * */
    private inner class DownloadProgressObserver : ContentObserver(null){

        init {
            scheduleExecutor = Executors.newSingleThreadScheduledExecutor()
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)

            //查看进度
            downloadManager?.query(downloadId) { cursor ->
                val downloadedSize = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val totalSize = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

                onChange?.invoke(downloadedSize, totalSize, status)
            }
        }
    }


    /**
     * 完成下载广播接收
     * */
    private inner class DownloadCompleteReceiver : BroadcastReceiver(){

        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1)

            if(id == downloadId){
                if(scheduleExecutor?.isShutdown != true){
                    scheduleExecutor?.shutdown()
                }

                progressObserver?.let {
                    application.contentResolver.unregisterContentObserver(it)
                    progressObserver = null
                }

                onComplete?.let { onComplete ->
                    downloadManager?.query(downloadId) { cursor ->
                        val uriString = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                        onComplete(Uri.parse(uriString))
                    }
                }
                application.unregisterReceiver(this)
            }
        }

    }
}

/**下载过程中的状态*/
fun downloadStatus(status: Int):String = when(status){
    //1<<3 等同于 2的3次方 = 8
    DownloadManager.STATUS_SUCCESSFUL->"下载成功"
    DownloadManager.STATUS_FAILED->"下载失败"
    DownloadManager.STATUS_PAUSED->"下载暂停"
    DownloadManager.STATUS_RUNNING->"下载中"
    DownloadManager.STATUS_PENDING->"下载延迟"
    else->"未知状态"
}


/**安装下载的apk应用
 * @param uri 本地uri路径
 * */
fun installApplication(activity: AppCompatActivity, uri: Uri){

    val file =uri.toFile()

    Intent(ACTION_VIEW).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION

            val contentUri = file.getUriFromFile(activity)

            setDataAndType(contentUri,"application/vnd.android.package-archive")
        }else{
            setDataAndType(uri,"application/vnd.android.package-archive")
        }

        activity.startActivity(this)
    }


}