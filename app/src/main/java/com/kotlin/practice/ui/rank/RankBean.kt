package com.kotlin.practice.ui.rank

import androidx.annotation.Keep
import java.io.Serializable

/**
 * 描述:排行榜基本信息
 * 功能介绍:页面间传递数据
 * @Keep避免混淆
 * 创建者:翁益亨
 * 创建日期:2023/1/10 13:33
 */
@Keep class RankBean : Serializable {

    var name:String? = null

    var ranking:Int? = 0

    override fun toString(): String {
        return "name:$name,ranking:$ranking"
    }


    companion object{
        /**
         * 网络接收的数据转化
         */

        fun trans(rankBean: RankBean):RankBean{
            var rankTransBean = RankBean()
            rankTransBean.name = rankBean.name
            rankTransBean.ranking = rankBean.ranking

            return rankTransBean
        }
    }
}