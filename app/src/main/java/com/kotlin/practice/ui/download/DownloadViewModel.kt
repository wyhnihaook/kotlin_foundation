package com.kotlin.practice.ui.download

import android.Manifest
import android.app.DownloadManager
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.MutableLiveData
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.util.*

/**
 * 描述:下载任务内容处理
 * 功能介绍:下载任务内容处理
 * （如果要实现断点续传就要指定下载的文件指定范围的大小，结合RandomAccessFile从指定位置来写入数据）
 * 创建者:翁益亨
 * 创建日期:2023/2/13 15:39
 */
class DownloadViewModel:BaseViewModel() {

    lateinit var permissionInstallLauncher: ActivityResultLauncher<String>

    override val title: String
        get() = BaseApp.getContext().getString(R.string.download_page)

    var editUrl = MutableLiveData<String>()

    var downloadProgress = MutableLiveData(0)

    var downloadStatus = MutableLiveData("下载状态：")

    /**任务开始时，记录当前的任务id*/
    var taskId = -1L

    /**设置默认下载的url*/
    fun loadDefaultUrl(any:Any){
        editUrl.value = "https://microdown.myapp.com/ug/20230209_50c7397d21720ca70e783f8e01fd421d_sign_v1.apk"
    }

    fun downloadUrl(any:Any){
        if (TextUtils.isEmpty(editUrl.value)) {
           (any as View).context.toast("下载地址不能为空")

        } else {
            permissionInstallLauncher.launchX(Manifest.permission.REQUEST_INSTALL_PACKAGES)
        }
    }

    fun startDownload(activity: AppCompatActivity){
        //打开授权后，就直接执行这里
        //下载功能
        val pdfUrl = editUrl.value!!

        taskId = download(pdfUrl) {
            this.onComplete {
                logError("下载完成：$it")

                downloadProgress.postValue(100)
                downloadStatus.postValue("下载状态：下载完毕")

                //如果是要安装的应用的情况
                //需要打开安装未知应用的权限，部分机型会有系统提示允许安装。兼容所有情况就需要提前设置安装
                //权限控制
                installApplication(activity, it)
            }

            this.onChange { downloadedSize: Int, totalSize: Int, status: Int ->
                logError("下载中-> downloadedSize:$downloadedSize,totalSize:$totalSize,status:$status,progress:${(100f * downloadedSize / totalSize).toInt()}")
                //这里的进度情况，可以模拟一个进度的增加，目前直接定位进度即可
                if (totalSize > 0) {
                    downloadStatus.postValue(downloadStatus(status))
                    downloadProgress.postValue((100f * downloadedSize / totalSize).toInt())
                }

            }

            //指定下载路径和文件名，以下两个方法都不用内存的写入权限
            //指定在当前apk路径下的files/当前设定的文件夹下（这里设置的是download）/文件（内部存储），会根据当前应用的卸载而消失
            this.destinationInExternalFilesDir(
                "download",
                "20230209_50c7397d21720ca70e783f8e01fd421d_sign_v1.apk"
            )
            //指定外部公共下载空间中的url（公共的外部存储），不会根据当前的任务卸载而消失
//            this.destinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"byt_v2.7.3_39_diyi_normal_release_20230213-175056.apk)
        }
    }

    fun removeTask(any:Any){
        if(taskId<0){
            (any as View).context.toast("任务未开始")
            return
        }
        val downloadManager = BaseApp.getContext().getSystemService<DownloadManager>()

        downloadManager?.remove(taskId)

        downloadProgress.postValue(0)
        downloadStatus.postValue("下载状态：已取消")

        taskId = -1L
    }
}






