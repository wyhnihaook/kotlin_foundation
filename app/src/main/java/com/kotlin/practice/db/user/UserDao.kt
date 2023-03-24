package com.kotlin.practice.db.user

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 描述:数据库访问对象
 * 功能介绍:访问User实例的DAO
 * 增删改查实现（注意：SQLite中的表和列的名称，都不区分大小写！！）
 * 可观察对象的查询/Flow，在插入、删除、更新展示的数据时，会及时更新显示内容（参考官方：https://developer.android.google.cn/kotlin/flow?hl=zh-cn）
 * 由系统发出包含数据库新数据项的新列表
 *
 * 分析Flow为什么能及时通知
 * 1.在app目录下的build中查询编译过后的getAll方法的源码。示例：app/build/generated/source/kapt/debug/com.kotlin.practice/db/user/UserDao_Impl
 * 2.比较发现，getTotalCount和返回Flow的getAll方法的区别在于返回值。使用Flow的方法返回了CoroutinesRoom.createFlow
 * 3.查询CoroutinesRoom的createFlow方法描述。发现内部构建了channel，有一个遍历，一直循环直到有取消操作，等待数据的更新，更新后重新调用channel的send方法发送通知（channel中发送的都是最新的数据CONFLATED模式）
 * 4.最最最重要的是发送通知的触发点（onInvalidated的回调）数据库发生变化之后就会执行当前回调，再由channel发送通知，再发送到Flow中等待处理
 * PS：CoroutinesRoom的createFlow的channel创建出来之后，不调用close会一直阻塞等待处理发送的内容
 *
 * 创建者:翁益亨
 * 创建日期:2023/1/5 16:10
 */
@Dao
interface UserDao {
    //查询表中总个数
    @Query("SELECT count(*) FROM USER")
    fun getTotalCount():Int

    //查询，需要结合SQL确定查询结果，全量查询
    @Query("SELECT * FROM USER")
    fun getAll():Flow<List<User>>

    //批量查询学号
    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds:IntArray):Flow<List<User>>

    //查询单个用户信息。可能存在重复情况，这里不做考虑，直接检索最近的一个
    @Query("select * from user where name like :name and gender like :gender limit 1")
    fun findByName(name:String,gender:String): User

    //查询单个用户信息
    @Query("select * from user where uid like :id limit 1")
    fun findById(id:Int): User

    //支持批量新增
    @Insert
    fun insertAll(vararg users: User)

    //只支持单个删除
    @Delete
    fun delete(user: User)

    //更新数据，只能根据对应的主键做相同匹配
    //选择性地返回 int 值，该值指示成功更新的行数
    //OnConflictStrategy.REPLACE 冲突时替换为新记录
    //OnConflictStrategy.IGNORE 忽略冲突
    //OnConflictStrategy.ROLLBACK 废弃了，使用ABORT替代
    //OnConflictStrategy.FAIL 废弃了，使用ABORT替代
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(user:User):Int
}


//查询当前数据库内容方式
//1.导出存储的路径/data/data/应用的applicationId/databases目录下
//2.下载SQLiteStudio绿色软件，通过添加命名的数据库文件，然后点击对应的表名，点击数据查看列表内容