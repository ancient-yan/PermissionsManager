package com.gwchina.sdk.debug

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import com.gwchina.sdk.base.data.models.LoginResponse
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface DebugApi {

    /**密码登录*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_DEVICE_ID)
    @POST("patriarch/user/login/pwd")
    fun pwdLogin(
            @Field("username") username: String,
            @Field("password") password: String):
            Observable<HttpResult<LoginResponse>>

    /**密码登录*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("http://172.168.50.89:32572/debug/test/member")
    fun makeMemberExpiry(): Observable<HttpResult<Unit>>

}

