package com.kotlin.practice.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.kotlin.practice.R
import com.kotlin.practice.util.MediaStoreFile

/**
 * 描述:ViewPager2的Fragment
 * 功能介绍:ViewPager2的Fragment
 * 创建者:翁益亨
 * 创建日期:2023/1/31 15:13
 */
class PhotoFragment internal constructor():Fragment() {

    //静态方法创建Fragment
    companion object{
        private const val FILE_NAME_KEY = "file_name"

        fun create(mediaStoreFile: MediaStoreFile) = PhotoFragment().apply {
            val image = mediaStoreFile.uri
            arguments = Bundle().apply {
                putString(FILE_NAME_KEY,image.toString())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = ImageView(context)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments?:return

        val resource = args.getString(FILE_NAME_KEY)?.let { it }?: R.drawable.ic_launcher_background

        view.setOnClickListener {
            view.findNavController().navigate(R.id.action_global_detailFragment)
        }

        Glide.with(view).load(resource).into(view as ImageView)
    }



}