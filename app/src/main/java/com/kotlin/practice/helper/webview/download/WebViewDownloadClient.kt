package com.kotlin.practice.helper.webview.download

import android.text.TextUtils
import com.kotlin.practice.helper.webview.WebViewConstants
import com.kotlin.practice.helper.webview.WebViewSessionStream
import com.kotlin.practice.helper.webview.WebViewUtils
import com.kotlin.practice.util.logError
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.zip.GZIPInputStream

/**
 * 描述:缓存下载功能
 * 功能介绍:网络请求
 * 创建者:翁益亨
 * 创建日期:2023/8/14 15:57
 */
const val HTTP_HEAD_FIELD_COOKIE: String = "Cookie"

class WebViewDownloadClient(private val mTask: DownloadTask): WebViewSessionStream.Callback {

    /**
     * A download connection implement.
     */
    private var mConn: WebViewDownloadConnection = WebViewDownloadConnection(mTask.mResourceUrl?:"",mTask)


    private var mOutputStream: ByteArrayOutputStream = ByteArrayOutputStream()

    /**
     * whether the download task is finished or is a bridge stream
     */
    private var mDownloadFinished = false


    companion object{

        /**
         * download buffer size
         */
        private const val READ_BUFFER_SIZE = 2048

        /**
         * Task which record the download info
         */
        class DownloadTask {
            /**
             * url of the resource to be download
             */
            var mResourceUrl: String? = null

            /**
             * ip address instead of host to launch a http request
             */
            var mIpAddress: String? = null

            /**
             * cookie to be set in the http download request
             */
            var mCookie: String? = null

            /**
             * the download request's response headers
             */
            var mRspHeaders: Map<String, List<String>>? = null

            /**
             * the network stream or memory stream or the bridge stream
             */
            var mInputStream: InputStream? = null

            /**
             * the task's download state
             */
            var mState = AtomicInteger(STATE_INITIATE)

            /**
             * whether the task's responding resource was intercepted by kernel
             */
            val mWasInterceptInvoked = AtomicBoolean(false)

            /**
             * list of download callback
             */
            var mCallbacks = ArrayList<WebViewDownloadCallback>()

            companion object {
                /**
                 * download in initiate state.
                 */
                const val STATE_INITIATE = 0

                /**
                 * download in queueing state.
                 */
                const val STATE_QUEUEING = 1

                /**
                 * the task is in downloading state.
                 */
                const val STATE_DOWNLOADING = 2

                /**
                 * the task is in download complete state.
                 */
                const val STATE_DOWNLOADED = 3

                /**
                 * the task is load from cache, not from network.
                 */
                const val STATE_LOAD_FROM_CACHE = 4
            }
        }

        /**
         * sub resource download callback.
         */
        class SubResourceDownloadCallback(private val resourceUrl: String) :
            WebViewDownloadCallback.SimpleDownloadCallback() {
            override fun onStart() {
                logError("${"session start download sub resource, url=$resourceUrl"}")
            }

            override fun onSuccess(content: ByteArray, rspHeaders: Map<String, List<String>>) {
                // save cache files / 下载资源文件之后的保存操作
                logError("${"finish download, url=$resourceUrl"}")
                val fileName: String = WebViewUtils.getMD5(resourceUrl)?:""
                WebViewUtils.saveResourceFiles(fileName, content, rspHeaders)
                // save resource data to db
                WebViewUtils.saveSonicResourceData(
                    resourceUrl,
                    WebViewUtils.getSHA1(content)?:"",
                    content.size*1L
                )
            }

            override fun onError(errorCode: Int) {
                logError("${"session download sub resource error: code = $errorCode, url=$resourceUrl"}")
            }
        }

    }


    /**
     * download the resource and notify download progress
     * 保存的逻辑存放在onSuccess回调中，也就是说读取在下载完毕之前的，都不会存储
     * onClose中再未下载的资源文件中也不会调用，只会调用已经缓存提供给WebView的内容
     * @return response code
     */
    fun download(): Int {
        onStart()
        val resultCode = mConn!!.connect()
        if (WebViewConstants.ERROR_CODE_SUCCESS !== resultCode) {
            onError(resultCode)
            return resultCode // error case
        }
        val responseCode: Int = mConn.getResponseCode()
        if (responseCode != HttpURLConnection.HTTP_OK) {
            onError(responseCode)
            return responseCode
        }
        mTask!!.mRspHeaders = mConn.getResponseHeaderFields()
        return if (getResponseStream(mTask.mWasInterceptInvoked)) {
            WebViewConstants.ERROR_CODE_SUCCESS
        } else WebViewConstants.ERROR_CODE_UNKNOWN
    }


