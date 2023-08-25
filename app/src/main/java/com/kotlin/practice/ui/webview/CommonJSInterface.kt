package com.kotlin.practice.ui.webview

import android.content.Context
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.util.logError

/**
 * 描述:和h5交互内容
 * 功能介绍:临时实现内容
 * 创建者:翁益亨
 * 创建日期:2023/8/15 14:01
 */
class CommonJSInterface(var activity: AppCompatActivity, var wv: WebView)  {

    @JavascriptInterface
    fun execNative(jsonString: String?) {
        if (TextUtils.isEmpty(jsonString)) return

        logError("html调用原生方法")
        var params: JSParams? = null
        try {
            params = GsonSerializer.get()!!.fromJson(jsonString, JSParams::class.java)
        } catch (ignore: Exception) {
        }
        if (params == null || TextUtils.isEmpty(params.actionName)) return

        if (TextUtils.isEmpty(params.actionType)) {
            //默认没有类型的时候，走老的逻辑，就是h5直接衔接
        } else {
            if (params.actionType.equals(JSParams.ACTION_TYPE_NAVBAR)) {
                when (params.actionName) {
                    JSParams.ACTION_TYPE_NAVBAR_CLOSE -> {
                        activity.runOnUiThread { activity.finish() }
                    }
                }
            } else if (params.actionType.equals(JSParams.ACTION_TYPE_HARDWARE)) {
                when (params.actionName) {
                    JSParams.ACTION_TYPE_HARDWARE_SCREEN_SIZE -> screenSize(params.actionId?:"")
                }
            } else if (params.actionType.equals(JSParams.ACTION_TYPE_OPEN_PAGE)) {
                when (params.actionName) {
                }
            }else if(params.actionType.equals(JSParams.ACTION_TYPE_USER_STATUS)){
                when (params.actionName) {
                }
            }else if(params.actionType.equals(JSParams.ACTION_TYPE_OTHER)){
                when (params.actionName) {
                }
            }
        }

    }


    private fun screenSize( actionId:String) {
        val map = HashMap<String,Any>()

        //获取屏幕尺寸，返回当前的是分辨率
        val displayMetrics = DisplayMetrics()
        val wm: WindowManager = BaseApp.getContext()
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display: Display = wm.defaultDisplay
        display.getMetrics(displayMetrics)

        //获取状态栏的高度
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight: Int = (activity.resources.getDimensionPixelSize(resourceId)/displayMetrics.density).toInt()

        map["height"] = displayMetrics.heightPixels.toString()
        map["safeBottomHeight"] = "0"//底部安全区域，IOS专用
        map["screenScale"] = "1"//未知，IOS定义未使用
        map["width"] = displayMetrics.widthPixels.toString();
        map["platform"] = "Android"
        map["statusBarHeight"] = statusBarHeight.toString()//状态栏高度

        val responseResult = HashMap<String,Any>()
        responseResult["code"] = 1
        responseResult["msg"] = ""
        responseResult["data"] = map

        val responseData = HashMap<String,Any>()
        responseData["actionType"] = JSParams.ACTION_TYPE_HARDWARE
        responseData["actionName"] = JSParams.ACTION_TYPE_HARDWARE_SCREEN_SIZE
        responseData["actionId"] = actionId
        responseData["response"] = responseResult


        val data:String =GsonSerializer.get()!!.toJson(responseData)
        //执行回调，将数据返回到h5
        activity.runOnUiThread {
            val method = "javascript:typeof(bytNativeCallJs)!='undefined'&&bytNativeCallJs($data)"
            wv.loadUrl(method)
        }
    }
}