package com.kotlin.practice.util

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**
 * 描述:静态方法提供本地url的转化
 * 功能介绍:将uri链接转化成能直接访问的地址
 * 创建者:翁益亨
 * 创建日期:2023/1/29 17:23
 */
fun File.getUriFromFile(context: Context): Uri =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        getUriFormFile24(context)
    } else {
        Uri.fromFile(this)
    }

//这里的 .fileProvider字符串同 AndroidManifest.xml中的provider声明的authorities
fun File.getUriFormFile24(context: Context):Uri = FileProvider.getUriForFile(context,context.packageName + ".fileProvider",this)
