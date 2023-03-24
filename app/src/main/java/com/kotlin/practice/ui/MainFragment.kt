package com.kotlin.practice.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.kotlin.practice.BR
import com.kotlin.practice.MainPageViewModel
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseFragment
import com.kotlin.practice.databinding.FragmentMainBinding

/**
 * 描述:首页管理类
 * 功能介绍:首页管理类
 * 创建者:翁益亨
 * 创建日期:2023/1/9 14:00
 */
class MainFragment : BaseFragment<FragmentMainBinding, MainViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_main

    //返回实例名称：用于创建对应变量名称的实例
    override fun initVariableId(): Int = BR.vm

    //获取activity的共享数据内容 activityViewModels() 固定写法
    private val mainPageViewModel: MainPageViewModel by activityViewModels()

    override fun initView() {
        super.initView()

        var name = mainPageViewModel.name.value

        //绑定界面和导航器。切记：BottomNavigationView中menu设定的id一定要和navigation中设置的导航id一致！！！！！不然会出现点击无法跳转的问题
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_main_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navBottomView.setupWithNavController(navController)

        //初始化定位页面
//        binding.navBottomView.selectedItemId = R.id.mine_fragment

        //将原有的图片渲染规则重置，默认是使用系统主题颜色colorPrimary，使用自带的图标色彩就要取消自带的色彩属性
        binding.navBottomView.itemIconTintList = null

        //往tab上添加角标/悬浮徽章。默认是一个红点，需要自己添加对应的属性已达到对应的效果
        val badge = binding.navBottomView.getOrCreateBadge(R.id.mine_fragment).apply {
            number = 120
            badgeGravity = BadgeDrawable.TOP_END//显示的位置
            badgeTextColor = resources.getColor(R.color.white)
            maxCharacterCount = 3 //最多显示三位，超过99就显示99+
        }


        navController.addOnDestinationChangedListener { controller: NavController,
                                                        destination: NavDestination,
                                                        arguments: Bundle? ->
            if (R.id.mine_fragment == destination.id) {
                badge.clearNumber()
                badge.isVisible = false
//                binding.navBottomView.removeBadge(R.id.mine_fragment)//直接移除菜单上的角标控件
            } else if (R.id.home_fragment == destination.id) {
                badge.isVisible = true
            }
        }


        android.os.Handler().postDelayed(Runnable {
            toggleShowLoading(false)

        }, 1000)


    }


    override fun getLoadingTargetView(): View? = binding.varyLayout

    override fun initLoadingAnimStatus(): Boolean = false


}