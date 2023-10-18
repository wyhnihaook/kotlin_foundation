package com.kotlin.practice.helper.webview

/**
 * 描述:网页资源下载相关内容
 * 功能介绍:下载相关配置内容
 * 创建者:翁益亨
 * 创建日期:2023/8/14 17:40
 */
class WebViewConfig {

    //完整url由下面三个数据构成 htmlAssetUrl + extraContent + htmlCompletion

    /**
     * 资源下载的路径 （针对同一个项目中嵌套其他路由信息展示-需要下载的资源内容都是不变的）由开发者提供避免问题
     */
    var htmlAssetUrl:String = ""

    /**
     * 可以从webView加载完毕的pageFinish回调进行获取最终的请求url进行设置
     * 一般是用于域名和路径信息中间的特殊字符存储。例如：https://www.baidu.com/#/home?a=b 的#/ （主要是由于请求https://www.baidu.com/会重新转向为https://www.baidu.com/#/）-转向中间耗时
     */
    var extraContent:String = ""

    /**
     * 剩下完整url补全内容
     */
    var htmlCompletion:String = ""

    /**
     * 本地资源路径，可以不进行配置，如果没有的本地资源路径的配置，统一都从网络上获取
     * 本地assets文件夹下创建
     * "web/index.html"//注意获取的相对路径前面不能添加 斜杆。例如：/web/index.html
     */
    var localHtmlAssetPath:String? = null

    /**
     * 正则表达式内容，用于获取html中的需要下载的资源相对地址
     * 示例：
     * "href=\"([^\"]*)\""
     * "src=\"([^\"]*)\""
     * 从html字符串中获取href/src中引用的相对路径，结合htmlAssetUrl域名拼接成绝对路径进行资源下载
     * 不管如何匹配，获取时：从第一个"后，最后一个"前截取出完整的相对资源地址信息
     */
    var matches = ArrayList<String>()



  companion object{
      /**
       * Http(s) connection time out , default 5s
       */
      var CONNECT_TIMEOUT_MILLIS = 5000

      /**
       * Http(s) read time out, default 15s
       */
      var READ_TIMEOUT_MILLIS = 15000

      /**
       * Buffer size when read data from network, default 10KB
       */
      var READ_BUF_SIZE = 1024 * 10

      /**
       * 同步最大下载个数
       */
      var maxDownloadCount:Int = 3

      /**
       * 缓存路径设置
       */
      var resourceCacheDir:String? = null

      /**
       * 缓存文件夹名称
       */
      var resourceCacheDirName:String? = null
  }
}