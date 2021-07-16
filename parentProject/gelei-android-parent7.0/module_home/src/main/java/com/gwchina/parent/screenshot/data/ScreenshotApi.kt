package com.gwchina.parent.screenshot.data

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Flowable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-16 17:16
 */
interface ScreenshotApi {

    /**
     * 添加截屏
     */
    @FormUrlEncoded
    @POST("patriarch/printScreen/add")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun addScreenshot(
            @Field("c_user_id") child_user_id: String,
            @Field("c_device_id") child_device_id: String
    ): Flowable<HttpResult<ScreenshotResponse>>

    /**
     * 批量删除截屏图片
     */
    @FormUrlEncoded
    @POST("patriarch/printScreen/delList")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun deleteScreenshotList(
            @Field("recordId_list") child_user_id: String
    ): Flowable<HttpResult<String>>

    /**
     * 截屏图片列表
     */
    @FormUrlEncoded
    @POST("patriarch/printScreen/picList")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun screenshotPicList(@Field("c_device_id") device_id: String, @Field("c_user_id") c_user_id: String): Flowable<HttpResult<List<ScreenshotData>>>

    /**
     * 截屏数据统计
     */
    @FormUrlEncoded
    @POST("patriarch/printScreen/statistics")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun screenshotStatisticsData(@Field("c_user_id") c_user_id: String, @Field("c_device_id") c_device_id: String): Flowable<HttpResult<ScreenshotStatisticsData>>

    /**
     * 查看截屏详情
     */
    @FormUrlEncoded
    @POST("patriarch/printScreen/picDetail")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun screenshotPicDetail(@Field("record_id") record_id: String): Flowable<HttpResult<ScreenshotData>>
}