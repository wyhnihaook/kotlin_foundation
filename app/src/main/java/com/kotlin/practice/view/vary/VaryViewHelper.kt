package com.kotlin.practice.view.vary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * 描述:实现功能接口
 * 功能介绍:对外提供实现类功能
 * 创建者:翁益亨
 * 创建日期:2023/1/4 18:15
 */
class VaryViewHelper(var viewSource: View) : IVaryViewHelper {

    //布局信息同步原占位组件
    private val params by lazy {
        viewSource.layoutParams
    }

    private lateinit var parentView:ViewGroup

    //当前展示的View内容
    private lateinit var currentView:View

    //当前替代元素的位置
    private var viewIndex:Int = 0

    init {
        initParams()
    }

    private fun initParams(){
        parentView = if(viewSource.parent != null){
            //发现上一层视图
            viewSource.parent as ViewGroup
        }else{
            //发现顶层视图，顶层视图的id都是content
            viewSource.rootView.findViewById(android.R.id.content)
        }

        val count = parentView.childCount

        for(i in 0..count){
            if(viewSource == parentView.getChildAt(i)){
                viewIndex = i
                break
            }
        }

        currentView = viewSource
    }

    override fun getCurrentLayout(): View = currentView

    override fun restoreView() {
        showLayout(viewSource)
    }

    override fun showLayout(view: View) {
        if(parentView == null){
            initParams()
        }

        currentView = view
        //判断两个view是否相同，如果不相同，就进行替换
        if(parentView.getChildAt(viewIndex) != view){
            //首先判断当前布局是否已经存在于其他组件/容器中，如果存在就先移除（理论上不存在这种情况）
            var parent = view.parent
            parent?.run { (this as ViewGroup).removeView(view) }

            //移除原容器中的对应占位内容后再添加数据
            parentView.apply {
                this.removeViewAt(viewIndex)
                this.addView(view,viewIndex,params)
            }


        }
    }

    override fun inflate(layoutId: Int): View = LayoutInflater.from(viewSource.context).inflate(layoutId,null)

    override fun getContext(): Context = viewSource.context

    override fun getView(): View = viewSource
}