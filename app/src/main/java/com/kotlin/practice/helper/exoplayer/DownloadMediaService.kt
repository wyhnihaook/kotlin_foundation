package com.kotlin.practice.helper.exoplayer

import android.app.Notification
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Scheduler
import com.kotlin.practice.R

/**
 * 描述:下载媒体服务
 * 功能介绍:可多线程执行
 * 创建者:翁益亨
 * 创建日期:2023/2/28 10:37
 */
private const val FOREGROUND_NOTIFICATION_ID = 1
private const val DOWNLOAD_NOTIFICATION_CHANNEL_ID = "download_channel"

class DownloadMediaService : DownloadService(
    FOREGROUND_NOTIFICATION_ID,
    DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    DOWNLOAD_NOTIFICATION_CHANNEL_ID, R.string.exo_channel_name, 0
) {

    private val JOB_ID = 1


    override fun getDownloadManager(): DownloadManager {
        TODO("Not yet implemented")
    }

    override fun getScheduler(): Scheduler? {
        TODO("Not yet implemented")
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        TODO("Not yet implemented")
    }
}