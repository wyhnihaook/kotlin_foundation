package com.kotlin.practice.ui.rank

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import com.kotlin.practice.base.BaseViewModel
import com.kotlin.practice.util.logError


/**
 * 描述:排行榜适配器
 * 功能介绍:列表数据网络请求获取
 * 创建者:翁益亨
 * 创建日期:2023/1/9 15:06
 */
class RankViewModel :BaseViewModel() {

    //数据获取类
    private val repository by lazy { RankRepo() }


    var count = MutableLiveData(0)

     fun onClickAddCount(any: Any){
         count.postValue(count.value as Int+1)

         //这里的any只要绑定的是onClickDebouncing属性上的内容，any返回的就一定是view，可以不做判断，一定会返回
         any?.let {
             if(any is View){
                 //通过获取的view跳转页面
//                 any.findNavController().navigate(R.id.rank_to_mine_fragment)//返回上一个页面
             }
         }
     }

    var onClickBack = View.OnClickListener {
        view->

        //navigateUp返回当前起始的fragment后将不再响应
        view.findNavController().navigateUp()//返回上一个页面
    }


    /**
     * 获取网络数据
     * */

    fun getRankMusic(any:Any) {

        launch {
            //使用LiveData持有数据结果，直接更新到界面上
            var musicBean = repository.getRankMusic()
            logError("GET:$musicBean")
        }
    }

    fun getMusic(any:Any){

        launch {
            var musicBean = repository.getMusic()
            logError("POST:$musicBean")
        }
    }
}