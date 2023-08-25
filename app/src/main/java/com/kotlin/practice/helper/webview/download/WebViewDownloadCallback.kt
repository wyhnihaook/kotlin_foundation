package com.kotlin.practice.helper.webview.download

/**
 * 描述:网页并行下载html资源信息回调
 * 功能介绍:下载回调
 * 创建者:翁益亨
 * 创建日期:2023/8/14 15:19
 */
interface WebViewDownloadCallback {
    /**
     * notify the download start.
     */
    fun onStart()

    /**
     * notify the download progress.
     *
     * @param pro downloaded size
     * @param total total size
     */
    fun onProgress(pro: Int, total: Int)

    /**
     * notify download success.
     *
     * @param content downloaded content bytes
     * @param rspHeaders http response headers
     */
    fun onSuccess(content: ByteArray, rspHeaders: Map<String, List<String>>)

    /**
     * notify download failed.
     *
     * @param errorCode error code
     */
    fun onError(errorCode: Int)

    /**
     * notify download finish. `onSuccess` or `onError`
     */
    fun onFinish()

    /**
     * an empty implementation of [SonicDownloadCallback]
     */
    open class SimpleDownloadCallback : WebViewDownloadCallback {
        override fun onStart() {}
        override fun onProgress(pro: Int, total: Int) {}
        override fun onSuccess(content: ByteArray, rspHeaders: Map<String, List<String>>) {}
        override fun onError(errorCode: Int) {}
        override fun onFinish() {}
    }
}