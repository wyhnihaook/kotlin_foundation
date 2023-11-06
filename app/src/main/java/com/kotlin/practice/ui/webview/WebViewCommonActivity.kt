package com.kotlin.practice.ui.webview

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.graphics.Bitmap
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.kotlin.practice.R
import com.kotlin.practice.databinding.ActivityWebviewCommonBinding
import com.kotlin.practice.helper.webview.WebViewConfig
import com.kotlin.practice.helper.webview.WebViewSession
import com.kotlin.practice.util.logError
import java.io.ByteArrayInputStream


/**
 * 描述:公用的网页
 * 功能介绍:公用的网页
 * 创建者:翁益亨
 * 创建日期:2023/8/15 11:01
 */
class WebViewCommonActivity : AppCompatActivity(R.layout.activity_webview_common) {

    private val mBinding by viewBinding(ActivityWebviewCommonBinding::bind)

    //系统自带代理方式获取
    private val mViewModel by viewModels<WebViewCommonViewModel>()

    //webview初始化
    private var wvContent: WebView? = null

    private var session: WebViewSession? = null

    //测试环境慢
    private val loadUrl:String = "https://h5-test.dby.cn/product-comparison/#/home?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50Q29kZSI6IjM3MjcwNjU2MjU4Nzg4OTY2NCIsImxvZ2luQ2hhbm5lbCI6IjEiLCJ0b2tlblZlcnNpb24iOiJqd3QtdjEiLCJyYW5kb21VdUlkIjoiY2JiM2ZkMTAyOTk4NGJlNDlkOGQ4NDk4OWY3NjcxZjAifQ.hmN7Lhr-vUWCtJnQmQGZ7bf4-39by_2dj5Svy1Ubj4A&empId=81270"

//    private val url: String =
//        "https://h5-dev.dby.cn/product-comparison/#/home?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50Q29kZSI6IjM3MjcwNjU2MjU4Nzg4OTY2NCIsImxvZ2luQ2hhbm5lbCI6IjEiLCJ0b2tlblZlcnNpb24iOiJqd3QtdjEiLCJyYW5kb21VdUlkIjoiY2JiM2ZkMTAyOTk4NGJlNDlkOGQ4NDk4OWY3NjcxZjAifQ.hmN7Lhr-vUWCtJnQmQGZ7bf4-39by_2dj5Svy1Ubj4A&empId=81270"

//    private val url:String = "http://47.111.119.136:7799/"

    //正式环境：快点的
//    private val url:String = "https://h5.dby.cn/product-comparison/#/home?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50Q29kZSI6IjM3NTI0MzIzOTI2OTIwODA2NCIsImxvZ2luQ2hhbm5lbCI6IjEiLCJ0b2tlblZlcnNpb24iOiJqd3QtdjEiLCJyYW5kb21VdUlkIjoiYWJkNjIzMmYyOWQ4NDA3NDlhY2ZkZmU1NjQwNzcyNDUifQ.vcs3v4uYYxscpXmHFhcKk40gwCf2CPKJGwmevAXpqPY&empId=1593"

    //保单认领
//    private val url:String = "https://admin.91duobaoyu.com/insurance-policy-claim/#/policyClaim?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhY2NvdW50Q29kZSI6IjM3MjcwNjU2MjU4Nzg4OTY2NCIsImxvZ2luQ2hhbm5lbCI6IjEiLCJ0b2tlblZlcnNpb24iOiJqd3QtdjEiLCJyYW5kb21VdUlkIjoiZTNjNjE1MDIwMTQwNDkzOGEwYjEyMThhNWQ5MDFiZjEifQ.1VFJE-rad_cKaX9kbB6cL9yjT3DEtKO_Aw0otVjF4CU&empId=81270"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //初始化webView
        val startTime = System.currentTimeMillis()
        logError("初始化的时间：${startTime}")


        //监听WebView初始化时间差，在手机上第一次273ms，下一次就是8ms

        //官方默认写法
        //ActivityTrainRecordsBinding.inflate(layoutInflater) 获取binding实例。通过setContentView(binding.root)设置布局信息

        mBinding.includeTitle.text = mViewModel.title
        mBinding.includeTitle.onClick = View.OnClickListener {
            mViewModel.back(it)
        }

        //需要一个类去管理对应的状态WebViewSession

        //vassnoic中sonicSession的runSonicFlow方法中初始化获取html信息并获取对应html中的需要异步下载的资源文件信息
        //handleFlow_Connection建立连接获取需要缓存的数据


        var config = WebViewConfig()
        //加载优先级由内部定义
        config.htmlAssetUrl = loadUrl
        config.localHtmlAssetPath = "web/index.html"//注意获取的相对路径前面不能添加 斜杆。例如：/web/index.html

        session = WebViewSession(this@WebViewCommonActivity, config)
//        session!!.readyCache()

