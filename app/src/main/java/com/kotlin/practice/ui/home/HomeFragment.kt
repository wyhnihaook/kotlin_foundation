package com.kotlin.practice.ui.home

import android.Manifest
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseFragment
import com.kotlin.practice.databinding.*
import com.kotlin.practice.util.launchX
import com.kotlin.practice.util.logError
import com.kotlin.practice.util.registerForPermissionsResult
import com.youth.banner.adapter.BannerImageAdapter
import com.youth.banner.holder.BannerImageHolder

/**
 * 描述:首页
 * 功能介绍:首页信息
 * 创建者:翁益亨
 * 创建日期:2023/2/1 17:24
 */

data class HomeRecommendData(var name:String = "",var url :String = "",var picurl :String = "",var artistsname :String = "")//推荐实体类

class HomeFragment :BaseFragment<FragmentHomeBinding,HomeViewModel>(){

    //照相机权限
    private lateinit var permissionCameraLauncher: ActivityResultLauncher<Array<String>>
    //下载存储权限
    private lateinit var permissionDownloadMemoryLauncher: ActivityResultLauncher<Array<String>>



    companion object{
        //类型声明
        @JvmStatic
        val BANNER = 0 // 轮播图
        @JvmStatic
        val FUNCTION_BLOCK = 1 //功能块
        @JvmStatic
        val HEAD_LINE = 2 //纯标题
        @JvmStatic
        val RECOMMEND_DATA = 3 //推荐的网络数据信息
    }

    override fun getLayoutId(): Int = R.layout.fragment_home

    override fun initVariableId(): Int = BR.vm

    //1.功能块、提供水平滑动的功能

    //构建水平轮播图，功能块，竖直列表
    override fun initParams() {
        super.initParams()

        viewModel.functionBlockList.add(HomeData(BANNER, mutableListOf(R.drawable.ic_home_banner1,R.drawable.ic_home_banner2,R.drawable.ic_home_banner3,R.drawable.ic_home_banner4)))

        viewModel.functionBlockList.add(HomeData(FUNCTION_BLOCK))

        viewModel.functionBlockList.add(HomeData(HEAD_LINE, title = "热门推荐"))

        //网络请求获取列表信息
        viewModel.getRecommendData{
            for(recommendData in viewModel.homeRecommendDataList){
                viewModel.functionBlockList.add(HomeData(RECOMMEND_DATA, recommendData = recommendData))
            }

            val bindingData =getRealBinding()
            bindingData?.let {
                //网络数据处理完成之后需要刷新页面数据
                binding.pager.adapter?.apply {
                    notifyDataSetChanged()
                }
            }

        }

    }

    override fun initView() {
        super.initView()

        //按照界面显示顺序添加点击事件的回调
        binding.pager.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = HomeAdapter(viewModel.functionBlockList)
        }

        permissionCameraLauncher = registerForPermissionsResult(
            onAllGranted = { binding.pager.findNavController().navigate(R.id.home_to_camera_activity)},
            onShowRequestRationale = {a,b,c->
                for(deny in b){
                    logError("拒绝,但是可以继续弹窗授权：$deny")
                }

                for(deny in c){
                    logError("所有拒绝权限：$deny")
                }
            },
            onDenied = {a,b->
                for(deny in b){
                    logError("拒绝,不能弹窗授权：$deny")
                }
            }
        )


