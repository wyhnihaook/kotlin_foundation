package com.kotlin.practice.ui.other

import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.paging.LoadState
import androidx.paging.cachedIn
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseFragment
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.databinding.FragmentOtherModulesBinding
import com.kotlin.practice.helper.recyclerview.FooterAdapter
import com.kotlin.practice.ui.mine.MineUserData
import com.kotlin.practice.util.logError
import com.zeekrlife.base.utils.setClickDebouncing
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 描述:功能块页面
 * 功能介绍:功能块页面，使用MotionLayout实现交互动画，上拉加载下拉刷新等功能
 * 创建者:翁益亨
 * 创建日期:2023/1/10 14:08
 */
class OtherModelsFragment : BaseFragment<FragmentOtherModulesBinding, OtherModelsViewModel>() {


    override fun getLayoutId(): Int = R.layout.fragment_other_modules

    override fun initVariableId(): Int = BR.vm


    override fun initView() {
        super.initView()

    }
}
