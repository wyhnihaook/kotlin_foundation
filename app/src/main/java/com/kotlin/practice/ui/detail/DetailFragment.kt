package com.kotlin.practice.ui.detail

import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseFragment
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.databinding.FragmentDetailBinding

/**
 * 描述:详情
 * 功能介绍:占位
 * 创建者:翁益亨
 * 创建日期:2023/1/31 15:51
 */
class DetailFragment :BaseFragment<FragmentDetailBinding,BaseViewModel>(){
    override fun getLayoutId(): Int = R.layout.fragment_detail

    override fun initVariableId(): Int = BR.vm
}