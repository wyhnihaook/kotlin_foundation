package com.kotlin.practice.ui.settings.bean

/**
 * 描述:文件数据
 * 功能介绍:文件数据
 * 创建者:翁益亨
 * 创建日期:2023/3/17 16:33
 */
class FileBean {

    //文件名称
    var fileName:String? = null

    //文件夹路径
    var filePath:String? = null

    constructor(fileName: String?, filePath: String?) {
        this.fileName = fileName
        this.filePath = filePath
    }
}