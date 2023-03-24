package com.kotlin.practice.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.kotlin.practice.view.vary.VaryViewHelperController
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 描述:基础类
 * 功能介绍:Fragment基类
 * 创建者:翁益亨
 * 创建日期:2023/1/9 13:47
 */
abstract class BaseFragment<BD : ViewDataBinding, VM : BaseViewModel> : Fragment() {
    /**
     * 当前的view的控制界面显示---->网络异常,网络过慢,请求空数据显示
     */
    private var mVaryViewHelperController: VaryViewHelperController? = null

    /**
     * 当前绑定界面组件管理类
     *
     * 需要在onDestroyView生命周期内对 view 的引用变量设置为 null，让View被回收
     * */
    private var _binding: BD? = null

    //兼容初始化时页面上主题变化回收，binding未初始化的情况（只在第一个渲染的页面中处理即可）
    protected fun getRealBinding():BD? = _binding

    protected val binding get() = _binding!!

    /**
     * 当前绑定业务和数据管理类
     * */
    protected lateinit var viewModel: VM


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        binding.lifecycleOwner = this
        getLoadingTargetView()?.let {
            mVaryViewHelperController = VaryViewHelperController(getLoadingTargetView()!!)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var type: Type = this::class.java.genericSuperclass
        var modelClass = (type as ParameterizedType).actualTypeArguments[1]
        viewModel = ViewModelProvider(this).get(modelClass as Class<VM>)

        //在布局中通过variable属性，关联ViewModel，将内部数据源和对应的ui界面进行高级绑定
        //这里声明的内容是将新建的viewModel对象绑定到布局文件中相同名称的内容上去。不然布局中没有对应的实例去支持数据的渲染
        //在代码中等同于执行  binding.名称 = viewModel实例
        binding.setVariable(initVariableId(), viewModel)

        //viewModel设置生命周期监听挂钩
        //让ViewModel拥有View的生命周期感应
        lifecycle.addObserver(viewModel)

        //数据初始化
        var executeStatus = viewModel.executeInitData(::initParams)

        initView()

        //判断是否实现过对应占位内容，如果子类实现了就初始化控制器
        getLoadingTargetView()?.let {
            mVaryViewHelperController = VaryViewHelperController(it)
        }

        //如果需要默认的占位，就进行占位替换展示（前提：必须是初始化加载的情况）
        if (initLoadingAnimStatus()&&executeStatus) {
            toggleShowLoading(true)
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding?.let {
            it.unbind()
            _binding = null
        }
    }

    /**
     * fragment跳转控制器获取，退出页面/跳转下一个页面功能
     */
    protected fun nav(): NavController {
        return NavHostFragment.findNavController(this)
    }

    //占位内容声明--------

    /**
     * @param toggle true：切换到占位布局 false：还原原有展示内容
     * */
    protected fun toggleShowLoading(toggle: Boolean, loadingMsg: String? = null) {
        if (mVaryViewHelperController == null) {
            //没有初始化占位布局时，不做操作
            return
        }

        if (toggle) {
            mVaryViewHelperController!!.showLoading()
        } else {
            mVaryViewHelperController!!.restore()
        }
    }


    //提供可以被复写的方法---------------

    /**
     * 需要被替代的布局内容
     * @return 默认不实现，返回null
     * */
    open fun getLoadingTargetView(): View? = null

    /**
     * 默认是否需要开启加载动画（注意！！当前方法返回true生效的前提需要复写getLoadingTargetView）
     * @return 默认不开启进入加载动画
     * */
    open fun initLoadingAnimStatus(): Boolean = false

    /**
     * 初始化时执行的操作（关联view操作相关：点击事件）
     * */
    open fun initView() {}

    /**
     * 初始化时执行的赋值操作（viewModel关联的参数初始化，避免二次初始化）
     * */
    open fun initParams() {}

    //子类必须要复写返回的布局Id
    open abstract fun getLayoutId(): Int

    //子类必须要复写返回的是绑定UI的id
    //绑定库在模块包中生成一个名为 BR 的类，其中包含用于数据绑定的资源的 ID。该库自动生成 BR.<布局中的variable的name>
    open abstract fun initVariableId(): Int

}