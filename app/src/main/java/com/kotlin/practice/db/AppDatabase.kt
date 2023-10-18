package com.kotlin.practice.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.db.book.Book
import com.kotlin.practice.db.book.BookDao
import com.kotlin.practice.db.relation.UserAndBook
import com.kotlin.practice.db.relation.UserAndBookDao
import com.kotlin.practice.db.theme.AppTheme
import com.kotlin.practice.db.theme.AppThemeDao
import com.kotlin.practice.db.user.User
import com.kotlin.practice.db.user.UserDao
import com.kotlin.practice.db.web.Web
import com.kotlin.practice.db.web.WebDao

/**
 * 描述:数据库
 * 功能介绍:提供操作数据库的方法
 * 数据库内部涵盖的数据表
 *
 * !!需要在子线程访问!!
 * 创建者:翁益亨
 * 创建日期:2023/1/5 16:30
 */
@Database(entities = [User::class, Book::class,AppTheme::class, Web::class],version = 10,

    exportSchema=true
    ,autoMigrations = [
        AutoMigration(from = 9, to = 10),
    ]

)
abstract class AppDatabase: RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun bookDao(): BookDao

    abstract fun userAndBookDao(): UserAndBookDao

    abstract fun appThemeDao():AppThemeDao

    abstract fun webDao():WebDao

    companion object{
        private const val databaseName = "database-name"

        @Volatile
        private var instance:AppDatabase? = null

        private fun buildDataBase(context: Context):AppDatabase{
            //第一次新建，会回调onCreate以及onOpen
            //第二次打开识别到已经存在数据库内容，就只会执行onOpen回调
            return Room
                .databaseBuilder(context,AppDatabase::class.java,databaseName)
                .addMigrations(MIGRATION_1_2,MIGRATION_7_8,MIGRATION_8_9,MIGRATION_9_10)
//                .fallbackToDestructiveMigration()//每次升级时，删除原有数据库内容
                .addCallback(object :RoomDatabase.Callback(){
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                    }

                })
                .build()
        }

        fun getInstance():AppDatabase{
            return instance?: synchronized(this){
                instance?:buildDataBase(BaseApp.getContext())
                    .also {
                        instance = it
                    }
            }
        }

        //版本迭代手动更新SQL（不推荐）-但是再迭代中新增的字段必须添加
        val MIGRATION_1_2 = Migration(1,2){
            //这里执行SQL进行对应表字段迭代处理
                database->
            //升级版本，其中添加了address 的关联
            database.execSQL("ALTER TABLE user add column address TEXT")//NOT NULL DEFAULT ''  标识当前内容存储时不能为空
        }

        //根据提示，设置开始版本，结束版本
        val MIGRATION_7_8 = Migration(6,8){
                database->
            database.execSQL("ALTER TABLE web add column htmlContent TEXT NOT NULL DEFAULT ''")//NOT NULL DEFAULT ''  标识当前内容存储时不能为空
        }


        //添加extraContent字段
        val MIGRATION_8_9 = Migration(8,9){
                database->
            database.execSQL("ALTER TABLE web add column extraContent TEXT NOT NULL DEFAULT ''")//NOT NULL DEFAULT ''  标识当前内容存储时不能为空
        }

        val MIGRATION_9_10 = Migration(9,10){
                database->
            database.execSQL("ALTER TABLE web add column eTag TEXT NOT NULL DEFAULT ''")//NOT NULL DEFAULT ''  标识当前内容存储时不能为空
        }

    }



}