package com.kotlin.practice.ui.plan.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.practice.databinding.ItemFuturePlansBinding
import com.kotlin.practice.helper.recyclerview.PagingDataCustomAdapter
import com.kotlin.practice.ui.plan.bean.FuturePlansBean

/**
 * 描述:paging适配器
 * 功能介绍:继承于PagingDataAdapter<数据类型，ViewHolder适配器>(比较器/删除修改时会使用到)
 * 创建者:翁益亨
 * 创建日期:2023/2/21 14:59
 */
class FuturePlansAdapter(diffCallback: DiffUtil.ItemCallback<FuturePlansBean>):
    PagingDataCustomAdapter<FuturePlansBean, FuturePlansViewHolder>(diffCallback) {

    override fun onBindViewHolder(holder: FuturePlansViewHolder, position: Int) {
        holder.binding.data = getItem(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuturePlansViewHolder
    = FuturePlansViewHolder(ItemFuturePlansBinding.inflate(LayoutInflater.from(parent.context),parent,false))
}

class FuturePlansViewHolder(val binding:ItemFuturePlansBinding):RecyclerView.ViewHolder(binding.root)