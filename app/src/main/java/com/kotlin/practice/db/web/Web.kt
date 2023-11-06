package com.kotlin.practice.db.web

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.kotlin.practice.db.convert.InspectionConverter

/**
 * 描述:本地缓存的数据内容
 * 功能介绍:存储网页链接（剔除携带的参数的完整链接） 以及 html中需要缓存的地址信息
 * 保证属性声明和构造函数中赋值的顺序，url->htmlContent 不能在构造函数中调换，不然对应的赋值也会出现调换
 * 创建者:翁益亨
 * 创建日期:2023/8/24 10:42
 */
@Entity(tableName = "web")
@TypeConverters(InspectionConverter::class)
data class Web(
    @PrimaryKey(autoGenerate = true) var uid:Int,//主键，必须要存在（必须保证唯一性）->如果您需要 Room 为实体实例分配自动 ID，请将 @PrimaryKey 的 autoGenerate 属性设为 true
    @ColumnInfo(name = "url") //不写则使用变量名做为列名，这里使用发起请求的url的协议头+host+path拼接作为唯一标识
    var url:String,

    var htmlContent:String,//html内容

    @ColumnInfo(name = "eTag", defaultValue = "")
    var eTag:String
) {
    constructor(url: String,htmlContent:String,eTag: String):this(0,url,htmlContent,eTag)
}