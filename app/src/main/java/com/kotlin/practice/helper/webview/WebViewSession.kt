package com.kotlin.practice.helper.webview

import android.content.Context
import android.content.res.AssetManager
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import com.kotlin.practice.db.AppDatabase
import com.kotlin.practice.db.web.Web
import com.kotlin.practice.helper.webview.WebViewConstants.Companion.WebCacheDataList
import com.kotlin.practice.helper.webview.download.WebViewDownloadCache
import com.kotlin.practice.helper.webview.download.WebViewDownloadEngine
import com.kotlin.practice.util.logError
import com.kotlin.practice.util.mainThread
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean


/**
 * 描述:核心处理缓存逻辑类
 * 功能介绍:衔接WebView和内部功能的类
 * 初始化要打开的url，通过本地进行url的解析，获取其中需要下载的资源文件
 * 1.所有的加载模式都要依照最新html网络请求返回的结果信息。避免缓存资源缺失去网络加载这个时候又更新过了资源信息造成的白屏问题
 * 2.携带eTag信息去校验时效性，如果是304就直接加载本地文件/这个时候资源缓存是否存在不重要，即使本地缺失，网络上下载也是可以
 * 3.如果返回200就直接重新加载并缓存最新的html信息，直接从网络同步
 * 4.如果返回异常就不拦截资源文件直接通过html网络请求来同步。实际步骤同步骤3
 *
 * 创建者:翁益亨
 * 创建日期:2023/8/15 10:24
 */
