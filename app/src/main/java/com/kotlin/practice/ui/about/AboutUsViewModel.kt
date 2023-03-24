package com.kotlin.practice.ui.about

import com.kotlin.practice.R
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.base.application

/**
 * 描述:关于我们
 * 功能介绍:关于我们
 * 创建者:翁益亨
 * 创建日期:2023/2/27 10:31
 */
class AboutUsViewModel :BaseViewModel() {
    override val title: String
        get() = application.getString(R.string.about_us_page)
}