package com.kotlin.practice.helper.webview

import com.kotlin.practice.db.web.Web

/**
 * 描述:
 * 功能介绍:
 * 创建者:翁益亨
 * 创建日期:2023/8/14 16:10
 */
class WebViewConstants {

    companion object{

        //缓存本地存在的数据Web缓存数据
        var WebCacheDataList = ArrayList<Web>()


        /**
         * SonicSDK log prefix
         */
        const val SONIC_SDK_LOG_PREFIX = "WebViewSdk_"

        /**
         * SonicSDK version
         */
        const val SONIC_VERSION_NUM = "2.0.0"

        /**
         * Sonic parameter prefix
         */
        const val SONIC_PARAMETER_NAME_PREFIX = "webview_"

        /**
         * This parameter in url will be as part of session id，and it is separated by SONIC_REMAIN_PARAMETER_SPLIT_CHAR.
         */
        const val SONIC_REMAIN_PARAMETER_NAMES = "webview_remain_params"


        const val SONIC_REMAIN_PARAMETER_SPLIT_CHAR = ";"

        /**
         * SonicSession mode : StandardSonicSession
         */
        const val SESSION_MODE_DEFAULT = 0

        /**
         * SonicSession mode : QuickSonicSession
         */
        const val SESSION_MODE_QUICK = 1

        /**
         * Unknown
         */
        const val ERROR_CODE_UNKNOWN = -1

        /**
         * Success
         */
        const val ERROR_CODE_SUCCESS = 0

        /**
         * Http(s) connection error : IO Exception
         */
        const val ERROR_CODE_CONNECT_IOE = -901

        /**
         * Http(s) connection error : time out
         */
        const val ERROR_CODE_CONNECT_TOE = -902

        /**
         * Http(s) connection error : nullPointer in native
         */
        const val ERROR_CODE_CONNECT_NPE = -903


        /**
         * Verify local file failed
         */
        const val ERROR_CODE_DATA_VERIFY_FAIL = -1001

        /**
         * Failed to create sonic directory
         */
        const val ERROR_CODE_MAKE_DIR_ERROR = -1003

        /**
         * File save failed
         */
        const val ERROR_CODE_WRITE_FILE_FAIL = -1004

        /**
         * Separate html to template and data failed
         */
        const val ERROR_CODE_SPLIT_HTML_FAIL = -1005

        /**
         * Obtain difference data between server and local data failed
         */
        const val ERROR_CODE_MERGE_DIFF_DATA_FAIL = -1006

        /**
         * Server data exception
         */
        const val ERROR_CODE_SERVER_DATA_EXCEPTION = -1007

        /**
         * Build template and data to html failed
         */
        const val ERROR_CODE_BUILD_HTML_ERROR = -1008

    }

}