class WebViewSession(context: Context, private val config: WebViewConfig) :
    Handler.Callback {

    @Volatile
    private var resourceDownloaderEngine: WebViewDownloadEngine? = null

    //标识当前页面已经关闭
    private val wasOnPageFinishInvoked = AtomicBoolean(false)

    //默认从本地获取资源，包括项目中asset文件以及内存缓存的数据
    //本地数据库存储的是html域名+路径 不携带参数 对应存储的内容就是需要预加载的链接信息
    private var loadLocalAssetHtml = false

    //尝试获取缓存信息
    private var webCache: Web? = null

    //本地同步的html资源信息
    var htmlString:String? = null

    init {
        resourceDownloaderEngine =
            WebViewDownloadEngine(WebViewDownloadCache.WebViewResourceCache())

        //获取本地缓存数据信息
        loop@ for (web in WebCacheDataList) {
            if (config.htmlAssetUrl.startsWith(web.url)) {
                //包含情况
                webCache = web

                //存在网络缓存的情况，不需要检查缓存是否完全下载到本地显示，如果没有显示完成的情况，让WebView自己去加载内容
                break@loop
            }
        }
    }


    //判断当前WebView是否被回收了，用于缓存数据的对象是否需要返回，因为是数据流的形式，所以需要在结束close。WebView内部加载完毕后会调用
    fun isDestroyedOrWaitingForDestroy(): Boolean {
        return wasOnPageFinishInvoked.get()
    }

    override fun handleMessage(msg: Message): Boolean {
        TODO("Not yet implemented")
    }


    //2.页面加载完毕
    //！！！！存在多次调用的情况！！！！
    fun pageFinish(url: String) {

    }

    //3.获取本地是否存在资源，由WebView的shouldInterceptRequest调用发起业务处理
    //开始获取本地资源，需要拦截本地数据的获取（请求）
    fun requestResource(url: String): WebResourceResponse? {
        //获取资源本地缓存地址
        return if (loadLocalAssetHtml) null else resourceDownloaderEngine!!.onRequestSubResource(
            url,
            this
        ) as WebResourceResponse?
    }


    //4.页面销毁的时候，处理的事务
    fun destroy() {
        wasOnPageFinishInvoked.set(true)
    }

    //5.绑定当前的WebView，将加载Url逻辑迁移到当前内容
    //首先校验html信息.发起get请求获取html信息。《资源文件通过WebView拦截回调获取全路径去缓存》
    fun bindWebViewAndLoad(webView: WebView) {

        var eTag = ""
        //从数据库中同步缓存信息，获取请求头需要携带的内容
        if (webCache != null) {
            eTag = webCache!!.eTag
        }

        if (TextUtils.isEmpty(eTag)) {
            //尝试从本地assets内容中获取，扩展名字，可以使用抽象方法实现的方式处理
          var readDataString = WebViewUtils.getAssetString(webView.context,"web/eTag.txt")
          if(readDataString!=null){
              eTag = readDataString
          }
        }

        WebViewSessionThreadPool.postTask(Runnable {
            var webViewHtmlServer = WebViewHtmlServer(
                config.htmlAssetUrl,
                eTag
            )
            var resultCode = webViewHtmlServer.connect()

            //连接情况：仅仅作为是否成功建立网络连接
            if (resultCode == WebViewConstants.ERROR_CODE_SUCCESS) {
                //连接内容成功才需要获取最新的html信息并做后续操作
                logError("网络请求code:" + webViewHtmlServer.responseCode)
                logError("网络请求eTag:" + webViewHtmlServer.eTagStorage)

                if (HttpURLConnection.HTTP_OK == webViewHtmlServer.responseCode) {
                    //html信息更新，需要本地缓存并且作为最新展示页面添加
                    //保存当前html文件到缓存中，保存eTag信息以及html内容到对应的数据库中
                    var db = AppDatabase.getInstance()

                    eTag = webViewHtmlServer.eTagStorage?:""

                    if (webCache != null) {
                        //更新
                        //如果是更新就要从列表中先获取web对象，更新的id要对应
                        //html内容和需要下载的链接都要变化
                        webCache!!.htmlContent = webViewHtmlServer.serverRsp!!
                        webCache!!.eTag = eTag

                        db.webDao().update(webCache!!)
                    } else {
                        //新插入
                        webCache = Web(
                            webViewHtmlServer.requestHostPath ?: "",
                            webViewHtmlServer.serverRsp!!,
                            eTag
                        )

                        db.webDao().insertAll(webCache!!)
                    }

                    mainThread {
                        htmlString = webViewHtmlServer.serverRsp!!

                        webView.loadDataWithBaseURL(
                            config.htmlAssetUrl,
                            webViewHtmlServer.serverRsp!!, "text/html; charset=utf-8", "UTF-8", null
                        )
                    }
                } else if (HttpURLConnection.HTTP_NOT_MODIFIED == webViewHtmlServer.responseCode) {
                    //html没有任何修改，不做任何处理，所有的信息都尝试从本地缓存中优先获取，没有去查看assets目录是否存在
                    //保存的逻辑是优先保存html信息，保存成功了之后才保存eTag信息，避免html保存时报错，依然通过缓存路径查找
                    if (webCache != null) {
                        mainThread {
                            htmlString = webCache!!.htmlContent

                            webView.loadDataWithBaseURL(
                                config.htmlAssetUrl,
                                webCache!!.htmlContent, "text/html; charset=utf-8", "UTF-8", null
                            )
                        }

                    } else {
                        if(config.localHtmlAssetPath!=null){
                            //本地assets文件检索，如果存在就加载，
                            loadLocalAssetHtml = true
                            htmlString = WebViewUtils.getAssetString(webView.context,config.localHtmlAssetPath!!)

                            loadUrl(webView,"file:///android_asset/${config.localHtmlAssetPath}")
                        }else{
                            //兜底
                            loadUrl(webView,config.htmlAssetUrl)
                        }

                    }

                } else {
                    //网络连接返回了其他情况。兜底异常，通过WebView调用正常加载html方法
                    loadUrl(webView,config.htmlAssetUrl)
                }
            } else {
                //连接建立异常处理，通过WebView调用正常加载html方法
                loadUrl(webView,config.htmlAssetUrl)
            }

        })
    }


    //资源下载url地址添加
    fun addLoadCacheAssetUrl(vararg preloadLinks: String) {
        resourceDownloaderEngine!!.addSubResourcePreloadTask(preloadLinks.asList())
    }

    //加载网络资源
    private fun loadUrl(webView: WebView,url:String){
        mainThread {
            webView.loadUrl(url)
        }
    }



}
