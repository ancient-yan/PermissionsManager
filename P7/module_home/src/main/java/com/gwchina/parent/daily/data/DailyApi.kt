package com.gwchina.parent.daily.data

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-13 11:44
 */
interface DailyApi {


    /**日记流首页数据**/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/lifecircle/show")
    fun dailyRecord(
            @Field("pos") pos: Int,
            @Field("limit") limit: Int
    ): Observable<HttpResult<DailyRecord>>

    /**回复日记**/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/lifecircle/add/comment")
    fun replyDaily(
            @Field("life_record_id") life_record_id: String,
            @Field("reply_comment_id") reply_comment_id: String = "",
            @Field("content") content: String
    ): Observable<HttpResult<CommentResult>>

    //消息列表
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/lifecircle/notice/message")
    fun dailyMessageList(
            @Field("pos") page: Int,
            @Field("limit") pageSize: Int
    ): Observable<HttpResult<List<DailyMessageListBean>>>

    //保存日记
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/lifecircle/write")
    @FormUrlEncoded
    fun publishDaily(@Field("rec_user_id") rec_user_id: String, @Field("photo_paths") photo_paths: String, @Field("content") content: String): Observable<HttpResult<String>>

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/lifecircle/delete")
    @FormUrlEncoded
    fun deleteDaily(@Field("life_record_id") life_record_id: String): Observable<HttpResult<String>>
}