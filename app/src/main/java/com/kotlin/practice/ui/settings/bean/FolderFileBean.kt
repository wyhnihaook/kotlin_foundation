package com.kotlin.practice.ui.settings.bean

/**
 * 描述:整合当前显示的数据
 * 功能介绍:点击之后重新根据完整的列表信息去匹配对应的内容
 * 创建者:翁益亨
 * 创建日期:2023/3/17 17:31
 */
class FolderFileBean {

    //文件或者文件夹的名称
    var name: String = ""

    //文件或者文件夹的路径
    var path: String = ""

    constructor(name: String, path: String) {
        this.name = name
        this.path = path
    }

    //以下两个数据需要通过数据来判断

    //表示当前是文件还是文件夹
    var isFolder:Boolean = false

    //判断是否是excel
    var isExcel:Boolean = false
}