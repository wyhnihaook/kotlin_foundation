package com.kotlin.practice.helper.webview

import android.text.TextUtils
import com.kotlin.practice.util.logError
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.util.zip.GZIPInputStream

/**
 * 描述:解析html文件类
 * 功能介绍:网络请求获取html，本地缓存html字符串内容
 * @param htmlHost 域名
 * @param htmlPath 路径
 * htmlHost + htmlPath = 完整链接
 *
 * htmlMatch 正则表达式获取对应的需要缓存的链接
 * 创建者:翁益亨
 * 创建日期:2023/8/23 10:27
 */
class WebViewHtmlServer(private val htmlUrl:String,private val htmlMatch:ArrayList<String>) {

    //建立网络连接的对象
    private var connectionImpl: URLConnection? = null
    //网络数据接收流
    private val outputStream = ByteArrayOutputStream()

    var serverRsp:String? = null


    init {
        connectionImpl = createConnection()
        initConnection(connectionImpl)
    }

    //发起请求的状态
    var responseCode:Int = WebViewConstants.ERROR_CODE_UNKNOWN
        get() {
            if(connectionImpl!=null && (connectionImpl is HttpURLConnection)){
               return (connectionImpl as HttpURLConnection).responseCode
            }
            return field
        }

    //获取数据流
    var responseStream:BufferedInputStream? = null
        get() {
            if(field == null && connectionImpl != null){
                try {
                    var inputStream:InputStream = connectionImpl!!.getInputStream()
                    return  if ("gzip".equals(connectionImpl!!.contentEncoding, ignoreCase = true)) {
                             BufferedInputStream(GZIPInputStream(inputStream))
                        } else {
                             BufferedInputStream(inputStream)
                        }
                }catch (e:Exception){

                }
            }
            return null
        }

    //创建连接
    private fun createConnection():URLConnection?{
        if(TextUtils.isEmpty(htmlUrl) ){
            return null
        }

        var connection:URLConnection? = null

        try {
            var url = URL(htmlUrl)

            connection = url.openConnection()

           if(connection!=null){

               if(connection is HttpURLConnection){
                   connection.instanceFollowRedirects = false
               }

               connection.setRequestProperty("Host",url.host)
           }


        }catch (e:Exception){
            if (connection != null) {
                connection = null
            }
        }

        return connection
    }


    //初始化连接信息
    private fun initConnection(connection:URLConnection?):Boolean{
        if(null!=connectionImpl){
            connection!!.connectTimeout = WebViewConfig.CONNECT_TIMEOUT_MILLIS
            connection!!.readTimeout = WebViewConfig.READ_TIMEOUT_MILLIS
            connection!!.setRequestProperty("method", "GET")
            connection!!.setRequestProperty("Accept-Encoding", "gzip")
            connection!!.setRequestProperty("Accept-Language", "zh-CN,zh;")

            //可扩展其他请求额外参数

            return true
        }

        return false
    }

    //请求连接，返回当前请求的标识内容
    fun connect():Int{
        //获取网络请求构建的结果
        var resultCode = internalConnect()

        if (WebViewConstants.ERROR_CODE_SUCCESS !== resultCode) {
            return resultCode // error case
        }

        //可选择性补充responseCode的返回处理内容

        //连接正常，尝试获取返回结果
         readServerResponse()

        return WebViewConstants.ERROR_CODE_SUCCESS
    }

    //手动调用
    @Synchronized
    fun internalConnect():Int{
        //建立链接内容
        if(connectionImpl is HttpURLConnection){
            try {
                connectionImpl!!.connect()
                return WebViewConstants.ERROR_CODE_SUCCESS;
            }catch (e:Exception){

            }
        }

        return WebViewConstants.ERROR_CODE_UNKNOWN;
    }


    //读取内容是否成功标识
    private fun readServerResponse():Boolean{
        var bufferedInputStream:BufferedInputStream? = responseStream ?: return false

        try {
            val buffer = ByteArray(WebViewConfig.READ_BUF_SIZE)

            var n = 0
            while ( -1 != bufferedInputStream!!.read(
                    buffer
                ).also {
                    n = it
                }
            ) {
                outputStream.write(buffer, 0, n)
            }

            if (n == -1) {
                serverRsp = outputStream.toString()

                logError("serverRsp:$serverRsp")
            }
        }catch (e:Exception){
            return false
        }

        return true
    }

    //获取页面中需要记载的href以及src内容
    fun getResponseLoadLink(response:String):ArrayList<String>{
        var preLinkList = ArrayList<String>()





        return preLinkList
    }


    //断开连接
    fun disconnect(){

    }


}