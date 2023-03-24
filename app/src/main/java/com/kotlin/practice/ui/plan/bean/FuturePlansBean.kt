package com.kotlin.practice.ui.plan.bean

import androidx.annotation.Keep
import java.io.Serializable

/**
 * 描述:未来计划实体类
 * 功能介绍:未来计划实体类
 * @Keep避免混淆
 * 创建者:翁益亨
 * 创建日期:2023/2/21 14:39
 */
@Keep
class FuturePlansBean:Serializable {

    companion object{
        fun trans(futurePlansBean: FuturePlansBean): FuturePlansBean {
            futurePlansBean.apply {
                name = "未来计划：${futurePlansBean.name}"
            }

            return futurePlansBean
        }
    }
    var id:Int = 0

    var name :String? = null

    var url :String? = null

    var picurl :String? = null

    var artistsname :String? = null

    override fun toString(): String {
        return "name:$name,url:$url,picurl:$picurl,artistsname:$artistsname"
    }
}