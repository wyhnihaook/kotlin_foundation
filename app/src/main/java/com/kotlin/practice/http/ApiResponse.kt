package com.kotlin.practice.http

import java.io.Serializable
import java.lang.IllegalStateException

/**
 * 描述:网络请求返回数据实体类
 * 功能介绍:基类信息
 * 创建者:翁益亨
 * 创建日期:2023/1/10 18:07
 */
class ApiResponse<V> : Serializable {
    //实际返回的可用数据结构
    var data: V?=null

    //错误toast
    var errorMsg:String = ""

    //业务错误码
    var code:Int = 1

    //内联特化，保证获取data结果为null时正确构造实际需要的内容
    inline fun <reified T> data():V{
        when(code){
            1,200->{
                if(data == null){
                    data = T::class.java.newInstance() as V
                }

                return data as V
            }

            //重新登录
            401->{
                return throw IllegalStateException("退出登录")
            }
        }

        //其他错误
        return throw IllegalStateException(errorMsg)
    }


}