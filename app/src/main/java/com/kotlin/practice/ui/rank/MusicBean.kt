package com.kotlin.practice.ui.rank

import java.io.Serializable

/**
 * 描述:音乐实例
 * 功能介绍:音乐实例
 * 创建者:翁益亨
 * 创建日期:2023/1/11 16:48
 */
class MusicBean :Serializable{
    var name :String? = null

    var url :String? = null

    var picurl :String? = null

    var artistsname :String? = null

    override fun toString(): String {
        return "name:$name,url:$url,picurl:$picurl,artistsname:$artistsname"
    }
}