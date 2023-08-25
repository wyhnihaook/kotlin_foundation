package com.kotlin.practice.ui.webview

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * 描述:TODO
 * 功能介绍:TODO
 * 创建者:翁益亨
 * 创建日期:2023/8/15 14:11
 */
class GsonSerializer {


    companion object{

        private var instance: GsonSerializer? = null;
        //       Synchronized添加后就是线程安全的的懒汉模式
        @Synchronized
        fun get(): GsonSerializer? {
            if (instance == null) {
                instance = GsonSerializer();
            }
            return instance;
        }
    }

    private val mGson = Gson()

    fun toJson(`object`: Map<*, *>?): String {
        return mGson.toJson(`object`)
    }

    fun toJson(`object`: Any?): String? {
        return mGson.toJson(`object`)
    }

    fun toJsonArray(`object`: Any?): JsonArray? {
        return mGson.toJsonTree(`object`).asJsonArray
    }

    /**
     * 返回格式化后的json字符串（带反斜杠\）
     */
    fun toJsonString(`object`: Any?): String? {
        return if (`object` is String) {
            mGson.toJson(`object`)
        } else mGson.toJson(mGson.toJson(`object`))
    }

    fun toJsonTree(`object`: Any?): JsonObject? {
        return mGson.toJsonTree(`object`).asJsonObject
    }

    fun <T> fromJson(json: String?, cls: Class<T>?): T {
        return mGson.fromJson(json, cls)
    }

    fun <T> fromJson(json: String?, type: Type?): T {
        return mGson.fromJson(json, type)
    }

    fun <T> fromJson(json: JsonElement?, cls: Class<T>?): T {
        return mGson.fromJson(json, cls)
    }
}