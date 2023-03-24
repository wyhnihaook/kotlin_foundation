package com.kotlin.practice.ui.mine

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.kotlin.practice.R
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.util.logError
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

/**
 * 描述:我的页面数据绑定类
 * 功能介绍:我的页面数据绑定
 * 创建者:翁益亨
 * 创建日期:2023/2/6 14:50
 */
class MineViewModel : BaseViewModel() {


    private val mineRepo by lazy { MineRepo() }

    var userData = MutableLiveData<MineUserData>()

    //获取网络数据之后进行渲染
    fun getUserInfo() {
        launch {
            //方式一：顺序执行获取结果，适用于没有其他交互，只有异步请求的情况
//            val userInfo = mineRepo.getUserInfoSpare()
//            userData.value = userInfo

            //方式二：将网络请求的执行放在flow模块中处理。链式请求可以穿插实现请求前后的额外逻辑，适用于复杂的逻辑操作（推荐）
            mineRepo.getUserInfo().onStart {
                //开始执行：Thread[main,5,main]
            }.onCompletion {
                //完成处理：Thread[main,5,main]
            }.collect {
                userData.value = it
            }

        }
    }


    /**查看数据库数据*/
    fun operateDatabase(any: Any) {
        (any as View).findNavController().navigate(R.id.mine_to_operate_database_page)
    }

    /**查看数据库数据*/
    fun viewData(any: Any) {
        (any as View).findNavController().navigate(R.id.mine_to_query_database_page)
    }


    /**切换主题*/
    fun switchThemes(any: Any) {
        (any as View).findNavController().navigate(R.id.mine_to_theme_fragment)
    }

    /**关于我们*/
    fun aboutUs(any: Any) {
        (any as View).findNavController().navigate(R.id.mine_to_about_us_page)
    }

    /**设置*/
    fun settings(any: Any) {
        (any as View).findNavController().navigate(R.id.mine_to_settings_page)
    }
}