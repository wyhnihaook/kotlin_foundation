package com.kotlin.practice.ui.settings

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.kotlin.practice.R
import com.kotlin.practice.databinding.ActivitySettingsBinding
import com.kotlin.practice.databinding.ItemFileManagerBinding
import com.kotlin.practice.ui.settings.bean.FolderBean
import com.kotlin.practice.ui.settings.bean.FolderFileBean
import com.kotlin.practice.util.context
import com.kotlin.practice.util.logError
import com.zeekrlife.base.utils.setClickDebouncing
import java.io.File

/**
 * 描述:本地文件管理器
 * 功能介绍:本地文件管理器（提供选择当前文件的功能）
 * 创建者:翁益亨
 * 创建日期:2023/3/17 15:44
 */
class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {

    private val mBinding by viewBinding(ActivitySettingsBinding::bind)

    private val mViewModel by viewModels<SettingsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding.titleInclude.text = mViewModel.title

        mBinding.titleInclude.onClick = View.OnClickListener {
            //判断当前文件是否存在父级，如果存在就返回父级目录，不存在就关闭页面
            if(!TextUtils.isEmpty(mViewModel.currentFilePath.value)){
                if(mViewModel.currentFilePath.value!! == mViewModel.directoryPath){
                    finish()
                }else{
                    val parentFile = File(mViewModel.currentFilePath.value).parentFile
                    //返回父级内容
                    mViewModel.currentFilePath.apply {
                        this.value = parentFile.absolutePath
                        mBinding.titleInclude.text = if(parentFile.absolutePath == mViewModel.directoryPath) getString(R.string.file_manager_page) else parentFile.name
                        mViewModel.getFile(this.value!!)
                    }
                }

            }else{
                finish()
            }
        }


        mBinding.rv.apply {
            layoutManager = LinearLayoutManager(context)
        }

        //这里的adapter要进行数据整合之后处理
        //每次点击要重新适配，所以这里在数据返回之后再进行处理

        mViewModel.filterCurrentFileData.observe(this) { t ->
            mBinding.rv.apply {
                adapter = FileManagerAdapter(t!!)
            }
        }


    }


    //adapter适配器内容
    inner class FileManagerAdapter(val data: List<FolderFileBean>) :
        RecyclerView.Adapter<FileManagerViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileManagerViewHolder =
            FileManagerViewHolder(
                ItemFileManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ).apply {
                itemView.setClickDebouncing {
                    //添加跳转到下一个文件夹
                    logError("${binding.data!!.name} :Name")
                    //遍历数据源结构，匹配当前的标题以及对应的路径，全匹配时进行数据替换
                    if(binding.data!!.isExcel){
                        //进行文件数据获取
                    }else if(binding.data!!.isFolder){
                        //跳转到下一个页面

                        mViewModel.currentFilePath.apply {
                            this.value = binding.data!!.path
                            mBinding.titleInclude.text = binding.data!!.name
                            mViewModel.getFile(this.value!!)
                        }
                    }
                }
            }

        override fun onBindViewHolder(holder: FileManagerViewHolder, position: Int) {

            var dataItem = data[position]

            holder.binding.data = dataItem

            if(dataItem.isFolder){
                holder.binding.logo.background = context().getDrawable(R.drawable.ic_folder_logo)
            }else if(dataItem.isExcel){
                holder.binding.logo.background = context().getDrawable(R.drawable.ic_excel)
            }else{
                holder.binding.logo.background = context().getDrawable(R.drawable.ic_file_logo)
            }
        }

        override fun getItemCount(): Int = data.size


    }

    class FileManagerViewHolder(val binding: ItemFileManagerBinding) :
        RecyclerView.ViewHolder(binding.root)

}