        //初始化绑定下载资源引擎和webView，只要监测到webView == null之后，页面就是被回收阶段
        //所有的资源都是要通过下载到本地缓存的方式存储，这里只需要在destroy中移除任务即可，其他情况就需要下载资源，下载完毕之后通知当前页面。能正常展示了
        //首先通过url进行解析，查看需要下载的资源文件

        resetWebView()
        //800ms
        //通过readyCache缓存数据 500ms -- 采用该实现方案
//        wvContent!!.loadUrl(loadUrl)
        //判断session是否存在，采用loadUrl或者bindWebView
        session!!.bindWebViewAndLoad(wvContent!!)


        //加载本地缓存的html信息(不包含本地资源文件)，目前没有实质性的时间差距，都需要1s加载（存在缓存js的内容）
//        wvContent!!.loadUrl("file:///android_asset/web/product.html")

        //包含本地文件 600ms
//        wvContent!!.loadUrl("file:///android_asset/web/index.html#/")

        //以下加载的内容最为快速，时间从800ms->400ms
        //从本地asset加载文件速度400ms
        //尝试从本地内存读取观察时间差距

        //记载本地缓存的html文件内容，可以通过缓存当前的html的文本信息，达到第二次进入页面，秒开页面的功能
        //存在缓存页面的时候执行下面的方法
//        val htmlString = "<!DOCTYPE html>\n" +
//                "<html lang=\"\">\n" +
//                "  <head>\n" +
//                "    <meta charset=\"utf-8\">\n" +
//                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
//                "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1.0, user-scalable=0, viewport-fit=cover\">\n" +
//                "    <link rel=\"icon\" href=\"favicon.ico\">\n" +
//                "    <title>产品对比</title>\n" +
//                "  <link href=\"static/css/chunk-vantUI.b249ad9e.css\" rel=\"stylesheet\"><link href=\"static/css/chunk-libs.95007e02.css\" rel=\"stylesheet\"><link href=\"static/css/app.670d3a31.css\" rel=\"stylesheet\"></head>\n" +
//                "  <body>\n" +
//                "    <noscript>\n" +
//                "      <strong>We're sorry but product_comparsion doesn't work properly without JavaScript enabled. Please enable it to continue.</strong>\n" +
//                "    </noscript>\n" +
//                "    <div id=\"app\"></div>\n" +
//                "    <!-- built files will be auto injected -->\n" +
//                "    <!--  -->\n" +
//                "  <script type=\"text/javascript\" src=\"static/js/runtime.js\"></script><script type=\"text/javascript\" src=\"static/js/chunk-vantUI.dbe093871856ee30ac83.js\"></script><script type=\"text/javascript\" src=\"static/js/chunk-libs.dbe093871856ee30ac83.js\"></script><script type=\"text/javascript\" src=\"static/js/app.dbe093871856ee30ac83.js\"></script></body>\n" +
//                "</html>\n"
//
////        baseUrl指定htmlString中的资源的相对路径，使用url的host
////        如果是本地资源就使用本地资源地址file:///android_asset/web/
////
////        file:///android_asset/web/ 400ms -需要判断多个环境，当前暂不实现本地和网络缓存数据的匹配
//        wvContent!!.loadDataWithBaseURL("file:///android_asset/web/",htmlString,"text/html; charset=utf-8","UTF-8",null)


        //直接从网络获取本地需要渲染的资源内容
//        val htmlProductString = "<!DOCTYPE html>\n" +
//                "<html lang=\"\">\n" +
//                "  <head>\n" +
//                "    <meta charset=\"utf-8\">\n" +
//                "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
//                "    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0,maximum-scale=1.0, user-scalable=0, viewport-fit=cover\">\n" +
//                "    <link rel=\"icon\" href=\"favicon.ico\">\n" +
//                "    <title>产品对比</title>\n" +
//                "  <link href=\"static/css/chunk-vantUI.bc065fe4.css\" rel=\"stylesheet\"><link href=\"static/css/chunk-libs.9e92a7b7.css\" rel=\"stylesheet\"><link href=\"static/css/app.1e31d7fe.css\" rel=\"stylesheet\"></head>\n" +
//                "  <body>\n" +
//                "    <noscript>\n" +
//                "      <strong>We're sorry but product_comparsion doesn't work properly without JavaScript enabled. Please enable it to continue.</strong>\n" +
//                "    </noscript>\n" +
//                "    <div id=\"app\"></div>\n" +
//                "    <!-- built files will be auto injected -->\n" +
//                "    <!--  -->\n" +
//                "  <script type=\"text/javascript\" src=\"static/js/runtime.js\"></script><script type=\"text/javascript\" src=\"static/js/chunk-vantUI.8e5989fdb8e5d8badd39.js\"></script><script type=\"text/javascript\" src=\"static/js/chunk-libs.8e5989fdb8e5d8badd39.js\"></script><script type=\"text/javascript\" src=\"static/js/app.8e5989fdb8e5d8badd39.js\"></script></body>\n" +
//                "</html>\n"
//
//        //网络资源文件下载
//        //未缓存js、css等信息的时候 800ms 等同于 直接 loadUrl
//        //当存在资源文件的时候 600ms 等同于读取本地文件获取本地的资源渲染
//        wvContent!!.loadDataWithBaseURL("https://h5.dby.cn/product-comparison/#/",htmlProductString,"text/html; charset=utf-8","UTF-8",null)

