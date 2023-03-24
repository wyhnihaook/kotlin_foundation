package com.kotlin.practice.ui.gallery

import com.kotlin.practice.R
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.util.MediaStoreFile

/**
 * 描述:存储显示数据
 * 功能介绍:存储显示数据
 * 创建者:翁益亨
 * 创建日期:2023/2/10 16:16
 */
class GalleryListViewModel :BaseViewModel() {

    var images: MutableList<MediaStoreFile> = mutableListOf()

    override val title: String
        get() = BaseApp.getContext().resources.getString(R.string.gallery_list_page)
}