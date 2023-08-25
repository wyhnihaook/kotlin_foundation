package com.kotlin.practice.ui.webview

import java.io.Serializable

/**
 * 描述:TODO
 * 功能介绍:TODO
 * 创建者:翁益亨
 * 创建日期:2023/8/15 14:02
 */
class JSParams: Serializable {

    companion object{
        const val ACTION_CLOSE = "CLOSE"
        const val ACTION_BACK = "BACK"
        const val ACTION_DO_LOGIN = "DO_LOGIN"
        const val ACTION_GET_LOGIN_INFO = "GET_LOGIN_INFO"
        const val ACTION_SET_STATUS_BAR_MODE = "SET_STATUS_BAR_MODE"
        const val ACTION_SHARE = "share"
        const val ACTION_OPEN_PAGE = "open_page" //打开新页面

        //v1.6.0新功能
        const val ACTION_SET_TITLE = "title" //设置头部标题名称


        const val CODE_SUCCESS = 0
        const val CODE_METHOD_NOT_FOUND = 1

        const val MSG_METHOD_NOT_FOUND = "没有找到native方法"

        /**h5中新增统一交互 */ //导航栏中处理的数据
        const val ACTION_TYPE_NAVBAR = "navibar"

        //导航栏的返回
        const val ACTION_TYPE_NAVBAR_BACK = "go_back"

        //导航栏的关闭页面
        const val ACTION_TYPE_NAVBAR_CLOSE = "close"

        //导航栏设置当前标题
        const val ACTION_TYPE_NAVBAR_TITLE = "title"

        //导航栏分享功能
        const val ACTION_TYPE_NAVBAR_SHARE = "share"

        //导航栏顶部模式，亮色/暗色
        const val ACTION_TYPE_NAVBAR_STATUS_BAR = "status_bar"

        //获取用户信息
        const val ACTION_TYPE_USER_STATUS = "user_status"

        //获取登录信息
        const val ACTION_TYPE_USER_LOGIN_INFO = "login_info"

        //获取用户信息
        const val ACTION_TYPE_OTHER = "other"
        const val ACTION_TYPE_SHARE_FILE = "share_file"
        const val ACTION_TYPE_FLUTTER_NOTIFY = "flutter_notify"
        const val ACTION_TYPE_DOWNLOAD_FILE = "download_file"
        const val ACTION_TYPE_REQUEST_PERMISSION = "request_permission"

        //硬件数据处理
        const val ACTION_TYPE_HARDWARE = "hardware"

        //拨打电话
        const val ACTION_TYPE_HARDWARE_CALL_PHONE = "call_phone"

        //获取硬件数据
        const val ACTION_TYPE_HARDWARE_SCREEN_SIZE = "screen_size"

        //跳转页面
        const val ACTION_TYPE_OPEN_PAGE = "open_page"

        //跳转原生页面
        const val ACTION_TYPE_OPEN_NATIVE = "open_native"

        //跳转flutter页面
        const val ACTION_TYPE_OPEN_FLUTTER = "open_flutter"

        //跳转Web页面
        const val ACTION_TYPE_OPEN_WEB_VIEW = "open_web"

        fun getBackParams(): JSParams {
            val params = JSParams()
            params.actionName = ACTION_BACK
            return params
        }

        class Response : Serializable {
            var code = 0
            var message: String? = null
            var data: ResponseData? = null
        }

        class ResponseData : Serializable {
            var token: String? = null
            var empId: String? = null
        }

        class ReqParams : Serializable {
            var isLightMode = false
            var actionId: String? = null
            var url //优先使用url
                    : String? = null
            var title //产品名称
                    : String? = null
            var describe //产品描述
                    : String? = null
            var shareId //分享id
                    : String? = null
            var contentType //互联网产品
                    : String? = null
            var source //埋点源
                    : String? = null
            var imageUrl //网络图片
                    : String? = null
            var page_name //公共组件
                    : String? = null
            var page_arguments //公共组件
                    : Map<*, *>? = null

            //params新增参数
            var phoneNumber //拨打电话信息
                    : String? = null

            //下载的分享文件
            var fileUrl: String? = null

            //下载文件的类型。（用于本地下载到对应目录）例如：图片下载到图库，pdf下载到外部存储
            //picture/video:下载到图库/视频（主要兼容iOS，），同步到相册/download：下载本地内存中   默认下载到本地文件内容
            var fileType: String? = null

            //2.5.0新增刷新功能
            var actionName //通知名称
                    : String? = null
            var actionArguments //通知携带的参数
                    : Map<*, *>? = null

            //2.6.0新增分享到微信是否额外添加参数标识
            //1.微信，2.企业微信，3.复制链接 {"1":"aaa","2":"ccc","3":"ddd"}
            var shareParameters: Map<*, *>? = null

            //3.1.1权限功能增加
            var permission: String? = null
        }
    }

    fun isResponseSuccess(): Boolean {
        return response!!.code == CODE_SUCCESS
    }

    fun isBackAction(): Boolean {
        return actionName != null && (actionName == ACTION_BACK)
    }

    fun setLoginInfoResponse(token: String, empId: String) {
        response = Response()
        response!!.code = CODE_SUCCESS
        response!!.data = ResponseData()
        response!!.data!!.token = token
        response!!.data!!.empId = empId
    }

    fun setMethodNotFound() {
        response = Response()
        response!!.code = CODE_METHOD_NOT_FOUND
        response!!.message = MSG_METHOD_NOT_FOUND
    }


    /**h5中新增统一交互 */
    var actionType //h5和原生的交互添加类型
            : String? = null
    var actionName // 操作类型
            : String? = null
    var actionId // 调用Id,随机数即可。用于区别同一类型的多次掉用。非必填
            : String? = null
    var reqParams: ReqParams? = null
    var params: ReqParams? = null
    var response:Response? = null

    /**
     * @return 返回状态栏模式
     */
    fun isLightMode(): Boolean {
        return if (reqParams == null) false else reqParams!!.isLightMode
    }

}