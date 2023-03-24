package com.kotlin.practice.ui.camera

import android.content.Intent
import android.view.KeyEvent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseActivity
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.databinding.ActivityCameraBinding
import com.kotlin.practice.util.KEY_EVENT_ACTION
import com.kotlin.practice.util.KEY_EVENT_EXTRA

/**
 * 描述:照相机功能块
 * 功能介绍:整个功能块嵌入
 * 创建者:翁益亨
 * 创建日期:2023/2/10 10:03
 */
class CameraActivity :BaseActivity<ActivityCameraBinding,BaseViewModel>(){
    override fun getLayoutId(): Int = R.layout.activity_camera

    override fun initVariableId(): Int = BR.vm

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP -> {
                val intent = Intent(KEY_EVENT_ACTION).apply { putExtra(KEY_EVENT_EXTRA,keyCode) }
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }
}