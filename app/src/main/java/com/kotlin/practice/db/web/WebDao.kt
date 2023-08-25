package com.kotlin.practice.db.web

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 描述:操作数据库中的内容
 * 功能介绍:获取本地存储的html数据信息
 * 创建者:翁益亨
 * 创建日期:2023/8/24 10:44
 */
@Dao
interface WebDao {
    //查询表中总个数
    @Query("SELECT count(*) FROM WEB")
    fun getTotalCount():Int

    //查询，需要结合SQL确定查询结果，全量查询
    @Query("SELECT * FROM WEB")
    fun getAll(): MutableList<Web>

    //支持批量新增
    @Insert
    fun insertAll(vararg webs: Web)

    //只支持单个删除
    @Delete
    fun delete(web: Web)

    //批量删除数据
    @Query("delete from WEB where url in (:urls)")
    fun deleteUrlList(vararg urls:String)

    //更新数据，只能根据对应的主键做相同匹配
    //选择性地返回 int 值，该值指示成功更新的行数
    //OnConflictStrategy.REPLACE 冲突时替换为新记录
    //OnConflictStrategy.IGNORE 忽略冲突
    //OnConflictStrategy.ROLLBACK 废弃了，使用ABORT替代
    //OnConflictStrategy.FAIL 废弃了，使用ABORT替代
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(web:Web):Int

}

//查询当前数据库内容方式
//1.导出存储的路径/data/data/应用的applicationId/databases目录下
//2.下载SQLiteStudio绿色软件，通过添加命名的数据库文件，然后点击对应的表名，点击数据查看列表内容