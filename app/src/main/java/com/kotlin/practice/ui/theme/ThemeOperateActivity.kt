package com.kotlin.practice.ui.theme

import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseActivity
import com.kotlin.practice.databinding.ActivityThemeOperateBinding
import com.kotlin.practice.databinding.ItemThemeBinding
import com.kotlin.practice.db.theme.AppTheme
import com.kotlin.practice.util.AppThemeKey
import com.kotlin.practice.util.ThemeMode
import com.kotlin.practice.util.ThemeUtils
import com.zeekrlife.base.utils.setClickDebouncing

/**
 * 描述:主题操作页面
 * 功能介绍:修改全局主题信息
 * 创建者:翁益亨
 * 创建日期:2023/2/7 15:25
 */
class ThemeOperateActivity : BaseActivity<ActivityThemeOperateBinding, ThemeOperateViewModel>() {

    override fun getLayoutId(): Int = R.layout.activity_theme_operate

    override fun initVariableId(): Int = BR.vm

    override fun initParams() {
        super.initParams()

        //查询状态后同步数据
        viewModel.queryThemeMode {
            //查询完成之后同步状态

            viewModel.themeList.add(
                ThemeOperateViewModel.ThemeData(
             viewModel.themeMode.value == ThemeMode.SYSTEM.mode||viewModel.themeMode.value == ThemeMode.UNKNOWN.mode,
                    ThemeMode.SYSTEM.themeName,ThemeMode.SYSTEM.mode
            ))

            viewModel.themeList.add(
                ThemeOperateViewModel.ThemeData(
                    viewModel.themeMode.value == ThemeMode.NIGHT.mode,
                    ThemeMode.NIGHT.themeName,ThemeMode.NIGHT.mode
                ))

            viewModel.themeList.add(
                ThemeOperateViewModel.ThemeData(
                    viewModel.themeMode.value == ThemeMode.LIGHT.mode,
                    ThemeMode.LIGHT.themeName,ThemeMode.LIGHT.mode
                ))

            binding.pager.adapter?.let {
                it.notifyDataSetChanged()
            }
        }

    }

    override fun initView() {
        super.initView()

        //显示主题切换列表信息
        binding.pager.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ThemeAdapter(viewModel.themeList)
        }
    }


    /**列表适配器*/
    inner class ThemeAdapter(var themeList:MutableList<ThemeOperateViewModel.ThemeData>) : RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder =
            ThemeViewHolder(ItemThemeBinding.inflate(LayoutInflater.from(parent.context),parent,false))

        override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
            holder.binding.data = themeList[position]
        }

        override fun getItemCount(): Int = themeList.size

        inner class ThemeViewHolder(var binding:ItemThemeBinding):RecyclerView.ViewHolder(binding.root){
            init {
                binding.root.setClickDebouncing {
                    //重写选择选中的角标
                    if(!binding.data!!.isCheck){
                        //将其他数据设置为false
                        for(data in themeList){
                            data.isCheck =  (data.themeMode == binding.data!!.themeMode)
                        }

                        when(binding.data!!.themeMode){
                            ThemeMode.SYSTEM.mode,ThemeMode.UNKNOWN.mode->{
                                ThemeUtils.systemTheme{
                                    viewModel.updateThemeMode(AppTheme(AppThemeKey,ThemeMode.SYSTEM.mode))
                                }
                            }
                            ThemeMode.NIGHT.mode->{
                                ThemeUtils.openDarkTheme{
                                    viewModel.updateThemeMode(AppTheme(AppThemeKey,ThemeMode.NIGHT.mode))
                                }
                            }
                            ThemeMode.LIGHT.mode->{
                                ThemeUtils.closeDarkTheme{
                                    viewModel.updateThemeMode(AppTheme(AppThemeKey,ThemeMode.LIGHT.mode))
                                }
                            }
                        }

                        notifyDataSetChanged()
                    }

                }
            }
        }
    }



}