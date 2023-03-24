package com.kotlin.practice.base

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.kotlin.practice.R
import com.kotlin.practice.util.changeStatusFountColor
import com.kotlin.practice.util.context
import com.kotlin.practice.view.vary.VaryViewHelperController
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 描述:基础类
 * 功能介绍:基类功能抽取
 * 创建者:翁益亨
 * 创建日期:2023/1/5 10:26
 */
abstract class BaseActivity<BD : ViewDataBinding,VM : BaseViewModel> : AppCompatActivity() {

    /**
     * 当前的view的控制界面显示---->网络异常,网络过慢,请求空数据显示
     */
    private var mVaryViewHelperController: VaryViewHelperController? = null

    /**
     * 当前绑定界面组件管理类
     * */
    protected lateinit var binding: BD
    /**
     * 当前绑定业务和数据管理类
     * */
    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarAdaptation()

        var type: Type = this::class.java.genericSuperclass
        var modelClass = (type as ParameterizedType).actualTypeArguments[1]
        viewModel = ViewModelProvider(this).get(modelClass as Class<VM>)

        //绑定基础布局，可以直接通过binding.访问对应布局的组件
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        //将ViewDataBinding生命周期与Activity绑定
        binding.lifecycleOwner = this

        //在布局中通过variable属性，关联ViewModel，将内部数据源和对应的ui界面进行高级绑定
        //这里声明的内容是将新建的viewModel对象绑定到布局文件中相同名称的内容上去。不然布局中没有对应的实例去支持数据的渲染
        //在代码中等同于执行  binding.名称 = viewModel实例
        binding.setVariable(initVariableId(),viewModel)

        //viewModel设置生命周期监听挂钩
        //让ViewModel拥有View的生命周期感应
        lifecycle.addObserver(viewModel)

        //判断是否实现过对应占位内容，如果子类实现了就初始化控制器
        getLoadingTargetView()?.let {
            mVaryViewHelperController = VaryViewHelperController(it)
        }

        //数据初始化
        var executeStatus = viewModel.executeInitData(::initParams)

        initView()

        //如果需要默认的占位，就进行占位替换展示
        if(initLoadingAnimStatus()&&executeStatus){
            toggleShowLoading(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.let{
            it.unbind()
        }
    }

    /**获取当前渲染的主题色，适配状态栏颜色。注意这里联动values中的themes.xml中的backgroundColor属性*/
    private fun statusBarAdaptation(){
        val defaultThemeAttrs = context().obtainStyledAttributes(
            intArrayOf(
                R.attr.backgroundColor,
            )
        )

        //匹配背景色的颜色，然后进行文本亮色或者暗色的修改
        when(defaultThemeAttrs.getColor(0,0XFFFFFF)){
            //根据主题设置文本颜色

            ContextCompat.getColor(context(),R.color.white)->{
                //文本颜色设置为黑色
                changeStatusFountColor(true)
            }
            ContextCompat.getColor(context(),R.color.black)->{
                changeStatusFountColor(false)
            }
        }

        defaultThemeAttrs.recycle()
    }

    //占位内容声明--------

    /**
     * @param toggle true：切换到占位布局 false：还原原有展示内容
     * */
    protected fun toggleShowLoading(toggle:Boolean,loadingMsg:String? = null){
        mVaryViewHelperController?.apply {
            if(toggle){
               showLoading(loadingMsg)
            }else{
               restore()
            }
        }
    }


    /**
     * @param toggle true：切换到占位布局 false：还原原有展示内容
     * */
    protected fun toggleShowEmpty(toggle:Boolean,emptyDesc:String? = null,emptyPic:Int?  = null,block:()->Unit = {}){

        mVaryViewHelperController?.apply {
            if(toggle){
                showEmpty(emptyDesc,emptyPic,block)
            }else{
                restore()
            }
        }
    }



    //提供可以被复写的方法---------------

    /**
     * 需要被替代的布局内容
     * @return 默认不实现，返回null
     * */
    open fun getLoadingTargetView():View? = null

    /**
     * 默认是否需要开启加载动画（注意！！当前方法返回true生效的前提需要复写getLoadingTargetView）
     * @return 默认不开启进入加载动画
     * */
    open fun initLoadingAnimStatus():Boolean = false

    /**
     * 初始化执行代码块
     * */
    open fun initView(){}

    /**
     * 初始化时执行的赋值操作（viewModel关联的参数初始化，避免二次初始化）
     * */
    open fun initParams() {}

    //子类必须要复写返回的布局Id
    open abstract fun getLayoutId():Int

    //子类必须要复写返回的是绑定UI的id
    //绑定库在模块包中生成一个名为 BR 的类，其中包含用于数据绑定的资源的 ID。该库自动生成 BR.<布局中的variable的name>
    open abstract fun initVariableId():Int
}