package com.kotlin.practice.ui.download

import android.Manifest
import android.Manifest.permission.REQUEST_INSTALL_PACKAGES
import android.app.DownloadManager
import android.text.TextUtils
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseActivity
import com.kotlin.practice.databinding.ActivityDownloadBinding
import com.kotlin.practice.util.*
import com.zeekrlife.base.utils.setClickDebouncing

/**
 * 描述:下载任务界面
 * 功能介绍:体现进度，支持开始暂停，多线程
 * 创建者:翁益亨
 * 创建日期:2023/2/13 15:37
 */
class DownloadActivity : BaseActivity<ActivityDownloadBinding, DownloadViewModel>() {

    override fun getLayoutId(): Int = R.layout.activity_download

    override fun initVariableId(): Int = BR.vm

    override fun initParams() {
        super.initParams()

        //（安装权限在设置中，需要提示用户手动打开），兼容处理：授权+设置模块打开
        //如果需要设置中打开，那么每次授权都是拒绝的
        viewModel.permissionInstallLauncher = registerForPermissionResult(onGranted = {
            viewModel.startDownload(this@DownloadActivity)
        },
            onDenied = { scope, _ ->
                scope?.installPackage {
                    viewModel.startDownload(this@DownloadActivity)
                }
            },
            onShowRequestRationale = { _, _ -> logError("温馨提示，需要权限来进行安装") })
    }

    override fun initView() {
        super.initView()

        binding.editUrl.apply {
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()

            //需要延时才会正常弹出
            postDelayed({ changeKeyboard(true) }, 500)
        }

    }

}