package com.kotlin.practice.util

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat.getInsetsController
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.kotlin.practice.base.BaseApp

/**
 * 描述:状态栏处理
 * 功能介绍:状态栏处理
 * 创建者:翁益亨
 * 创建日期:2023/2/14 14:21
 */

/**状态栏高度获取*/
val Activity.statusBarHeight: Int
    get() = ViewCompat.getRootWindowInsets(window.decorView)?.getInsets(WindowInsetsCompat.Type.statusBars())?.top
        ?: BaseApp.getContext().resources.getIdentifier("status_bar_height", "dimen", "android")
            .let { if (it > 0) BaseApp.getContext().resources.getDimensionPixelSize(it) else 0 }

/**状态栏的背景颜色设置*/
@setparam:ColorInt
inline var Activity.statusBarColor: Int
    get() = window.statusBarColor
    set(value) {
        window.statusBarColor = value
    }

/**显示或者隐藏顶部状态栏*/
fun Activity.changeStatusBars(isShow: Boolean = true){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (isShow){
            window?.insetsController?.show(WindowInsets.Type.statusBars())
        } else{
            window?.insetsController?.hide(WindowInsets.Type.statusBars())
        }
    } else {
        getInsetsController(window, window.decorView).let { controller ->
            if (isShow){
                controller?.show(WindowInsetsCompat.Type.statusBars())
            } else{
                controller?.hide(WindowInsetsCompat.Type.statusBars())
            }
        }
    }
}

/**
 * 改变状态栏文字颜色
 * 只有2种选择,白色与黑色
 */
fun Activity.changeStatusFountColor(isLight: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        //android R (android 11, API 30) 使用下面的新api
        /*
         传入0则是清理状态,恢复高亮
         */
        val state = if (isLight) WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS else 0
        window?.insetsController?.setSystemBarsAppearance(state, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
    } else {
        //低于android R 使用兼容模式
        getInsetsController(window, window.decorView).let { controller ->
            controller?.isAppearanceLightStatusBars = isLight
        }
    }
}

/**
 * 显示输入法，必须页面存在EditText并且焦点聚焦的情况才生效
 * */
fun Fragment.changeKeyboard(isShow: Boolean){
    activity?.changeKeyboard(isShow)
}
fun Activity.changeKeyboard(isShow: Boolean){
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (isShow){
            window?.insetsController?.show(WindowInsets.Type.ime())
        } else{
            window?.insetsController?.hide(WindowInsets.Type.ime())
        }
    } else {
        getInsetsController(window, window.decorView).let { controller ->
            if (isShow){
                controller?.show(WindowInsetsCompat.Type.ime())
            } else{
                controller?.hide(WindowInsetsCompat.Type.ime())
            }
        }
    }
}

