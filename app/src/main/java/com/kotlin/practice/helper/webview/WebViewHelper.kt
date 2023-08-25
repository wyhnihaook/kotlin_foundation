package com.kotlin.practice.helper.webview

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import com.kotlin.practice.util.logError
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

/**
 * 描述:帮助类
 * 功能介绍:工具类
 * 创建者:翁益亨
 * 创建日期:2023/8/14 15:36
 */
class WebViewHelper {

    companion object{
        /**
         * Response header extensions.
         */
        private const val HEADER_EXT = ".header"

        /**
         * The resource cache root dir which resource cache will be storage.
         * it's expected to be a dir in /sdcard dir for security.
         *
         * @return The root cache dir.
         */
        private fun getSonicResourceCacheDir(): File {
            val file = File(if(WebViewConfig.resourceCacheDir==null)Environment.getExternalStorageDirectory() else File(WebViewConfig.resourceCacheDir), WebViewConfig.resourceCacheDirName?:"/WebViewResource/")
            if (!file.exists() && !file.mkdir()) {
                logError("${"getSonicResourceCacheDir error:make dir(" + file.absolutePath + ") fail!"}")
                //不存在的情况通知提示
//                notifyError(null, file.absolutePath, WebViewConstants.ERROR_CODE_MAKE_DIR_ERROR)
            }
            return file
        }


        /**
         *
         * @param resourceName resource file name
         * @return The path of the resource header file.
         */
        fun getSonicResourceHeaderPath(resourceName: String): String {
            return getSonicResourceCachePath() + resourceName + HEADER_EXT
        }

        /**
         *
         * @param resourceName resource file name
         * @return The path of the resource file.
         */
        fun getSonicResourcePath(resourceName: String): String {
            return getSonicResourceCachePath() + resourceName
        }

        /**
         *
         * @return Returns the absolute path to the resource cache directory on
         * the filesystem (including File.separator at the end of path).
         */
        fun getSonicResourceCachePath(): String {
            var dirPath: String = getSonicResourceCacheDir().absolutePath ?:""
            if (!dirPath.endsWith(File.separator)) {
                dirPath += File.separator
            }
            return dirPath
        }

        /**
         * Get headers from local cache file
         *
         * @param headerPath header file path
         * @return The last http response headers from local cache.
         */
        fun getHeaderFromLocalCache(headerPath: String?): Map<String, MutableList<String>> {
            val headers: MutableMap<String, MutableList<String>> = HashMap()
            val headerFile = File(headerPath)
            if (headerFile.exists()) {
                val headerString: String = WebViewUtils.readFile(headerFile)?:""
                if (!TextUtils.isEmpty(headerString)) {
                    val headerArray =
                        headerString.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    if (headerArray.size > 0) {
                        var tmpHeaderList: MutableList<String>?
                        for (header in headerArray) {
                            val keyValues =
                                header.split(" : ".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            if (keyValues.size == 2) {
                                val key = keyValues[0].trim { it <= ' ' }
                                tmpHeaderList = headers[key.lowercase(Locale.getDefault())]
                                if (null == tmpHeaderList) {
                                    tmpHeaderList = ArrayList(1)
                                    headers[key.lowercase(Locale.getDefault())] = tmpHeaderList
                                }
                                tmpHeaderList.add(keyValues[1].trim { it <= ' ' })
                            }
                        }
                    }
                }
            }
            return headers
        }


        /**
         * Write string to the file represented by
         * the specified `File` object.
         *
         * @param str      The string is to be saved
         * @param filePath path to write
         * @return Returns `true` if string is saved successfully.
         */
        fun writeFile(str: String, filePath: String): Boolean {
            return writeFile(str.toByteArray(), filePath)
        }

        /**
         * Write bytes to the specific file.
         *
         * @param content   The data is to be saved
         * @param filePath  path to write
         * @return Returns `true` if string is saved successfully.
         */
        fun writeFile(content: ByteArray?, filePath: String): Boolean {
            val file = File(filePath)
            var fos: FileOutputStream? = null
            try {
                if (!file.exists() && !file.createNewFile()) {
                    return false
                }
                fos = FileOutputStream(file)
                fos.write(content)
                fos.flush()
                return true
            } catch (e: Throwable) {
                logError("${"writeFile error:(" + filePath + ") " + e.message}")
            } finally {
                if (null != fos) {
                    try {
                        fos.close()
                    } catch (e: Throwable) {
                        logError("${"writeFile close error:(" + filePath + ") " + e.message}")
                    }
                }
            }
            return false
        }


        /**
         *
         * @param headers response headers
         * @return the string which represent the last response header split by "\r\n
         */
        fun convertHeadersToString(headers: Map<String, List<String>>): String? {
            if (headers != null && headers.isNotEmpty()) {
                val headerString = StringBuilder()
                val entries:Set<Map.Entry<String, List<String>>> = headers.entries
                for ((key, values) in entries) {
                    if (!TextUtils.isEmpty(key)) {
                        for (value in values) {
                            if (!TextUtils.isEmpty(value)) {
                                headerString.append(key).append(" : ")
                                headerString.append(value).append("\r\n")
                            }
                        }
                    }
                }
                return headerString.toString()
            }
            return ""
        }

    }


}