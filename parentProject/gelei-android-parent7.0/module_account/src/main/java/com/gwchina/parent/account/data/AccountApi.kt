package com.gwchina.parent.account.data

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import com.gwchina.sdk.base.data.models.LoginResponse
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 14:29
 */
interface AccountApi {

    /**短信登录*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_DEVICE_ID)
    @POST("patriarch/user/login/sms/glapp")
    fun smsLogin(
            @Field("username") username: String,
            @Field("sms_code") sms_code: String):
            Observable<HttpResult<LoginResponse>>

    /**微信登录*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_DEVICE_ID)
    @POST("patriarch/user/login/wechat/glapp")
    fun wechatLogin(@Field("auth_code") auth_code: String): Observable<HttpResult<LoginResponse>>

    /**绑定微信*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_DEVICE_ID)
    @POST("patriarch/user/login/wechat/bind/glapp")
    fun bindWechat(
            @Field("auth_code") auth_code: String,
            @Field("username") username: String,
            @Field("sms_code") sms_code: String):
            Observable<HttpResult<LoginResponse>>

}