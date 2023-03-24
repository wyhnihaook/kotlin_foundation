package com.kotlin.practice.ui.database


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseActivity
import com.kotlin.practice.databinding.ActivityQueryDatabaseBinding
import com.kotlin.practice.databinding.ItemUserBinding
import com.kotlin.practice.db.user.User
import com.kotlin.practice.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 描述:查看数据库内容结果
 * 功能介绍:查看数据库内容结果
 * 创建者:翁益亨
 * 创建日期:2023/2/8 15:57
 */
class QueryDatabaseActivity :BaseActivity<ActivityQueryDatabaseBinding,QueryDatabaseViewModel>(){
    override fun getLayoutId(): Int = R.layout.activity_query_database

    override fun initVariableId(): Int = BR.vm

    override fun getLoadingTargetView(): View? = binding.pager

    override fun initParams() {
        super.initParams()

        viewModel.getUserList{
            toggleShowEmpty(it)
            if(!it){
                binding.pager.adapter?.apply {
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun initView() {
        super.initView()

        binding.pager.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DatabaseAdapter(viewModel.userList)

            toggleShowEmpty(viewModel.userList.size == 0)
        }


    }


    class DatabaseAdapter(private var userList:MutableList<User>) : RecyclerView.Adapter<DatabaseViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DatabaseViewHolder =
            DatabaseViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context),parent,false))

        override fun onBindViewHolder(holder: DatabaseViewHolder, position: Int) {
            holder.binding.user = userList[position]
        }

        override fun getItemCount(): Int = userList.size
    }

    class DatabaseViewHolder(var binding:ItemUserBinding):RecyclerView.ViewHolder(binding.root)
}