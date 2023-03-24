package com.kotlin.practice.ui.settings.bean

import java.io.Serializable

/**
 * 描述:文件夹数据
 * 功能介绍:文件夹下会包括文件列表和文件夹列表
 * 创建者:翁益亨
 * 创建日期:2023/3/17 16:34
 */
class FolderBean : Serializable {

    //文件夹名称
    var folderName:String = ""
    //文件夹路径
    var folderPath:String = ""

    //文件夹集合
    var folderList: ArrayList<FolderBean> = ArrayList()

    //文件集合
    var fileList: ArrayList<FileBean> = ArrayList()

    constructor(folderName: String, folderPath: String) {
        this.folderName = folderName
        this.folderPath = folderPath
    }
}