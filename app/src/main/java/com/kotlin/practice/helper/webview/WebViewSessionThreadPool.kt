package com.kotlin.practice.helper.webview

import androidx.annotation.NonNull
import com.kotlin.practice.util.logError
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 描述:线程池
 * 功能介绍:专用于资源下载
 * 创建者:翁益亨
 * 创建日期:2023/8/14 17:48
 */
class WebViewSessionThreadPool {

    /**
     * ExecutorService object (Executors.newCachedThreadPool())
     */
    private var executorServiceImpl: ExecutorService? = null

    /**
     * Constructor and initialize thread pool object
     * default one core pool and the maximum number of threads is 6
     *
     */
    init {
        executorServiceImpl = ThreadPoolExecutor(
            1, 6,
            60L, TimeUnit.SECONDS,
            SynchronousQueue(),
            SessionThreadFactory()
        )
    }

    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the `Executor` implementation.
     *
     * @param task The runnable task
     * @return Submit success or not
     */
    private fun execute(task: Runnable): Boolean {
        return try {
            executorServiceImpl!!.execute(task)
            true
        } catch (e: Throwable) {
            logError("${"execute task error:" + e.message}")
            false
        }
    }

    /**
     * Post an runnable to the pool thread
     *
     * @param task The runnable task
     * @return Submit success or not
     */

    companion object{
        /**
         * Singleton object
         */
//        private val sInstance: WebViewSessionThreadPool =
//            WebViewSessionThreadPool()

        private val sInstance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            WebViewSessionThreadPool()
        }

        fun postTask(task: Runnable): Boolean {
            return sInstance.execute(task)
        }


        /**
         *  ThreadFactory
         */
        private class SessionThreadFactory internal constructor() : ThreadFactory {
            /**
             * Thread group
             */
            private val group: ThreadGroup

            /**
             * Thread number
             */
            private val threadNumber = AtomicInteger(1)

            /**
             * Constructor
             */
            init {
                val securityManager = System.getSecurityManager()
                group =
                    if (securityManager != null) securityManager.threadGroup else Thread.currentThread().threadGroup
            }

            /**
             * Constructs a new `Thread`.  Implementations may also initialize
             * priority, name, daemon status, `ThreadGroup`, etc.
             *
             * @param r A runnable to be executed by new thread instance
             * @return Constructed thread, or `null` if the request to
             * create a thread is rejected
             */
            override fun newThread(@NonNull r: Runnable): Thread {
                val thread = Thread(group, r, NAME_PREFIX + threadNumber.getAndIncrement(), 0L)
                if (thread.isDaemon) {
                    thread.isDaemon = false
                }
                if (thread.priority != 5) {
                    thread.priority = 5
                }
                return thread
            }

            companion object {
                /**
                 * Thread prefix name
                 */
                private const val NAME_PREFIX = "pool-webview-session-thread-"
            }
        }

    }
}