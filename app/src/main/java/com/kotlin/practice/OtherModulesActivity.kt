package com.kotlin.practice

import android.content.Intent
import android.view.KeyEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kotlin.practice.base.BaseActivity
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.databinding.ActivityOtherModulesBinding
import com.kotlin.practice.util.KEY_EVENT_ACTION
import com.kotlin.practice.util.KEY_EVENT_EXTRA

/**
 * 描述:其他功能块页面
 * 功能介绍:模拟分包开发，每一个完整的功能入口都新开一个Activity
 * 创建者:翁益亨
 * 创建日期:2023/1/10 14:04
 */
class OtherModulesActivity :BaseActivity<ActivityOtherModulesBinding,BaseViewModel>(){

    override fun getLayoutId(): Int = R.layout.activity_other_modules

    override fun initVariableId(): Int = BR.vm




}