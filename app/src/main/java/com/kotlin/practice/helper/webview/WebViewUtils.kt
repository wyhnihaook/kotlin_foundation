package com.kotlin.practice.helper.webview

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.kotlin.practice.util.logError
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.security.MessageDigest

/**
 * 描述:工具类
 * 功能介绍:工具类
 * 创建者:翁益亨
 * 创建日期:2023/8/14 16:38
 */
class WebViewUtils {

    companion object{

        /**
         * save resource files, including resource and headers.
         *
         * @param resourceName resource file name
         * @param resourceBytes resource bytes content
         * @param headers resource headers
         * @return The result of save files. true if all data is saved successfully.
         */
        fun saveResourceFiles(
            resourceName: String?,
            resourceBytes: ByteArray?,
            headers: Map<String, List<String>>
        ): Boolean {
            if (resourceBytes != null && !WebViewHelper.writeFile(
                    resourceBytes,
                    WebViewHelper.getSonicResourcePath(resourceName?:"")
                )
            ) {

                logError("${"saveResourceFiles error: write resource data fail."}")

                return false
            }
            //保存文件时，

//            if (headers != null && headers.isNotEmpty()&& !WebViewHelper.writeFile(
//                    WebViewHelper.convertHeadersToString(
//                        headers
//                    )?:"", WebViewHelper.getSonicResourceHeaderPath(resourceName?:"")
//                )
//            ) {
//                logError("${"saveResourceFiles error: write header file fail."}")
//
//                return false
//            }
            return true
        }


        /**
         * save resource data to database, such as resource sha1, resource size etc.
         *
         * @param resourceUrl the resource url
         * @param resourceSha1 the resource sha1
         * @param resourceSize the resource size
         */
        fun saveSonicResourceData(resourceUrl: String, resourceSha1: String, resourceSize: Long) {

            logError("${"saveSonicResourceData resourceUrl = $resourceUrl, resourceSha1 = $resourceSha1, resourceSize = $resourceSize"}")

            //保存到本地数据库信息
        }

        fun getSHA1(content: String): String? {
            return if (TextUtils.isEmpty(content)) {
                ""
            } else getSHA1(content.toByteArray())
        }

        fun getSHA1(contentBytes: ByteArray?): String? {
            return if (contentBytes == null || contentBytes.isEmpty()) {
                ""
            } else try {
                val sha1 = MessageDigest.getInstance("SHA1")
                sha1.update(contentBytes, 0, contentBytes.size)
                toHexString(sha1.digest())
            } catch (e: Exception) {
                ""
            }
        }

        /*
        * @param file The file path of template
        * @return Returns a string containing all of the content read from template file.
        */
        fun readFile(file: File?): String? {
            if (file == null || !file.exists() || !file.canRead()) {
                return null
            }

            // read
            var bis: BufferedInputStream? = null
            var reader: InputStreamReader? = null
            val buffer: CharArray
            var rtn: String? = null
            var n: Int
            try {
                bis = BufferedInputStream(FileInputStream(file))
                reader = InputStreamReader(bis)
                val size = file.length().toInt()
                if (size > 1024 * 12) {
                    buffer = CharArray(1024 * 4)
                    val result = java.lang.StringBuilder(1024 * 12)
                    while (-1 != reader.read(buffer).also { n = it }) {
                        result.append(buffer, 0, n)
                    }
                    rtn = result.toString()
                } else {
                    buffer = CharArray(size)
                    n = reader.read(buffer)
                    rtn = String(buffer, 0, n)
                }
            } catch (e: Throwable) {
                logError("${"readFile error:(" + file.name + ") " + e.message}")
            } finally {
                if (bis != null) {
                    try {
                        bis.close()
                    } catch (e: java.lang.Exception) {
                        logError("${"readFile close error:(" + file.name + ") " + e.message}")
                    }
                }
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: java.lang.Exception) {
                        logError("${"readFile close error:(" + file.name + ") " + e.message}")
                    }
                }
            }
            return rtn
        }


        fun getMD5(content: String): String? {
            return if (TextUtils.isEmpty(content)) "" else try {
                val sha1 = MessageDigest.getInstance("MD5")
                sha1.update(content.toByteArray(), 0, content.toByteArray().size)
                toHexString(sha1.digest())
            } catch (e: java.lang.Exception) {
                ""
            }
        }

        private fun toHexString(b: ByteArray): String? {
            val sb = StringBuilder(b.size * 2)
            for (aB in b) {
                sb.append(hexChar.get(aB.toInt() and 0xf0 ushr 4))
                sb.append(hexChar.get(aB.toInt() and 0xf))
            }
            return sb.toString()
        }

        private val hexChar = charArrayOf(
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        )


        /**
         *
         * @param file path of the file to read
         * @return Returns the content bytes read from the file.
         */
        fun readFileToBytes(file: File): ByteArray? {
            logError("${!file.canRead()}--${!file.exists()}--${file == null}")
            if (file == null || !file.exists() || !file.canRead()) {
                return null
            }

            // read
            var bis: BufferedInputStream? = null
            var out: ByteArrayOutputStream? = null
            var rtn: ByteArray? = null
            var n: Int
            try {
                bis = BufferedInputStream(FileInputStream(file))
                val size = file.length().toInt()
                if (size > 1024 * 12) {
                    out = ByteArrayOutputStream()
                    val buffer = ByteArray(1024 * 4)
                    while (bis.read(buffer).also { n = it } != -1) {
                        out.write(buffer, 0, n)
                    }
                    rtn = out.toByteArray()
                } else {
                    rtn = ByteArray(size)
                    n = bis.read(rtn)
                }
            } catch (e: Throwable) {
                logError("${"readFile error:(" + file.name + ") " + e.message}")
            } finally {
                if (bis != null) {
                    try {
                        bis.close()
                    } catch (e: Exception) {
                        logError("${"readFile close error:(" + file.name + ") " + e.message}")
                    }
                }
            }
            return rtn
        }

        /**
         * Get mime type for url simply.
         * (Maybe `android.webkit.MimeTypeMap.getMimeTypeFromExtension` is better.)
         * @param url target url
         * @return mime type
         */
        fun getMime(url: String): String {
            var mime = "text/html"
            val currentUri = Uri.parse(url)
            val path = currentUri.path
            if (path!!.endsWith(".css")) {
                mime = "text/css"
            } else if (path.endsWith(".js")) {
                mime = "application/x-javascript"
            } else if (path.endsWith(".jpg") || path.endsWith(".gif") ||
                path.endsWith(".png") || path.endsWith(".jpeg") ||
                path.endsWith(".webp") || path.endsWith(".bmp")
            ) {
                mime = "image/*"
            }
            return mime
        }


        /**
         * Get filtered headers by session id, this method will return a map of header(k-v) which
         * will not contains "Set-Cookie", "Cache-Control", "Expires".
         *
         * @param srcHeaders      The source headers
         * @return The headers of sessionId
         *
         * 使用请求头携带信息来标识html是否修改，返回304则直接使用数据库/本地html文件，如果是200就需要重新获取字符串比对后使用（理论上一定不为同一个数据）
         * 每次请求从请求头中获取同步到数据库中
         */
        fun getFilteredHeaders(srcHeaders: Map<String, List<String>>?): HashMap<String, String> {
            val headers = HashMap<String, String>()
            if (null != srcHeaders) {
                var headerValues: List<String>
                try {
                    for ((key, value) in srcHeaders) {
                        if ("Set-Cookie".equals(key, ignoreCase = true) || "Cache-Control".equals(
                                key,
                                ignoreCase = true
                            ) ||
                            "Expires".equals(key, ignoreCase = true) || "Etag".equals(
                                key,
                                ignoreCase = true
                            )
                        ) {
                            // forbid webview kernel to run cache related logic
                            continue
                        }
                        headerValues = value
                        if (null != headerValues && 1 == headerValues.size) {
                            headers[key] = headerValues[0]
                        }
                    }
                } catch (e: Throwable) {
                    logError("${"getFilteredHeaders error! " + e.message}")
                }
            }
            return headers
        }
    }

}