package com.zeekrlife.base.utils

import android.os.SystemClock
import android.view.View
import java.util.concurrent.ConcurrentHashMap

/**
 * 防抖功能
 */

const val DEBOUNCING_DEFAULT_VALUE: Long = 500
private val KEY_MILLIS_MAP: MutableMap<String, Long> = ConcurrentHashMap<String, Long>(64)

fun isValidDebouncing(view: View, duration: Long = DEBOUNCING_DEFAULT_VALUE): Boolean {
    return isValidDebouncing(view.hashCode().toString(), duration)
}

fun isValidDebouncing(key: String, duration: Long): Boolean {
    val curTime = SystemClock.elapsedRealtime()
    if (KEY_MILLIS_MAP.size > 64) {
        val iterator = KEY_MILLIS_MAP.entries.iterator()
        if (iterator.hasNext()) {
            val (_, validTime) = iterator.next()
            //当前的时间大于存储的时间，说明可以直接点击，释放所有点击能生效的view的hashCode值（之前点击存储的信息）
            if (curTime >= validTime) {
                //从当前数组中，删除next获取的当前元素
                iterator.remove()
            }
        }
    }

    //获取当前view存储的上一次点击的时间
    val validTime = KEY_MILLIS_MAP[key].ifLong()
    //当前的时间大于快速点击防抖的时间，点击生效，重置当前点击的时间
    if (curTime >= validTime) {
        //在duration时间间隔内，防止快速点击
        KEY_MILLIS_MAP[key] = curTime + duration
        return true
    }
    return false
}

fun View.setClickDebouncing(minTime: Long = DEBOUNCING_DEFAULT_VALUE, block: (View) -> Unit) {
    setOnClickListener {
        if (isValidDebouncing(it, minTime)) {
            block(it)
        }
    }
}

inline fun Long?.ifLong(): Long = this ?: 0L