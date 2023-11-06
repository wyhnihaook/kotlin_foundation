package com.kotlin.practice.helper.webview.download

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.text.TextUtils
import android.webkit.WebResourceResponse
import com.kotlin.practice.helper.webview.WebViewConfig
import com.kotlin.practice.helper.webview.WebViewSession
import com.kotlin.practice.helper.webview.WebViewSessionThreadPool
import com.kotlin.practice.helper.webview.WebViewUtils
import com.kotlin.practice.util.logError
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 描述:下载资源的引擎
 * 功能介绍:通过改类进行资源异步下载/通知主线程的操作
 * 创建者:翁益亨
 * 创建日期:2023/8/14 15:54
 */
class WebViewDownloadEngine(cache: WebViewDownloadCache?) :Handler.Callback{


    companion object{

        /**
         * message code enqueue:
         * when downloading tasks more than config number, the task should enqueue for waiting.
         */
        private const val MSG_ENQUEUE = 0

        /**
         * message code: one download task is complete and the download queue is free.
         */
        private const val MSG_DEQUEUE = 1


        /**
         * A queue implementation using [LinkedHashMap].
         * A queue with map function.
         */
        private class SonicDownloadQueue :
            LinkedHashMap<String?, WebViewDownloadClient.Companion.DownloadTask?>() {
            @Synchronized
            fun dequeue(): WebViewDownloadClient.Companion.DownloadTask? {
                if (values.iterator().hasNext()) {
                    val task: WebViewDownloadClient.Companion.DownloadTask? = values.iterator().next()
                    return remove(task?.mResourceUrl)
                }
                return null
            }

            @Synchronized
            fun enqueue(task: WebViewDownloadClient.Companion.DownloadTask?) {
                if (task != null && !TextUtils.isEmpty(task.mResourceUrl)) {
                    put(task.mResourceUrl, task)
                }
            }
        }


    }

    private val resourceTasks: ConcurrentMap<String, WebViewDownloadClient.Companion.DownloadTask> =
        ConcurrentHashMap()


    /**
     * the download task queue.
     */
    private var mQueue: SonicDownloadQueue? = null

    /**
     * download thread handler.
     */
    private var mHandler: Handler? = null

    /**
     * number of downloading tasks.
     */
    private var mNumOfDownloadingTask: AtomicInteger? = null

    /**
     * A download cache.
     */
    private var mCache: WebViewDownloadCache? = null

    /**
     * 构造函数
     */
    init{
        mQueue = SonicDownloadQueue()
        val queueThread = HandlerThread("Download-Thread")
        queueThread.start()
        mHandler = Handler(queueThread.looper, this)
        mNumOfDownloadingTask = AtomicInteger(0)
        mCache = cache
    }


    override fun handleMessage(msg: Message): Boolean {
        when (msg.what) {
           MSG_ENQUEUE -> {
                val task: WebViewDownloadClient.Companion.DownloadTask = msg.obj as WebViewDownloadClient.Companion.DownloadTask
                mQueue!!.enqueue(task)
                task.mState.set(WebViewDownloadClient.Companion.DownloadTask.STATE_QUEUEING)
               logError("${ "enqueue sub resource(" + task.mResourceUrl + ")."}")
            }

            MSG_DEQUEUE -> {
                if (!mQueue!!.isEmpty()) {
                    val task: WebViewDownloadClient.Companion.DownloadTask? = mQueue!!.dequeue()
                    if(task != null){
                        startDownload(task)
                        logError("${"dequeue sub resource(" + task?.mResourceUrl + ")."}")
                    }
                }
            }

            else -> {}
        }
        return false
    }