        val endTime = System.currentTimeMillis()

        logError("结束花费时间：${endTime - startTime}")


    }


    override fun onDestroy() {
        super.onDestroy()
        if (null != session) {
            session!!.destroy()
            session = null
        }
        //为了避免内存泄露,这里都会被回收当前网页的信息,但是下次初始化后,所有内部初始化的内容都是未准备好的,需要花费将近1s进行初始化
        //所以每次销毁一个网页内容后,就要新建一个webview进行全局缓存,每次打开网页就从缓存池中获取一个,避免内部初始化过长
        if (wvContent != null) {
            mBinding.root.removeView(wvContent)
            wvContent!!.removeAllViews()
            wvContent!!.destroy()
            wvContent = null
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun resetWebView() {
        if (wvContent != null) {
            mBinding.root.removeView(wvContent)
            wvContent!!.destroy()
        }
        wvContent = WebView(this)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        //注意，这里不能使用ConstraintLayout做根容器，LayoutParams中添加WRAP_CONTENT，那么h5里的100%就会失效
//        layoutParams.leftToLeft = mBinding.root.id
//        layoutParams.rightToRight = mBinding.root.id
//        layoutParams.bottomToBottom = mBinding.root.id
//        layoutParams.topToBottom = mBinding.includeTitle.titleContainer.id

        wvContent!!.layoutParams = layoutParams

        mBinding.root.addView(wvContent)
//        WebView.setWebContentsDebuggingEnabled(true)
        val webSettings = wvContent!!.settings

        // add java script interface
        // note:if api level lower than 17(android 4.2), addJavascriptInterface has security
        // issue, please use x5 or see https://developer.android.com/reference/android/webkit/
        // WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)

        // add java script interface
        // note:if api level lower than 17(android 4.2), addJavascriptInterface has security
        // issue, please use x5 or see https://developer.android.com/reference/android/webkit/
        // WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)
        webSettings.javaScriptEnabled = true

        // init webview settings

        webSettings.useWideViewPort = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.setAppCacheEnabled(false)
        webSettings.domStorageEnabled = true
        webSettings.allowContentAccess = true // 是否可访问Content Provider的资源，默认值 true
        webSettings.allowFileAccess = true // 是否可访问本地文件，默认值 true
//        webSettings.allowFileAccessFromFileURLs = false // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
//        webSettings.allowUniversalAccessFromFileURLs = false // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        wvContent!!.addJavascriptInterface(createJSInterface(), "NativeCaller")

        wvContent!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                logError("当前加载进度：$newProgress")
            }
        }

        wvContent!!.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                logError("页面开始加载")
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                //在所有网络资源加载完成后调用的/渲染完成显示在界面上
                logError("页面加载完成：$url");
                if (session != null) {
                    session!!.pageFinish(url)
                }
            }


            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                //仅仅针对于当前加载html完成，link/script文档加载过程中，不代表页面渲染出来，需要依赖CPU分配到onPageFinished回调让页面渲染出来
                logError("页面显示完成：$url")
            }


            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                logError("url reloading:"+request!!.url.toString())
                return super.shouldOverrideUrlLoading(view, request)
            }

            @TargetApi(21)
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return shouldInterceptRequest(view, request.url.toString())
            }

            override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
                //同步阻塞内容，通过当前输出线程名称发现执行WebView资源加载的请求是TaskSchedulerFo。不是主线程的main
                //所以这边可以自行发起请求阻塞等待结果内容返回并保存操作
//                logError("Thread.currentThread().name:"+Thread.currentThread().name)
                logError("----开始下载需要下载的资源文件：$url")

                return if (session != null) {
                    if(url == loadUrl){
                        //从session的返回中进行匹配对应的html资源（永远保持最新）
                        //这里一定存在最新的资源信息
                        if(session!!.htmlString != null){
                            return  WebResourceResponse(
                                "text/html",
                                "utf-8",
                                session!!.htmlString!!.byteInputStream()
                            )
                        }
                        return null
                    }

                    //测试环境中，本地文件读取->https://h5-test.dby.cn/product-comparison/static/js/chunk-libs.0134cd5df34111674845.js) 过大 10MB
                    //存在session的情况，优先将获取资源的任务添加到列表中
                    session!!.addLoadCacheAssetUrl(url)
                    //从队列中尝试获取资源结果
                    val response: WebResourceResponse? = session!!.requestResource(url)
                    logError("-----缓存资源同步设置:$response")
                    response
                } else null
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler,
                error: SslError?
            ) {
                logError("ssl继续")
                handler.proceed()
            }
        }

    }

    private fun createJSInterface(): CommonJSInterface {
        return CommonJSInterface(this, wvContent!!)
    }
}