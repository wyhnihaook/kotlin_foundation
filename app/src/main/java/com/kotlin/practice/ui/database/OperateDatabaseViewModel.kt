package com.kotlin.practice.ui.database

import android.content.Context
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseApp
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.db.AppDatabase
import com.kotlin.practice.db.user.User
import com.kotlin.practice.util.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 描述:操作数据库
 * 功能介绍:操作数据库
 * 创建者:翁益亨
 * 创建日期:2023/2/8 15:59
 */
class OperateDatabaseViewModel:BaseViewModel() {

    override val title: String
        get() = BaseApp.getContext().getString(R.string.operate_database_page)

    //每次操作完之后，显示操作结果
    val operateResult = MutableLiveData<String>()

    val editName = MutableLiveData<String>()

    val editGender = MutableLiveData<String>()

    val editAddress = MutableLiveData<String>()

    private val db by lazy{
        AppDatabase.getInstance()
    }


    /**新增用户*/
    fun addUser(any: Any){
        operateDatabase((any as View).context,0){
            var user = User(editName.value,editGender.value,editAddress.value)

            db.userDao().insertAll(user)

            operateResult.postValue("成功插入用户信息：$user")
        }
    }


    /**更新用户地址，这里简单处理更新用户地址*/
    fun update(any:Any){
        operateDatabase((any as View).context,1){
            var user = db.userDao().findByName(editName.value!!,editGender.value!!)

            user.address = editAddress.value

            db.userDao().update(user)

            operateResult.postValue("成功更新用户信息：$user")
        }
    }

    /**删除用户*/
    fun delete(any:Any){
        operateDatabase((any as View).context,2){
            var user = db.userDao().findByName(editName.value!!,editGender.value!!)

            db.userDao().delete(user)

            operateResult.postValue("成功删除用户信息：$user")
        }
    }



    /**
     * @param type 0:插入用户 1:更新用户 2:删除用户
     * */
    private fun operateDatabase(context:Context,type:Int,block:()->Unit){
        if(TextUtils.isEmpty(editName.value) ||
            TextUtils.isEmpty(editGender.value)){

           context.toast("${
               when(type){
                   0->"插入"
                   1->"更新"
                   else->"删除"
               }
           }用户数据必填项不能为空")
            return
        }

        launch {
            withContext(Dispatchers.IO){
                block()
            }
        }
    }
}