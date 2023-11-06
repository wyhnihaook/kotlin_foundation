package com.kotlin.practice.helper.webview

/**
 * 描述:网页资源下载相关内容
 * 功能介绍:下载相关配置内容
 * 创建者:翁益亨
 * 创建日期:2023/8/14 17:40
 */
class WebViewConfig {

    /**
     * 需要加载的html信息内容（完整html路径）
     */
    var htmlAssetUrl:String = ""


    /**
     * 本地资源路径，可以不进行配置，如果没有的本地资源路径的配置，统一都从网络上获取
     * 本地assets文件夹下创建
     * "web/index.html"//注意获取的相对路径前面不能添加 斜杆。例如：/web/index.html
     */
    var localHtmlAssetPath:String? = null



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