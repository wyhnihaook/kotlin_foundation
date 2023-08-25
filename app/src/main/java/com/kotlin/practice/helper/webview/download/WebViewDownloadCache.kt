package com.kotlin.practice.helper.webview.download

import android.text.TextUtils
import com.kotlin.practice.helper.webview.WebViewHelper
import com.kotlin.practice.helper.webview.WebViewUtils
import java.io.File

/**
 * 描述:下载到本地缓存的信息
 * 功能介绍:同一管理当前缓存的类
 * 创建者:翁益亨
 * 创建日期:2023/8/14 15:21
 */
open abstract class WebViewDownloadCache {
    /**
     * get the cached content according to the url
     *
     * @param url the download url
     * @return bytes of cached content of the url
     */
    abstract fun getResourceCache(url: String): ByteArray?

    /**
     * get the cached response headers according to the url
     *
     * @param url the download url
     * @return cached headers of the url
     */
    abstract fun getResourceCacheHeader(url: String): Map<String, List<String>>

    /**
     *
     * @return Sub resource cache
     */
    fun getSubResourceCache(): WebViewDownloadCache? {
        return WebViewResourceCache()
    }

    /**
     * An sub resource cache implementation [WebViewDownloadCache]
     */
    class WebViewResourceCache : WebViewDownloadCache() {
        override fun getResourceCache(resourceUrl: String): ByteArray? {
            //原有完整逻辑
            //1.检查资源文件是否存在/不存在文件->从网络中加载 return null
            //2.检查当前资源文件是否是超过最大保存时间/超过后->从网络中加载 return null
            //3.校验本地文件的正确性/不正确->从网络中加载 return null
            //4.检索处本地的文件，返回提供WebView的loadUrl中的资源信息

            //----修改后的逻辑为检索当前存在就直接加载----
            //资源文件的下载 -> CSS/JS等

            if (TextUtils.isEmpty(resourceUrl)) {
                return null
            }

            //这里尝试性去获取对应的本地资源文件，当然不存在的时候还是返回null
            val resourceId: String = WebViewUtils.getMD5(resourceUrl)?:""


            var resourceBytes: ByteArray?
            val resourcePath: String = WebViewHelper.getSonicResourcePath(resourceId)
            val resourceFile = File(resourcePath)
            resourceBytes = WebViewUtils.readFileToBytes(resourceFile)


            return resourceBytes
        }

        override fun getResourceCacheHeader(resourceUrl: String): Map<String, List<String>> {
            val resourceName: String = WebViewUtils.getMD5(resourceUrl)?:""
            val headerPath: String = WebViewHelper.getSonicResourceHeaderPath(resourceName)
            return WebViewHelper.getHeaderFromLocalCache(headerPath)
        }

        companion object {

        }
    }
}