    /**
     * start downloading one resource.
     * if the responding cache exists and isn't expire, will use the cache directly and won't launch a http request;
     * if the number of downloading tasks is bigger than config, the task will be delayed before downloading pool is free.
     *
     * @param resourceUrl the resource's url
     * @param ipAddress if dns prefetch the ip address, will use the ip instead of host
     * @param cookie set the cookie for the download http request
     * @param callback a callback used for notify the download progress and result
     * @return the download task info
     */
    fun download(
        resourceUrl: String,
        ipAddress: String,
        cookie: String,
        callback: WebViewDownloadCallback?
    ): WebViewDownloadClient.Companion.DownloadTask? {
        if (TextUtils.isEmpty(resourceUrl)) {
            return null
        }
        synchronized(mQueue!!) {
            if (mQueue!!.containsKey(resourceUrl)) {
                logError("${"sub resource download task has been in queue ($resourceUrl)."}")
                return mQueue!![resourceUrl]
            }
        }
        val task = WebViewDownloadClient.Companion.DownloadTask()
        task.mResourceUrl = resourceUrl
        if(callback!=null){
            task.mCallbacks.add(callback)
        }
        task.mCallbacks.add(object : WebViewDownloadCallback.SimpleDownloadCallback() {
            override fun onFinish() {
                //在内容加载完毕之后，这里会发送通知，将队列中等待的队列去除并执行下载操作
                task.mState.set(WebViewDownloadClient.Companion.DownloadTask.STATE_DOWNLOADED)
                mHandler!!.sendEmptyMessage(MSG_DEQUEUE)
            }
        })

        // query cache
        val resourceBytes = mCache!!.getResourceCache(resourceUrl)
        if (resourceBytes != null) {
            task.mInputStream = ByteArrayInputStream(resourceBytes)
            task.mRspHeaders = mCache!!.getResourceCacheHeader(resourceUrl)
            task.mState.set(WebViewDownloadClient.Companion.DownloadTask.STATE_LOAD_FROM_CACHE)
            logError("${"load sub resource($resourceUrl) from cache."}")

            return task
        }

        // no cache then start download
        task.mIpAddress = ipAddress
        task.mCookie = cookie
        if (mNumOfDownloadingTask!!.get() < WebViewConfig.maxDownloadCount
            //同一时间最大下载个数
        ) {
            startDownload(task)
        } else {
            val enqueueMsg = mHandler!!.obtainMessage(
                MSG_ENQUEUE,
                task
            )
            mHandler!!.sendMessage(enqueueMsg)
        }
        return task
    }

    /**
     * dispatch the download task to really download.
     *
     * @param task download task
     */
    private fun startDownload(task: WebViewDownloadClient.Companion.DownloadTask) {
        //往线程池中添加任务，执行下载资源功能
        WebViewSessionThreadPool.postTask(Runnable {
            mNumOfDownloadingTask!!.incrementAndGet()
            task.mState.set(WebViewDownloadClient.Companion.DownloadTask.STATE_DOWNLOADING)
            val engine = WebViewDownloadClient(task)
            engine.download()
        })
    }

    /**
     * When the webview initiates a sub resource interception, the client invokes this method to retrieve the data
     * 检索本地资源是否符合html的需求，存在则直接返回，不存在则返回null，使用网络请求获取资源文件
     * @param url The url of sub resource
     * @param session current sonic session
     * @return Return the data to kernel
     */

    fun onRequestSubResource(url: String
                             , session: WebViewSession
    ): Any? {
        logError("${"session onRequestSubResource: resource url($url)."}")

        var inputStream: InputStream? = null
        var headers: Map<String, List<String>>? = null
        if (resourceTasks.containsKey(url)) {
            val subRes: WebViewDownloadClient.Companion.DownloadTask? = resourceTasks[url]
            if(subRes != null){
                //WebView拦截资源获取是，要取消当前所有下载完成后的回调
                subRes.mWasInterceptInvoked.set(true)
                if (subRes.mState.get() === WebViewDownloadClient.Companion.DownloadTask.STATE_INITIATE
                    || subRes.mState.get() === WebViewDownloadClient.Companion.DownloadTask.STATE_QUEUEING
                ) {
                    return null
                } else {
                    if (subRes.mInputStream == null) {
                        return null
                    }
                    inputStream = subRes.mInputStream
                    headers = subRes.mRspHeaders
                }
            }

        } else {
            return null
        }
        val webResourceResponse: WebResourceResponse?
        //标识只有页面没有被回收的时候能正常返回数据
        if (!session.isDestroyedOrWaitingForDestroy()) {
            val mime: String = WebViewUtils.getMime(url)
//            val filteredHeaders: Map<String, String> = WebViewUtils.getFilteredHeaders(headers)
            //原生的返回结构体内容，这里的输入流结束之后，会自动执行close操作，第一次是自定义的inputStream后续直接从缓存中获取时的输入流是系统的InputStream
            webResourceResponse = WebResourceResponse(
                mime,
                "utf-8",
//                session.getCharsetFromHeaders(filteredHeaders),
                inputStream
//                , filteredHeaders
            )

        } else {
            webResourceResponse = null
            logError("${"session onRequestSubResource error: session is destroyed!"}")
        }
        return webResourceResponse
    }

    /**
     * preload the sub resource in the "sonic-link" header.
     * 预加载列表信息传递/网络连接的时候从html中先获取对应的需要加载的资源信息，同步到此
     * @param preloadLinks The links which need to be preloaded.
     */
    fun addSubResourcePreloadTask(preloadLinks: List<String>) {
//        val runtime: SonicRuntime = SonicEngine.getInstance().getRuntime()
        for (link in preloadLinks) {
            if (!resourceTasks.containsKey(link)) {
                resourceTasks[link] = download(
                    link,"","",
//                    runtime.getHostDirectAddress(link),
//                    runtime.getCookie(link),
                    WebViewDownloadClient.Companion.SubResourceDownloadCallback(link)
                )
            }
        }
    }



}