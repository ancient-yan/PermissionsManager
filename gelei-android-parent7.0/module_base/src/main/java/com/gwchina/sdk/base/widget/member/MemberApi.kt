package com.gwchina.sdk.base.widget.member

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Flowable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-28 14:15
 */
internal interface MemberApi {

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/member/expire/retain/device")
    @FormUrlEncoded
    fun setRetainDevice(@Field("device_id") device_id: String): Flowable<HttpResult<Void>>

}