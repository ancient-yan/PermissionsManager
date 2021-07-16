package com.gwchina.parent.level.data

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-28 19:56
 */
interface LevelApi {

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/guard/items")
    @FormUrlEncoded
    fun loadGuardItem(
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<List<GuardLevel>>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/guard/set")
    fun setGuardLevel(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("guard_level_id") guard_level_id: String,
            @Field("guard_item_id_list") guard_item_id_list: String): Observable<HttpResult<Unit>>

}