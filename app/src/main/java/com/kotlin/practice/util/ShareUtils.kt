package com.kotlin.practice.util

import android.content.Context
import android.net.Uri
import androidx.core.app.ShareCompat

/**
 * 描述:分享功能封装
 * 功能介绍:挂载到context的系统分享功能
 * 创建者:翁益亨
 * 创建日期:2023/1/17 10:14
 */

//默认的标题名称
const val titleDefault = "分享弹窗标题"

/**
 * 分享文本
 * @param content ：分享出来的文本内容
 * @param title ：系统弹窗顶部显示的标题信息
 */
fun Context.shareText(content: String = "", title: String = titleDefault) =
    share("text/plain") {
        setText(content)
        setChooserTitle(title)
    }


/**
 * 分享单张图片
 * */
fun Context.shareImage(imageUri: Uri, title: String = titleDefault) =
    shareTextWithImage(null, imageUri, title)

/**
 * 分享多图
 * */
fun Context.shareImages(imageUris: List<Uri>, title: String = titleDefault) =
    shareTextWithImages(null, imageUris, title)

/**
 * 分享图片（可携带文本）
 * @param imageUri ：当前访问图片的内存地址信息
 * */
fun Context.shareTextWithImage(content: String?, imageUri: Uri, title: String = titleDefault) =
    share("image/*") {
        setText(content)
        setStream(imageUri)
        setChooserTitle(title)
    }


/**
 * 分享多图
 * */
fun Context.shareTextWithImages(content: String?, imageUris: List<Uri>, title: String = titleDefault) =
    share("image/*") {
        setText(content)
        imageUris.forEach { addStream(it) }
        setChooserTitle(title)
    }


/**
 * 单文件分享
 * */
fun Context.shareFile(uri: Uri, mimeType: String, title: String = titleDefault) = share(mimeType) {
    setStream(uri)
    setChooserTitle(title)
}


/**
 * 多文件分享（这里要注意，多文件必须是相同的类型）
 * */
fun Context.shareFiles(uris: List<Uri>, mimeType: String, title: String = titleDefault) = share(mimeType) {
    uris.forEach {
        addStream(it)
    }
    setChooserTitle(title)
}


/**
 * 内联函数，确定分享结果
 * @param mimeType : 分享类型。示例：text/plain
 * @param block ：局部加强内联优化，让内联函数里的函数类型的参数可以被间接调用
 * 使用ShareCompat.IntentBuilder构造器模式进行参数的配置
 * */
inline fun Context.share(
    mimeType: String = "",
    crossinline block: ShareCompat.IntentBuilder.() -> Unit
) = ShareCompat.IntentBuilder(this).setType(mimeType).apply(block).startChooser()


