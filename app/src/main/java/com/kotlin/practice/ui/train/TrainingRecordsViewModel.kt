package com.kotlin.practice.ui.train

import com.kotlin.practice.R
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.base.BaseViewModel

/**
 * 描述:培训记录
 * 功能介绍:培训记录数据
 * 创建者:翁益亨
 * 创建日期:2023/2/24 10:42
 */
class TrainingRecordsViewModel : BaseViewModel() {

    override val title: String
        get() = BaseApp.getContext().getString(R.string.train_records_page)
}