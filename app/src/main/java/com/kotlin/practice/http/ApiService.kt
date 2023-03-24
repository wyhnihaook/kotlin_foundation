package com.kotlin.practice.http

import com.kotlin.practice.ui.home.HomeRecommendData
import com.kotlin.practice.ui.mine.MineUserData
import com.kotlin.practice.ui.plan.bean.FuturePlansBean
import kotlinx.coroutines.flow.Flow
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * 描述:接口内容
 * 功能介绍:网络请求接口
 * 这里返回的固定是ApiResponse，返回的类型在获取时设定
 * 创建者:翁益亨
 * 创建日期:2023/1/11 14:24
 */
interface ApiService {

    /**
     * 获取列表数据
     * */
    @GET("/rank/list/{page}")
    suspend fun getRankList(@Path("page") page:Int):ApiResponse<String>

    /**
     * 未来计划获取
     * 请求url：/api/rand.music?sort=1&format=2
     * */
    @GET("/api/rand.music")
    suspend fun getFuturePlan(@Query("sort") type:String,
                            @Query("format") format:String): ApiResponse<FuturePlansBean>

    /**
     * 用户信息获取
     * 请求url：/api/rand.music?sort=1&format=2
     * */
    @GET("/api/rand.music")
    suspend fun getUserInfo(@Query("sort") type:String,
                             @Query("format") format:String): ApiResponse<MineUserData>

    /**
     * 音乐信息
     * 请求url：/api/rand.music?sort=1&format=2
     * */
    @GET("/api/rand.music")
    suspend fun getRankMusic(@Query("sort") type:String,
                          @Query("format") format:String):ApiResponse<MineUserData>


    /**
     * 音乐信息（表单格式提交 参数结合@Field/@FieldMap使用）
     * Content-Type=application/x-www-form-urlencoded
     * */
    @FormUrlEncoded
    @POST("/api/rand.music")
    suspend fun getMusic(@Field("sort")sort:String,
    @Field("format")format:String):ApiResponse<HomeRecommendData>


    /**
     * Content-Type=application/json
     * 注册（JSON提交 参数结合@Body使用）
     * RequestBody.create(null, new Gson().toJson(params))
     * */
    @POST("/user/register")
    suspend fun register(@Body body: RequestBody):ApiResponse<String>

    /**
     * 下载url路径下的文件
     * */
    @Streaming
    @GET
    suspend fun downloadPDF(@Url url:String)

}