package com.kotlin.practice.ui.settings

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.base.application
import com.kotlin.practice.ui.settings.bean.FileBean
import com.kotlin.practice.ui.settings.bean.FolderBean
import com.kotlin.practice.ui.settings.bean.FolderFileBean
import com.kotlin.practice.util.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * 描述:文件管理器
 * 功能介绍:文件管理器
 * 创建者:翁益亨
 * 创建日期:2023/3/17 15:47
 */
class SettingsViewModel : BaseViewModel() {

    override val title: String
        get() = application.getString(R.string.file_manager_page)

    val directoryPath = Environment.getExternalStorageDirectory().absolutePath

    //当前的目录路径信息，默认为null，表示当前为根目录
    val currentFilePath = MutableLiveData<String>()

    //过滤出当前应该显示的内容信息
    val filterCurrentFileData = MutableLiveData<List<FolderFileBean>>()

    //注意：！！！！这里需要开启sd卡权限才会读取，不然目录无法读取！！！！
    //注意：！！！！这里是耗时操作，需要配合协程使用！！！！
    init {

        getFile(directoryPath)
    }


    /**
     * @param path 路径信息
     * */
    fun getFile(path:String){

        launch {

            //切换到子线程
            withContext(Dispatchers.IO) {
                //初始化时查询sd卡信息
                val directory = File(path)

                logError("path:${directory.absoluteFile}")

                //只有sd卡存在的情况才处理
                if (directory != null && directory.exists()) {
                    val files: Array<File> = directory.listFiles()

                    if (files != null && files.isNotEmpty()) {
                        //返回数组有效的长度
                        //内存根目录结构创建
                        currentFilePath.postValue(directory.absolutePath)
                        var listData = ArrayList<FolderFileBean>()

                        for (i in files.indices) {

                            //过滤出隐藏文件。界面上不提供用户查看
                            if (files[i].isDirectory) {
                                //文件夹的情况,需要继续遍历

                                logError("${files[i].isHidden}：文件夹名称：${files[i].name}")

                                if (!files[i].isHidden) {
                                    //这一层的文件夹信息保存
                                    var folderItem = FolderFileBean(files[i].name,files[i].absolutePath)
                                    folderItem.isFolder = true

                                    listData.add(folderItem)
                                }
                            } else {
                                //文件的情况
                                logError("${files[i].isHidden}：文件名称：${files[i].name}")
                                if (!files[i].isHidden) {
                                    var fileItem = FolderFileBean(files[i].name,files[i].absolutePath)
                                    fileItem.isExcel =(files[i].name.endsWith("xls")||files[i].name.endsWith("xlsx"))
                                    listData.add(FolderFileBean(name = files[i].name,path = files[i].absolutePath))
                                }
                            }
                        }

                        filterCurrentFileData.postValue(listData)

                    } else {
                        //内存中没有可识别的文件或者文件夹
                    }

                } else {
                    //内存中不存在，提示异常信息
                }

            }

            //主线程执行回调内容，上述子线程内容执行完毕后才会执行到这里
            logError("${Thread.currentThread()}")
        }
    }

}