        permissionDownloadMemoryLauncher = registerForPermissionsResult(
            onAllGranted = { binding.pager.findNavController().navigate(R.id.home_to_download_activity)},
            onShowRequestRationale = {a,b,c->
                for(deny in b){
                    logError("拒绝,但是可以继续弹窗授权：$deny")
                }

                for(deny in c){
                    logError("所有拒绝权限：$deny")
                }
            },
            onDenied = {a,b->
                for(deny in b){
                    logError("拒绝,不能弹窗授权：$deny")
                }
            }
        )
    }


    /**
     * 多适配器的创建
     * */

    //数据类的创建，用于区分类型排版
    data class HomeData(val type:Int,val imageUrls:MutableList<Int> = mutableListOf(),val title:String = "",val recommendData: HomeRecommendData = HomeRecommendData())

    inner class HomeAdapter(var homeDataList:MutableList<HomeData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
           return when(viewType){
                BANNER ->BannerViewHolder(ItemHomeBannerBinding.inflate(LayoutInflater.from(context),parent,false))
                FUNCTION_BLOCK->BlockViewHolder(ItemHomeFunctionBlockBinding.inflate(LayoutInflater.from(context),parent,false))
                RECOMMEND_DATA->RecommendViewHolder(ItemHomeRecommendListBinding.inflate(
                    LayoutInflater.from(context),parent,false))
                else-> HeadLineViewHolder(ItemHomeHeadLineBinding.inflate(LayoutInflater.from(context),parent,false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when(holder){
                is BannerViewHolder->{
                    //设置对应的数据内容
                    //这里处理的数据类型，可以是网络数据，修改Int类型为String即可
                    holder.binding.banner.setAdapter(object : BannerImageAdapter<Int>(homeDataList[position].imageUrls){
                        override fun onBindView(
                            p0: BannerImageHolder?,
                            p1: Int?,
                            p2: Int,
                            p3: Int) {
                            p0?.apply {
                                Glide.with(p0!!.imageView.context).load(p1).into(p0!!.imageView)
                            }
                        }
                    },true)

                    holder.binding.banner.setOnBannerListener{
                        data,position->
                        logError("点击轮播图：$position")
                        binding.pager.findNavController().navigate(R.id.home_to_webview_activity)
                    }
                }
                is HeadLineViewHolder->{
                    holder.binding.title = homeDataList[position].title
                }
                is RecommendViewHolder->{
                    holder.binding.data = homeDataList[position].recommendData
                }
            }
        }

        override fun getItemCount(): Int = homeDataList.size

        override fun getItemViewType(position: Int): Int = homeDataList[position].type


        //1.轮播图
        inner class BannerViewHolder(var binding: ItemHomeBannerBinding):RecyclerView.ViewHolder(binding.root){
            init {
                binding.banner.apply {
                    setBannerRound2(20f)
                    setLoopTime(4000)
                    setIndicator(binding.indicator,false)
                }
            }
        }
        //2.功能块
        inner class BlockViewHolder(var binding: ItemHomeFunctionBlockBinding):RecyclerView.ViewHolder(binding.root){
            init {
                binding.functionBlock.setClickCallback(::mineCamera,::mineTask,::comparePreviousYears,::futurePlans,::trainRecords)
            }

            /**
             * 方法块点击功能方法实现
             * */
            /**客户拍摄任务*/
            private fun mineCamera(){
                //照相机权限授权
                permissionCameraLauncher.launchX(arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ))

            }

            /**下载任务，实现下载本地功能，并分享*/
            private fun mineTask(){
                //存储权限申请
                permissionDownloadMemoryLauncher.launchX(arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ))
            }

            /**往年比较*/
            private fun comparePreviousYears(){
                binding.root.findNavController().navigate(R.id.home_to_other_activity)
            }

            /**未来计划*/
            private fun futurePlans(){
                binding.root.findNavController().navigate(R.id.home_to_future_plans_activity)
            }

            /**培训记录*/
            private fun trainRecords(){
                binding.root.findNavController().navigate(R.id.home_to_training_records_activity)
            }
        }
        //3.标题信息展示
        inner class HeadLineViewHolder(var binding:ItemHomeHeadLineBinding):RecyclerView.ViewHolder(binding.root)

        //4.列表展示信息
        inner class RecommendViewHolder(var binding:ItemHomeRecommendListBinding):RecyclerView.ViewHolder(binding.root)
    }

}