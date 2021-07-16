package com.gwchina.parent.message.data

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-05-23 12:03
 */
interface MessageApi{

    /**消息中心*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/notification/show")
    fun loadMessageList(
            @Field("pos") pos: Int/*开始位置*/,
            @Field("limit") limit: Int/*数量*/
    ): Observable<HttpResult<List<Message>>>

    /**删除所有消息*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/notification/delete")
    fun deleteMessageList(): Observable<HttpResult<Unit>>

}