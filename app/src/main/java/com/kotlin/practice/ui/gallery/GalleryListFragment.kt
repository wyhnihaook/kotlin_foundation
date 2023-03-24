package com.kotlin.practice.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.practice.BR
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseFragment
import com.kotlin.practice.databinding.FragmentGalleryListBinding
import com.kotlin.practice.databinding.ItemPhotoBinding
import com.kotlin.practice.util.MediaStoreFile
import com.kotlin.practice.util.MediaStoreUtils
import com.kotlin.practice.helper.recyclerview.GridItemDividerDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 描述:图库列表信息展示
 * 功能介绍:图库信息
 * 创建者:翁益亨
 * 创建日期:2023/1/31 16:28
 */
class GalleryListFragment : BaseFragment<FragmentGalleryListBinding, GalleryListViewModel>() {

    private val mediaStoreUtils: MediaStoreUtils by lazy {
        MediaStoreUtils(requireContext())
    }

    override fun getLayoutId(): Int = R.layout.fragment_gallery_list

    override fun initVariableId(): Int = BR.vm

    override fun initParams() {
        super.initParams()

        lifecycleScope.launch {
            val images: MutableList<MediaStoreFile> = mediaStoreUtils.getImages()
            //设置数据源
            withContext(Dispatchers.Main) {
                this@GalleryListFragment.viewModel.images.addAll(images)
                //recyclerview数据适配
                binding.list.adapter?.apply {
                    //刷新数据，这里主要将页面上的数据进行展示
                    notifyDataSetChanged()
                }
            }
        }
    }


    override fun initView() {
        super.initView()

        //注意：这里不能使用BindingAdapter进行处理，因为界面渲染需要恢复状态，组件的初始化，只能写在生命周期中

        //直接在布局中进行声明
        binding.list.apply {
            //1.布局设置
            layoutManager = GridLayoutManager(requireContext(), 4)
            //2.设置适配器adapter
            adapter = ImageListAdapter(viewModel.images)

            //从主题中获取当前的颜色值，适配深色模式
            val defaultThemeAttrs =
                context.theme.obtainStyledAttributes(
                    intArrayOf(
                        android.R.attr.textColor,
                    )
                )
            //设置分割线
            addItemDecoration(
                GridItemDividerDecoration(
                    defaultThemeAttrs.getColor(0, 0XFFFFFF),
                    resources.getDimensionPixelOffset(R.dimen.dp_3)
                )
            )
        }
    }


    /**适配器设置*/
    class ImageListAdapter(var images: MutableList<MediaStoreFile>) :
        RecyclerView.Adapter<ImageListAdapter.ImageViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder =
        //attachToRoot生效前提：parent不为null，true给加载的布局指定parent父布局（默认）。false：需要parent手动调用addView才会添加到界面上，所有布局属性才会生效
            //生成的view，需要让适配器自己去添加，不能手动添加，所以这里一定要设置false。由RecyclerView中的ChildHelper进行addView
            ImageViewHolder(
                ItemPhotoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            //绑定数据源和界面。直接渲染
            holder.binding.vm = images[position]
        }

        override fun getItemCount(): Int = images.size

        inner class ImageViewHolder(var binding: ItemPhotoBinding) :
            RecyclerView.ViewHolder(binding.root) {
            init {
                //点击事件
                binding.image.setOnClickListener {
                    val action = GalleryListFragmentDirections.listToGalleryFragment(adapterPosition,images[adapterPosition].uri.toString())
                    it.findNavController().navigate(action)
                }
            }
        }
    }

}