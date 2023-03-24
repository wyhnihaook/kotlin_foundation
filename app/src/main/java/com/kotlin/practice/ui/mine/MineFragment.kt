package com.kotlin.practice.ui.mine

import com.google.android.material.appbar.AppBarLayout
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseFragment
import com.kotlin.practice.databinding.FragmentMineBinding
import com.kotlin.practice.helper.appbarlayout.AppBarLayoutStateChangeListener

/**
 * 描述:排行榜查看自身信息
 * 功能介绍:用户信息
 * 创建者:翁益亨
 * 创建日期:2023/1/10 15:02
 */
data class MineUserData(
    var id:Int =0,
    var name: String = "",
    var url: String = "",
    var picurl: String = "",
    var artistsname: String = ""
)//用户模拟信息

class MineFragment : BaseFragment<FragmentMineBinding, MineViewModel>() {

    override fun getLayoutId(): Int = R.layout.fragment_mine

    override fun initVariableId(): Int = BR.vm

    override fun initParams() {
        super.initParams()

        viewModel.getUserInfo()
    }

    override fun initView() {
        super.initView()

        //如果想要图片跟随事件变化，推荐重新自定义CollapsingToolbarLayout，使用canvas操作，不会对界面产生过大的负载
        //滚动，是否折叠监听
        binding.appbar.addOnOffsetChangedListener(object : AppBarLayoutStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State) {

            }

            override fun onStateOffset(offset: Int) {

            }

        })

    }
}