    class WebViewDownloadConnection(private val url: String,private val mTask: DownloadTask) {
        val connectionImpl: URLConnection?
        private var responseStream: BufferedInputStream? = null

        init {
            connectionImpl = createConnection()
            initConnection(connectionImpl)
        }

        private fun createConnection(): URLConnection? {
            val currentUrl = url
            if (TextUtils.isEmpty(currentUrl)) {
                return null
            }
            var connection: URLConnection? = null
            try {
                var url = URL(currentUrl)
                var originHost: String? = null
                if (!TextUtils.isEmpty(mTask.mIpAddress)) {
                    originHost = url.host
                    url = URL(currentUrl.replace(originHost, mTask.mIpAddress?:""))

                    logError("${"create UrlConnection with DNS-Prefetch(" + originHost + " -> " + mTask.mIpAddress + ")."}")
                }
                connection = url.openConnection()
                if (connection != null) {
                    if (!TextUtils.isEmpty(originHost)) {
                        connection.setRequestProperty("Host", originHost)
                    }
                }
            } catch (e: Throwable) {
                if (connection != null) {
                    connection = null
                }
                logError("${"create UrlConnection fail, error:" + e.message + "."}")

            }
            return connection
        }

        private fun initConnection(connection: URLConnection?): Boolean {
            if (null != connection) {
                connection.connectTimeout = 5000
                connection.readTimeout = 15000
                connection.setRequestProperty("method", "GET")
                connection.setRequestProperty("Accept-Encoding", "gzip")
                connection.setRequestProperty("Accept-Language", "zh-CN,zh;")
                if (!TextUtils.isEmpty(mTask.mCookie)) {
                    connection.setRequestProperty(HTTP_HEAD_FIELD_COOKIE, mTask.mCookie)
                }
                return true
            }
            return false
        }

        @Synchronized
        fun connect(): Int {
            if (connectionImpl is HttpURLConnection) {
                return try {
                    connectionImpl.connect()
                    WebViewConstants.ERROR_CODE_SUCCESS
                } catch (e: IOException) {
                    WebViewConstants.ERROR_CODE_CONNECT_IOE
                }
            }
            return WebViewConstants.ERROR_CODE_UNKNOWN
        }

        fun disconnect() {
            if (connectionImpl is HttpURLConnection) {
                try {
                    connectionImpl.disconnect()
                } catch (e: Exception) {
                    logError("${"disconnect error:" + e.message}")
                }
            }
        }

        fun getResponseStreamFunc(): BufferedInputStream? {
            if (null == responseStream && null != connectionImpl) {
                try {
                    //请求没有获取完整的回复内容时，获取到的内容为长度为0
                    val inputStream = connectionImpl.getInputStream()
                    responseStream =
                        if ("gzip".equals(connectionImpl.contentEncoding, ignoreCase = true)) {
                            BufferedInputStream(GZIPInputStream(inputStream))
                        } else {
                            BufferedInputStream(inputStream)
                        }
                } catch (e: Throwable) {
                    logError("${"getResponseStream error:" + e.message + "."}")
                }
            }
            return responseStream
        }

        fun getResponseCode(): Int {
            return if (connectionImpl is HttpURLConnection) {
                try {
                    connectionImpl.responseCode
                } catch (e: IOException) {
                    val errMsg = e.message
                    logError("${"getResponseCode error:$errMsg"}")
                    WebViewConstants.ERROR_CODE_CONNECT_IOE
                }
            } else WebViewConstants.ERROR_CODE_UNKNOWN
        }

        fun getResponseHeaderFields(): Map<String, List<String>> {
            return connectionImpl?.headerFields?: HashMap()
        }
    }

