package com.kotlin.practice.contentprovider

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.kotlin.practice.util.logError

/**
 * 描述:获取Context
 * 功能介绍:模拟第三方sdk通过contentProvider获取当前应用程序流程
 * 创建者:翁益亨
 * 创建日期:2023/2/16 14:45
 */
class InitialContentProvider :ContentProvider() {
    /**通过程序启动，初始化该实例，会获得对于的程序的Context对象*/
    override fun onCreate(): Boolean {
        //这里打印的context为com.kotlin.practice.base.BaseApp@96cad3a
        logError("context init :$context")
        initContentProviderContext(context!!)
      return  true
    }


    /**不需要实现的操作方法*/

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}

private lateinit var currentAppContext:Context
fun initContentProviderContext(context: Context){
    logError("初始化第三方sdk context成功：$context")
    currentAppContext = context
}