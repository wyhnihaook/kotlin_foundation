package com.kotlin.practice.ui.gallery

import com.kotlin.practice.R
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.util.MediaStoreFile

/**
 * 描述:图库适配器
 * 功能介绍:数据存储
 * 创建者:翁益亨
 * 创建日期:2023/2/13 15:13
 */
class GalleryViewModel :BaseViewModel(){

    override val title: String
        get() = BaseApp.getContext().getString(R.string.gallery_page)

    var images: MutableList<MediaStoreFile> = mutableListOf()

    //跳转页面携带定位数据
    var checkIndex :Int = 0
    var checkPicName :String = ""
}