    private fun readServerResponse(breakCondition: AtomicBoolean?): Boolean {
        //尝试从服务端获取资源文件
        val bufferedInputStream: BufferedInputStream? = mConn.getResponseStreamFunc()
        //如果当前网络请求还未返回的情况，就直接返回false。但是在getResponseStreamFunc方法的调用中一定会返回数据（即使没有数据，也会返回长度为0的数据流）
        if (null == bufferedInputStream) {
            logError("${"readServerResponse error: bufferedInputStream is null!"}")
            return false
        }
        try {
            val buffer =
                ByteArray(READ_BUFFER_SIZE)
            val total = mConn!!.connectionImpl!!.contentLength
            var n = 0
            var sum = 0
            //如果是结束阻断当前读取的情况/mTask.mWasInterceptInvoked设置true
            //那么就拦截处理当前的读取操作，那么n也不会等于-1，也不会标识当前已经从网络上读取数据完毕/返回 true
            //(breakCondition == null || !breakCondition.get()) &&
            while ( -1 != bufferedInputStream.read(
                    buffer
                ).also {
                    n = it
                }
            ) {
                mOutputStream!!.write(buffer, 0, n)
                sum += n
                if (total > 0) {
                    onProgress(sum, total)
                }
            }

            if((breakCondition == null || !breakCondition.get())){
                if (n == -1) {
                    mDownloadFinished = true
                    onSuccess(mOutputStream!!.toByteArray(), mConn.getResponseHeaderFields())
                }
            }else{
                //当前需要被截断，那么就要将下载完毕的数据流 close 执行复写中的内容，将数据结果存储下来（执行onClose）
                //手动调用一次下载成功用于保存到本地的方法
                onSuccess(mOutputStream!!.toByteArray(), mConn.getResponseHeaderFields())
            }

        } catch (e: java.lang.Exception) {
            logError("${"readServerResponse error:" + e.message + "."}")
            return false
        }
        return true
    }

    private val lock = java.lang.Object()

    @Synchronized
    private fun getResponseStream(breakConditions: AtomicBoolean): Boolean {
        return if (readServerResponse(breakConditions)) {
            //这里的mDownloadFinished依赖于readServerResponse的内部执行逻辑，如果资源没有被下载下来的情况，那么mDownloadFinished 则为false
            //没有下载的情况mOutputStream对象内容为空，netStream即使尝试获取服务端资源也为null
            val netStream: BufferedInputStream? =
                if (mDownloadFinished) null else mConn.getResponseStreamFunc()
            mTask.mInputStream = WebViewSessionStream(this, mOutputStream, netStream,mTask.mResourceUrl)
            //锁定后立即释放，略过该操作
            synchronized(lock) {lock.notify()}
            if (mDownloadFinished) {
                logError("${"sub resource compose a memory stream (" + mTask.mResourceUrl + ")."}")
            } else {
                logError("${"sub resource compose a bridge stream (" + mTask.mResourceUrl + ")."}")
            }
            true
        } else {
            false
        }
    }

    override fun onClose(readComplete: Boolean, outputStream: ByteArrayOutputStream) {
        logError("${"sub resource bridge stream on close(" + mTask.mResourceUrl + ")."}")
        if (!mDownloadFinished) {
            onSuccess(outputStream.toByteArray(), mConn.getResponseHeaderFields())
        }
    }


    private fun onStart() {
        for (callback in mTask.mCallbacks) {
            if (callback != null) {
                callback.onStart()
            }
        }
    }

    private fun onProgress(pro: Int, total: Int) {
        for (callback in mTask.mCallbacks) {
            if (callback != null) {
                callback.onProgress(pro, total)
            }
        }
    }

    private fun onSuccess(content: ByteArray, rspHeaders: Map<String, List<String>>) {
        for (callback in mTask.mCallbacks) {
            if (callback != null) {
                callback.onSuccess(content, rspHeaders)
            }
        }
        onFinish()
    }

    private fun onError(errCode: Int) {
        for (callback in mTask.mCallbacks) {
            if (callback != null) {
                callback.onError(errCode)
            }
        }
        onFinish()
    }

    private fun onFinish() {
        for (callback in mTask.mCallbacks) {
            if (callback != null) {
                callback.onFinish()
            }
        }
        mConn.disconnect()
    }


}