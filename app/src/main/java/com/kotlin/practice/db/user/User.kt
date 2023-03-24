package com.kotlin.practice.db.user

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * 描述:Room数据库用户信息类
 * 功能介绍:本地数据库用户信息存储
 * User 的每个实例都代表应用数据库中 user 表中的一行
 * 创建者:翁益亨
 * 创建日期:2023/1/5 16:08
 */
//可以在Entity中定义复合主键, primaryKeys = ["name","gender"]，！！推荐还是自定义一个唯一标识符用来更新对应的数据！！
@Entity(tableName = "user")//设置表名，默认使用类名，无大小写要求
data class User(
    @PrimaryKey(autoGenerate = true) var uid:Int,//主键，必须要存在（必须保证唯一性）->如果您需要 Room 为实体实例分配自动 ID，请将 @PrimaryKey 的 autoGenerate 属性设为 true
    @ColumnInfo(name = "name") //不写则使用变量名做为列名
    var name:String?,
    @ColumnInfo(name = "gender") var gender:String?,
    @ColumnInfo(name = "address") var address:String? = null ,

    @Ignore val birthday:String?  = null//不将字段存储/映射到数据库中
){
    //存在Ignore的字段，需要重新构建一个对应个数的构造函数，用于数据库映射。不然数据库中四个字段无法对应默认五个字段的构造器
    //内容初始化即可，数据库对应数据会做映射重新在实例上赋值
    constructor(name: String?,gender: String?,address: String?):this(0,name,gender,address,null)

}

//升迁数据库，添加字段，添加表结构
//1.自动升级版本（推荐，自动生成代码，不管是文件新增还是字段新增都能很好适配）
//1.1在app的build.gradle中添加arguments属性，设置生成的路径
//1.2在AppDatabase文件中，设置exportSchema=true（默认是false）,添加autoMigrations=[AutoMigration(from = 2, to = 3)]，from和to是在原基础上存在1.1生成的文件依次升级
//1.3升级版本号

//2.手动升级版本
//2.1创建Migration执行更改的SQL命令
//2.2 exportSchema=false，并且在打开数据库时，添加addMigrations优先执行SQL
//2.3升级版本号