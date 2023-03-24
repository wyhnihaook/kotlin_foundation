package com.kotlin.practice.ui.database

import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseActivity
import com.kotlin.practice.databinding.ActivityOperateDatabaseBinding

/**
 * 描述:操作数据库页面
 * 功能介绍:操作数据库页面
 * 创建者:翁益亨
 * 创建日期:2023/2/8 15:56
 */
class OperateDatabaseActivity :BaseActivity<ActivityOperateDatabaseBinding,OperateDatabaseViewModel>(){
    override fun getLayoutId(): Int = R.layout.activity_operate_database

    override fun initVariableId(): Int = BR.vm

}