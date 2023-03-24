package com.kotlin.practice.ui.database

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.db.AppDatabase
import com.kotlin.practice.db.user.User
import com.kotlin.practice.util.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 描述:查询数据结果
 * 功能介绍:查询数据结果
 * 创建者:翁益亨
 * 创建日期:2023/2/8 15:57
 */
class QueryDatabaseViewModel : BaseViewModel() {

    private val queryDatabaseRepo by lazy {
        QueryDatabaseRepo()
    }

    override val title: String
        get() = BaseApp.getContext().getString(R.string.query_database_page)

    val userList = mutableListOf<User>()

    /**
     * 获取数据库人员端口
     * @param block 用于界面上是否显示空占位
     * */
    fun getUserList(block:(Boolean)->Unit) {
        viewModelScope.launch {
            queryDatabaseRepo.getUserList().onStart {
                //开始执行 start Thread[main,5,main]
                logError("start ${Thread.currentThread()}")
            }.catch {
                //捕获异常
                logError("error ${Thread.currentThread()}")
            }.onCompletion {
                //结合数据库执行时，不会执行到这里，除非页面关闭或者取消当前的协程即可
                //完成数据的获取 onCompletion Thread[main,5,main]
                logError("onCompletion ${Thread.currentThread()}")
            }.collectLatest {
                //每次只要同步最新的数据
                userList.clear()
                userList.addAll(it)

                block.invoke(userList.size == 0)
            }
        }
    }
}