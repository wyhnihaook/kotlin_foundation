package com.kotlin.practice.view.vary

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kotlin.practice.R
import com.zeekrlife.base.utils.setClickDebouncing

/**
 * 描述:控制界面占位的类
 * 功能介绍:处理占位内容的管理类，提供占位展示内容
 * 创建者:翁益亨
 * 创建日期:2023/1/5 9:56
 */
class VaryViewHelperController(viewSource: View) {

    //会和构造方法一起合并执行，所以能直接访问到对应的构造器中的元素
    private var helper:IVaryViewHelper = VaryViewHelper(viewSource)


    //展示占位布局内容
    //1.网络请求失败

    //2.异常情况

    //3.空数据占位
    fun showEmpty(emptyDesc:String? ,emptyPic:Int? ,block:()->Unit){
        var layout = helper.inflate(R.layout.view_empty)

        layout.findViewById<ImageView>(R.id.iv_empty_data).apply {
            setImageDrawable(ContextCompat.getDrawable(this.context,emptyPic?: R.drawable.ic_empty_data))
        }

        layout.findViewById<TextView>(R.id.tv_empty_data).text = emptyDesc?:"没有任何数据"


        layout.findViewById<View>(R.id.empty_container_view).setClickDebouncing {
            block()
        }
        helper.showLayout(layout)
    }

    //4.加载中
    //添加默认值，方法调用时可以不传递存在默认值的参数
    fun showLoading(loadingMsg:String? = null){
        var layout = helper.inflate(R.layout.view_loading)

        loadingMsg?.let {
            (layout).findViewById<TextView>(R.id.loading_msg).text = it
        }

        helper.showLayout(layout)
    }

    //恢复到原先展示的内容
    fun restore(){
        helper.restoreView()
    }


}