package com.kotlin.practice.ui.plan

import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseActivity
import com.kotlin.practice.databinding.ActivityFuturePlansBinding
import com.kotlin.practice.helper.recyclerview.FooterAdapter
import com.kotlin.practice.helper.recyclerview.LinearItemDividerDecoration
import com.kotlin.practice.ui.plan.adapter.FuturePlansAdapter
import com.kotlin.practice.ui.plan.bean.FuturePlansBean
import com.kotlin.practice.util.logError
import com.kotlin.practice.util.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 描述:未来计划
 * 功能介绍:下拉刷新/上拉加载功能实现
 * 创建者:翁益亨
 * 创建日期:2023/2/21 14:25
 */
class FuturePlansActivity : BaseActivity<ActivityFuturePlansBinding, FuturePlansViewModel>() {

    private val futurePlansAdapter = FuturePlansAdapter(FuturePlansDiffCallback)

    override fun getLayoutId(): Int = R.layout.activity_future_plans

    override fun getLoadingTargetView(): View? = binding.refreshLayout

    override fun initVariableId(): Int = BR.vm

    override fun initView() {
        super.initView()

        binding.pager.apply {
            layoutManager = LinearLayoutManager(context)

            adapter = futurePlansAdapter.withLoadStateFooter(
                footer = FooterAdapter { futurePlansAdapter.retry() }
            )

            itemAnimator = null
            addItemDecoration(LinearItemDividerDecoration(context.getDrawable(R.drawable.list_line_divider)!!))
        }

        //刷新事件监听处理
        binding.refreshLayout.setOnRefreshListener {
            futurePlansAdapter.refresh()
        }

        //监听上拉加载的状态，这里发现footerAdapter内部渲染只有加载中、加载失败的监听。没有更多数据适配需要自定义实现
        futurePlansAdapter.addLoadStateListener {
            when (it.append) {
                is LoadState.Loading -> {
                    // 加载中 (加载数据时候回调)
                    logError("append loading")
                }
                is LoadState.Error -> {
                    //加载失败 （加载数据失败回调）
                    logError("append error")
                }
                is LoadState.NotLoading -> {
                    logError("append NotLoading:${it.append.endOfPaginationReached}")
                }
            }
        }

        //进行网络请求
        //RecyclerView 列表现在会显示来自数据源的分页数据，并会在必要时自动加载另一个页面。
        //submitData() 方法会挂起，并且直到 PagingSource 失效或调用适配器的刷新方法后才会返回。这意味着，submitData() 调用之后的代码的执行时间可能会显著晚于预期。
        lifecycleScope.launch {

            //方式一
//            viewModel.flow.collectLatest {
//                futurePlansAdapter.submitData(it)
//            }

            //方式二
            viewModel.flow2.observe(this@FuturePlansActivity) {

                futurePlansAdapter.submitData(lifecycle, it)
            }


            //适配器状态监听处理
            futurePlansAdapter.loadStateFlow.collectLatest { loadStates ->
                when (loadStates.refresh) {
                    !is LoadState.Loading -> {
                        //网络请求处理LoadResult完成回调,这里的adapter的itemCount是结合LoadResult.Page中的数据后的长度，这里可以处理页面返回的异常情况
                        if (binding.refreshLayout.isRefreshing) binding.refreshLayout.finishRefresh()
                        if (loadStates.refresh is LoadState.Error) {
                            //加载数据失败
                            toggleShowEmpty(true, "网络异常") {
                                futurePlansAdapter.refresh()
                            }

                        } else {
                            //从占位页面中恢复
                            toggleShowEmpty(false)
                        }
                    }
                }
            }
        }

    }

    //适配器所需的比较器。对item的数据做处理时才会使用到比较器
    object FuturePlansDiffCallback : DiffUtil.ItemCallback<FuturePlansBean>() {
        //检查两个item实例对象是否代表同一个项目（网络数据的一致性，一般用id标识）
        //如果项目内容相同，则标识同一个对象
        override fun areItemsTheSame(oldItem: FuturePlansBean, newItem: FuturePlansBean): Boolean {
            return oldItem.id == newItem.id
        }

        //检查两个项目是否具有相同的数据
        //此方法用于检测是否内容发生修改，可以用内存地址的引用或者复写toString比较内容是否完全一致
        //相同为true，否则为false
        override fun areContentsTheSame(
            oldItem: FuturePlansBean,
            newItem: FuturePlansBean
        ): Boolean {
            return oldItem.toString() == newItem.toString()
        }

    }

}