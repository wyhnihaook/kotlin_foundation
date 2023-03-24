package com.kotlin.practice.helper.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.practice.databinding.ViewNetworkStateFooterBinding
import com.kotlin.practice.util.logError
import com.zeekrlife.base.utils.setClickDebouncing

/**
 * 描述:全局底部上拉加载状态适配器
 * 功能介绍:根据需求添加类型即可，全局都使用该适配器进行展示
 * 创建者:翁益亨
 * 创建日期:2023/2/20 13:59
 */

/**
 * @param reload 对应适配器执行retry重新加载的方法
 * */
class FooterAdapter(val reload:()->Unit) : LoadStateCustomAdapter<NetworkStateViewHolder>() {
    //从LoadStateAdapter源码中发现，展示到页面的item情况从displayLoadStateAsItem入手，只有Loading和Error

    //页面内容适配
    override fun onBindViewHolder(holder: NetworkStateViewHolder, loadState: LoadState) {

        holder.binding.data = FooterBean().apply {
          isLoading = loadState.isLoadStateLoading
          noMore = loadState.endOfPaginationReached
          isError = loadState.isLoadStateError
        }

        //如果不执行下方代码刷新xml中的数据，布局中的逻辑内容将不会执行，所有view将暴露在页面上！！！！
        //当变量或可观察对象发生变化时，绑定(bind)被安排在下一帧之前发生变化。但是，有时必须立即执行绑定(bind)。要强制执行，请使用 executePendingBindings() 方法
        //调用该绑定时立即强制框架执行它到目前为止在绑定上需要做的所有事情
        holder.binding.executePendingBindings()

        logError("$loadState :loadState.isLoadStateLoading :${loadState.isLoadStateLoading} , " +
                "loadState.isLoadStateError:${loadState.isLoadStateError}" +
                " loadState.isLoadStateNotLoading:${loadState.isLoadStateNotLoading}")
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): NetworkStateViewHolder = NetworkStateViewHolder(ViewNetworkStateFooterBinding.inflate(
        LayoutInflater.from(parent.context),parent,false)).apply {
            binding.reload.setClickDebouncing {
                reload()
            }
    }


}


class NetworkStateViewHolder(val binding:ViewNetworkStateFooterBinding):RecyclerView.ViewHolder(binding.root)


 val LoadState.isLoadStateError:Boolean
    get() = this is LoadState.Error

inline val LoadState.isLoadStateLoading:Boolean
    get() = this is LoadState.Loading

inline val LoadState.isLoadStateNotLoading:Boolean
    get() = this is LoadState.NotLoading
