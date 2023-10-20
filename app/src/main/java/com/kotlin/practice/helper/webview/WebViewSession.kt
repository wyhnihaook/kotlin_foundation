package com.kotlin.practice.helper.webview

import android.content.Context
import android.content.res.AssetManager
import android.os.Handler
import android.os.Message
import android.webkit.WebResourceResponse
import android.webkit.WebView
import com.kotlin.practice.db.AppDatabase
import com.kotlin.practice.db.web.Web
import com.kotlin.practice.helper.webview.WebViewConstants.Companion.WebCacheDataList
import com.kotlin.practice.helper.webview.download.WebViewDownloadCache
import com.kotlin.practice.helper.webview.download.WebViewDownloadEngine
import com.kotlin.practice.util.logError
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.concurrent.atomic.AtomicBoolean


/**
 * 描述:核心处理缓存逻辑类
 * 功能介绍:衔接WebView和内部功能的类
 * 初始化要打开的url，通过本地进行url的解析，获取其中需要下载的资源文件
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
    private var loadLocalAssetHtml = true

    //当前加载的文本的内容
    private var htmlString: String? = null

    //尝试获取缓存信息
    private var webCache: Web? = null

    init {
        resourceDownloaderEngine =
            WebViewDownloadEngine(WebViewDownloadCache.WebViewResourceCache())

        //获取本地缓存数据信息
        loop@ for (web in WebCacheDataList) {
            if (web.url.startsWith(config.htmlAssetUrl)) {
                //包含情况
                webCache = web

                //存在网络缓存的情况，不需要检查缓存是否完全下载到本地显示，如果没有显示完成的情况，让WebView自己去加载内容
                break@loop
            }
        }

        //读取对应显示的html内容，进行当前请求的html比较（主要比较资源地址是否一致，如果不一致，就需要重新下载）
        if (webCache != null) {
            htmlString = webCache!!.htmlContent
        }

        if (config.localHtmlAssetPath == null) {
            //不管存在不存在本地资源的情况，没有网络缓存文件，一定不加载本地数据
            loadLocalAssetHtml = false
        } else {
            //存在本地缓存信息，理论上一定和asset文件中不一致，才会进行缓存
            //没有缓存，有本地文件的情况就加载本地数据
            if (htmlString != null) {
                loadLocalAssetHtml = false
            } else {
                //设置html为当前本地的信息
                val am: AssetManager = context.resources.assets
                var input: InputStream? = null
                try {
                    input = am.open(config.localHtmlAssetPath!!)
                    val len = input.available()
                    val buffer = ByteArray(len)
                    input.read(buffer)
                    input.close()
                    htmlString = buffer.decodeToString()

                    logError("htmlString:$htmlString")

                } catch (e: IOException) {
                    logError("e:$e")
                    htmlString = null
                    //读取失败，依然采用网络请求方式获取
                    loadLocalAssetHtml = false
                }
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


    //在进程初始化的时候就读取数据库存储的web信息，后续就只是维护本地数据即可（必须点进去才会出发二次请求更新内容）
    //当然在每次初始化的时候需要监测一次本地是否还存在该资源，如果不存在的情况就默认
    //PS：如果内部存在的情况则会尝试 获取 ，如果获取不到那么也会让WebView自己去网络上获取缓存数据。所以不必时时刻刻获取最新的内容，初始化获取一次就行

    //"https://h5-test.dby.cn/product-comparison/#/home?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50Q29kZSI6IjM3MjcwNjU2MjU4Nzg4OTY2NCIsImxvZ2luQ2hhbm5lbCI6IjEiLCJ0b2tlblZlcnNpb24iOiJqd3QtdjEiLCJyYW5kb21VdUlkIjoiY2JiM2ZkMTAyOTk4NGJlNDlkOGQ4NDk4OWY3NjcxZjAifQ.hmN7Lhr-vUWCtJnQmQGZ7bf4-39by_2dj5Svy1Ubj4A&empId=81270"


    //提供回调处理方式，这里准备的缓存是用于下一次进入查看的（这里处理的是预加载的信息）
    //1.根据url去准备缓存对应的信息
    //不管页面如何，启动异步下载直到下载完成回调即可（目标是要把对应页面做本地缓存信息）
    fun readyCache() {
        //1.1加载html字符串，整理其中的需要下载的资源文件内容。其中涉及内容标签为link/script中的src
        //1.2根据当前url获取对应的域名信息（host）进行拼接到src，作为下载资源的绝对路径  -- 参考VasSonic SonicSession中handleFlow_Connection初始化进行html同步
        //注意：耗时操作放到子线程处理

        //从数据库中获取eTag标识
        var eTag = ""
        if (webCache != null) {
            eTag = webCache!!.eTag
        }

        WebViewSessionThreadPool.postTask(Runnable {
            //匹配下载内容一：<link href=""   匹配下载内容二：<script ... src=" "
            var webViewHtmlServer = WebViewHtmlServer(
                config.htmlAssetUrl + config.extraContent + config.htmlCompletion,
                config.matches, eTag
            )
            webViewHtmlServer.connect()

            //连接内容成功才需要获取最新的html信息并做后续操作
            logError("网络请求code:" + webViewHtmlServer.responseCode)

            if (HttpURLConnection.HTTP_OK == webViewHtmlServer.responseCode) {
                //这里需要判断是否和本地资源内容一致，如果一致就不做处理，如果不一致就需要本地数据库存储最新的内容，并且去下载资源文件
                //1.先尝试读取数据库中存储的路径信息对应的html内容（应用启动时，就获取的静态内容），如果获取不到再去读取本地缓存的信息 （扩展实现方法，提供本地读取实现）
                //2.获取到对应的html信息和本地信息比较，如果一致就不做其他操作，如果不一致就使用当前的html进行资源下载

                //是否匹配，这里先固定不匹配，进行后续操作
                var isMatch = htmlString == webViewHtmlServer.serverRsp
                //数据存储对应的eTag信息
                var eTag = webViewHtmlServer.eTagStorage

                if (isMatch) {
                    //html中匹配内容完全一致，这里就不做处理
                    logError("完全一致，不需要缓存，即使网络缓存内容缺失，会再次尝试下载到本地缓存内容")
                } else {
                    logError("内容不一致，需要缓存资源文件用于下一次展示")
                    //解析同步的url，获取对应的标识，进行缓存下载（下次生效），并且在资源文件匹配不到的情况（可能重定向，需要一并下载），也进行下载
                    //最多分为多个部分下载
                    //针对app中的url规则，域名会存在/#/分割标识，作为下载的链接
                    var preloadLinks: ArrayList<String> = ArrayList()

                    //手动判断类型
                    var matches = ArrayList<String>()
                    //是否时普通规则匹配
                    var normalMatch =
                        webViewHtmlServer.serverRsp!!.contains("href=\"") || webViewHtmlServer.serverRsp!!.contains(
                            "src=\""
                        )

                    if (config.matches.isEmpty()) {

                        if (normalMatch) {
                            //匹配规则，由外部传递
                            matches.add("href=\"([^\"]*)\"")
                            matches.add("src=\"([^\"]*)\"")
                        } else {
                            //当不存在""将资源文件的相对路径包裹时，采用以下方案
                            matches.add("href=[^\">\\s]*")
                            matches.add("src=[^\">\\s]*")
                        }

                    }else{
                        matches = config.matches
                    }


                    //1.网络获取的html信息存储
                    //2.构建当前需要预加载的资源文件
                    for (match: String in matches) {
                        val regex = Regex(match)

                        regex.findAll(webViewHtmlServer.serverRsp!!.trim()).map {
                            logError("value:" + it.value)
                            val content = it.value.substring(
                                it.value.indexOf(if (normalMatch) "\"" else "=") + 1,
                                if (normalMatch) it.value.lastIndexOf("\"") else it.value.length
                            )
                            if (content.endsWith(".js") || content.endsWith(".css")) {
                                preloadLinks.add(config.htmlAssetUrl + content)
                            }
                            it.value
                        }.count()
                    }

                    if (preloadLinks.isNotEmpty()) {

                        //数据库存储对应信息
                        //首先判断静态数据是否存在已经存储的url信息，如果已经存在，就更新。不存在，就添加
                        var db = AppDatabase.getInstance()

                        if (webCache != null) {
                            //更新
                            //如果是更新就要从列表中先获取web对象，更新的id要对应
                            //html内容和需要下载的链接都要变化
                            webCache!!.htmlContent = webViewHtmlServer.serverRsp!!
                            webCache!!.linkList = preloadLinks
                            webCache!!.eTag = eTag ?: ""

                            db.webDao().update(webCache!!)
                        } else {
                            //新插入
                            webCache = Web(
                                config.htmlAssetUrl,
                                webViewHtmlServer.serverRsp!!,
                                config.extraContent,
                                preloadLinks,
                                eTag ?: ""
                            )

                            db.webDao().insertAll(webCache!!)
                        }

                        //更新完毕之后，刷新本地的静态数据
                        val webCacheList = db.webDao().getAll()
                        //获取本地数据之后进行静态存储
                        WebCacheDataList.clear()
                        WebCacheDataList.addAll(webCacheList)

                        //只有需要存在下载链接的情况，才进行下载
                        resourceDownloaderEngine!!.addSubResourcePreloadTask(preloadLinks)
                    }
                }
            }
        })
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
    fun bindWebViewAndLoad(webView: WebView) {
        if (loadLocalAssetHtml) {
            webView.loadUrl("file:///android_asset/${config.localHtmlAssetPath}" + config.extraContent)
        } else {
            if (webCache != null) {
                //存在网络缓存，直接通过缓存静态数据进行展示 存在 /#/ 的时候需要携带，不然页面加载会再
                //htmlContent中的资源地址要根据baseUrl获取完整网络资源地址
                //存在缓存才需要进行添加，因为本地内容无需通过链接检查是否下载，只有缓存内容才需要（可在内存中被删除）
                resourceDownloaderEngine!!.addSubResourcePreloadTask(webCache!!.linkList)

                webView.loadDataWithBaseURL(
                    webCache!!.url + webCache!!.extraContent,
                    webCache!!.htmlContent, "text/html; charset=utf-8", "UTF-8", null
                )
            } else {
                webView.loadUrl(config.htmlAssetUrl + config.extraContent + config.htmlCompletion)
            }
        }
    }

}
