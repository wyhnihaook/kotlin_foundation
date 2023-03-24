package com.kotlin.practice.util

import android.graphics.Outline
import android.text.SpannableString
import android.text.Spanned
import android.text.SpannedString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.EditText
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.zeekrlife.base.utils.setClickDebouncing

/**
 * 描述:DataBinding适配器（必须是静态类）
 * 功能介绍:拓展XML中的适配器属性
 * 创建者:翁益亨
 * 创建日期:2023/1/9 15:50
 */
object ViewAdapter {

    /**注意：XML的属性是按顺序，从上到下识别后执行的，有序！！！*/

    /**防抖点击事件，适配器一定要添加JvmStatic修饰*/
    //xml适配器，第一个参数默认为当前的view，后面的参数为需要适配的内容
    @BindingAdapter("android:onClickDebouncing")
    @JvmStatic fun setOnClickDebouncing(
        view: View,
        block: (Any) -> Unit) {
        view.setClickDebouncing {
            block(it)
        }
    }


    /**glide加载图片，这里对占位图可以设置默认图，默认url和placeholderRes默认数值。*/
    @BindingAdapter("android:url", "android:placeholderRes", "android:imageRadius", requireAll = false)
    @JvmStatic fun loadImage(imageView: ImageView, url:String?, placeholderRes:Int,imageRadius:Float = 0f){
        Glide.with(imageView)
            .load(url)
            .apply(RequestOptions().placeholder(placeholderRes))
            .into(imageView)

        if(imageRadius>0){
            //设置圆角
            imageView.outlineProvider = object:ViewOutlineProvider(){
                override fun getOutline(view: View?, outline: Outline?) {
                    outline?.setRoundRect(0,0,imageView.width,imageView.height,imageRadius)
                }
            }

            imageView.clipToOutline = true
        }

    }


    /**EditText设置对应的hint字体大小方法*/
    @BindingAdapter("android:hintSize")
    @JvmStatic fun hintSize(editText: EditText, textSize:Float){
        if(!TextUtils.isEmpty(editText.hint)){
            val ss = SpannableString(editText.hint)
            val ass = AbsoluteSizeSpan(textSize.toInt(),false)
            ss.setSpan(ass,0,editText.hint.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            editText.hint = SpannedString(ss)
        }
    }


}