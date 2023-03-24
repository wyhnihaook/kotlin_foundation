package com.kotlin.practice.ui.gallery

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseFragment
import com.kotlin.practice.databinding.FragmentGalleryBinding
import com.kotlin.practice.util.MediaStoreFile
import com.kotlin.practice.util.MediaStoreUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 描述:图库显示
 * 功能介绍:ViewPager2使用
 * 创建者:翁益亨
 * 创建日期:2023/1/30 11:10
 */
class GalleryFragment: BaseFragment<FragmentGalleryBinding, GalleryViewModel>() {

    private val mediaStoreUtils:MediaStoreUtils by lazy {
        MediaStoreUtils(requireContext())
    }

    private val args: GalleryFragmentArgs by navArgs()

    override fun getLayoutId(): Int = R.layout.fragment_gallery

    override fun initVariableId(): Int = BR.vm

    override fun initParams() {
        super.initParams()

        viewModel.checkIndex = args.index
        viewModel.checkPicName = args.picName?:""

        lifecycleScope.launch {
           var images : MutableList<MediaStoreFile> = mediaStoreUtils.getImages()
            //设置数据源
            withContext(Dispatchers.Main){
                viewModel.images = images
                binding.pager.apply {
                    adapter?.let {
                        ( it as ImageAdapter).setMediaListAndNotify(viewModel.images)
                        setCurrentItem(viewModel.checkIndex,false)
                    }
                }
            }
        }
    }

    override fun initView() {
        super.initView()

        binding.pager.apply {
            offscreenPageLimit = 2
            adapter = ImageAdapter(childFragmentManager,viewModel.images)
        }

    }


    //ViewPager的适配器创建
    //设置内部类，绑定生命周期
    inner class ImageAdapter(fragment: FragmentManager,var imageList : MutableList<MediaStoreFile>):FragmentStateAdapter(fragment,lifecycle){
        override fun getItemCount(): Int = imageList.size

        override fun createFragment(position: Int): Fragment {
            //每个ViewPager中嵌入的Fragment页面信息
            return PhotoFragment.create(imageList[position])
        }

        //删除和添加时要进行复写，区分每一个Fragment唯一标识。和getItemId必须同时出现
        override fun containsItem(itemId: Long): Boolean {
            return null != imageList.firstOrNull { it.id == itemId }
        }
        //删除和添加时要进行复写，区分每一个Fragment唯一标识。和containsItem必须同时出现
        override fun getItemId(position: Int): Long {
            return imageList[position].id
        }

        fun setMediaListAndNotify(imageList: MutableList<MediaStoreFile>) {
            this.imageList = imageList
            notifyDataSetChanged()
        }
    }
}