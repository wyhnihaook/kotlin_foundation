package com.kotlin.practice.db.convert

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 描述:转换器
 * 功能介绍:将list转化为string存储
 * 创建者:翁益亨
 * 创建日期:2023/8/24 11:45
 */
class InspectionConverter {
    @TypeConverter
    fun stringToObject(value:String): List<String> {
        val listType = object : TypeToken<List<String>>(){}.type
        return Gson().fromJson(value,listType)
    }

    @TypeConverter
    fun objectToString(list:List<Any>):String{
        val gson = Gson()
        return gson.toJson(list)
    }
}