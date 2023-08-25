package com.kotlin.practice.helper.webview

import android.util.Log
import androidx.annotation.NonNull
import com.kotlin.practice.util.logError
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference

/**
 * 描述:下载文件的输入流
 * 功能介绍:保存到任务队列中用于回显
 * 创建者:翁益亨
 * 创建日期:2023/8/14 17:23
 */
class WebViewSessionStream( callback: Callback,
                            outputStream: ByteArrayOutputStream,
                            netStream: BufferedInputStream?,
                            private val resourceUrl:String?) : InputStream() {

    companion object{

    }

    /**
     * Unread data from network
     */
    private var netStream: BufferedInputStream? = null

    /**
     * Read data from network
     */
    private var memStream: BufferedInputStream? = null

    /**
     * OutputStream include `memStream` data and `netStream` data
     */
    private var outputStream: ByteArrayOutputStream? = null

    /**
     * `netStream` data completed flag
     */
    private var netStreamReadComplete = true

    /**
     * `memStream` data completed flag
     */
    private var memStreamReadComplete = true

    /**
     * When `SonicSessionStream` close the stream will invoke the `Callback`
     */
    interface Callback {
        /**
         * Close callback
         *
         * @param readComplete `SonicSessionStream` data has read completed
         * @param outputStream outputStream include `memStream` data and `netStream` data
         */
        fun onClose(readComplete: Boolean, outputStream: ByteArrayOutputStream)
    }

    /**
     * Callback WeakReference
     */
    private var callbackWeakReference: WeakReference<Callback>? = null


    //构造函数
    /**
     * Constructor
     *
     * @param callback     Callback
     * @param outputStream Read data from network
     * @param netStream    Unread data from network
     */
    init {
        if (null != netStream) {
            this.netStream = netStream
            netStreamReadComplete = false
        }
        if (outputStream != null) {
            this.outputStream = outputStream
            memStream = BufferedInputStream(ByteArrayInputStream(outputStream.toByteArray()))
            memStreamReadComplete = false
        } else {
            this.outputStream = ByteArrayOutputStream()
        }
        callbackWeakReference = WeakReference(callback)
    }



    /**
     * Closes this input stream and releases any system resources
     * associated with the stream and invoke the callback's onClose method
     *
     */
    @Throws(IOException::class)
    override fun close() {

        logError("${"$resourceUrl -close: memory stream and socket stream, netStreamReadComplete=$netStreamReadComplete, memStreamReadComplete=$memStreamReadComplete"}")

        var error: Throwable? = null
        try {
            if (null != memStream) {
                memStream!!.close()
            }
        } catch (e: Throwable) {

            logError("${"close memStream error:" + e.message}")

            error = e
        } finally {
            memStream = null
        }
        try {
            if (null != netStream) {
                netStream!!.close()
            }
        } catch (e: Throwable) {
            logError("${"close netStream error:" + e.message}")
            error = e
        } finally {
            netStream = null
        }
        val callback: Callback? =
            callbackWeakReference!!.get()
        callback?.onClose(netStreamReadComplete && memStreamReadComplete, outputStream!!)
        outputStream = null
        if (error != null) {
            logError("${"throw error:" + error.message}")
            if (error is IOException) {
                throw (error as IOException?)!!
            } else { // Turn all exceptions to IO exceptions to prevent scenes that the kernel can not capture
                throw IOException(error)
            }
        }
    }

    /**
     *
     *
     *
     * Reads a single byte from this stream and returns it as an integer in the
     * range from 0 to 255. Returns -1 if the end of the stream has been
     * reached. Blocks until one byte has been read, the end of the source
     * stream is detected or an exception is thrown.
     *
     * @throws IOException if the stream is closed or another IOException occurs.
     */
    @Synchronized
    @Throws(IOException::class)
    override fun read(): Int {
        var c = -1
        try {
            if (null != memStream && !memStreamReadComplete) {
                c = memStream!!.read()
            }
            if (-1 == c) {
                memStreamReadComplete = true
                if (null != netStream && !netStreamReadComplete) {
                    c = netStream!!.read()
                    if (-1 != c) {
                        outputStream!!.write(c)
                    } else {
                        netStreamReadComplete = true
                    }
                }
            }
        } catch (e: Throwable) {
            logError("${"read error:" + e.message}")

            if (e is IOException) {
                throw e
            } else { //Turn all exceptions to IO exceptions to prevent scenes that the kernel can not capture
                throw IOException(e)
            }
        }
        return c
    }

    /**
     * Reads a byte of data from this input stream
     * Equivalent to `read(buffer, 0, buffer.length)`.
     */
    @Synchronized
    @Throws(IOException::class)
    override fun read(@NonNull buffer: ByteArray): Int {
        return read(buffer, 0, buffer.size)
    }

    /**
     *
     * Reads up to `byteCount` bytes from this stream and stores them in
     * the byte array `buffer` starting at `byteOffset`.
     * Returns the number of bytes actually read or -1 if the end of the stream
     * has been reached.
     *
     * @throws IndexOutOfBoundsException if `byteOffset < 0 || byteCount < 0 || byteOffset + byteCount > buffer.length`.
     * @throws IOException               if the stream is closed or another IOException occurs.
     */
    @Synchronized
    @Throws(IOException::class)
    override fun read(@NonNull buffer: ByteArray, byteOffset: Int, byteCount: Int): Int {
        val arrayLength = buffer.size
        if ((byteOffset or byteCount < 0) || (byteOffset > arrayLength) || (arrayLength - byteOffset < byteCount)) {
            throw ArrayIndexOutOfBoundsException()
        }
        for (i in 0 until byteCount) {
            var c: Int
            try {
                if (read().also { c = it } == -1) {
                    return if (i == 0) -1 else i
                }
            } catch (e: IOException) {
                if (i != 0) {
                    return i
                }
                throw e
            }
            buffer[byteOffset + i] = c.toByte()
        }
        return byteCount
    }
}