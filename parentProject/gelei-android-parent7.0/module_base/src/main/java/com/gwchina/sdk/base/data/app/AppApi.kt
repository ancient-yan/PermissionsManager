package com.gwchina.sdk.base.data.app

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import com.gwchina.sdk.base.data.models.*
import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface AppApi {

    /**
     * @param device_type “01” - PC, “02” - IOS, “03” - Android
     * @param device_sn 设备序列号
     * @param device_name 设备名称
     */
    @FormUrlEncoded
    @POST("common/device/init")
    fun uploadDevice(
            @Field("device_type") device_type: String,
            @Field("app_platform") app_platform: String,
            @Field("device_sn") device_sn: String,
            @Field("device_name") device_name: String,
            @Field("app_version") app_version: String
    ): Observable<HttpResult<DeviceId>>

    /**更新token，同时也会返回最新的用户数据*/
    @FormUrlEncoded
    @POST("common/user/refresh/token")
    @Headers(ApiParameter.WITH_DEVICE_ID)
    fun updateToken(@Field("app_token") appToken: String): Observable<HttpResult<LoginResponse>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/index/child/list")
    fun updateChildList(@Field("version") version: String = "3.0.2"): Observable<HttpResult<SyncChildrenResponse>>

    @FormUrlEncoded
    @POST("common/device/registid/update")
    fun uploadPushId(
            @Field("device_id") device_id: String,
            @Field("regist_id") registerId: String,
            @Field("platform") platform: String,
            @Field("pa_type") pushType: String
    ): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @POST("common/sms/get/code")
    fun sendCode(
            @Field("sms_type") sms_type: String,
            @Field("mobile") mobile: String
    ): Flowable<HttpResult<Unit>>

    @Headers(ApiParameter.WITH_DEVICE_ID)
    @FormUrlEncoded
    @POST("common/sms/check/code")
    fun validateCode(
            @Field("sms_type") sms_type: String,
            @Field("mobile") mobile: String,
            @Field("sms_code") sms_code: String
    ): Flowable<HttpResult<Unit>>

    @POST("patriarch/advertising/list")
    fun loadAppAdvertisingList(): Flowable<HttpResult<List<Advertising>>>


    /**检查孩子端设备权限*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/privilege/list")
    fun childDevicePermissionChecker(@Field("child_user_id") child_user_id: String, @Field("child_device_id") child_device_id: String):Flowable<HttpResult<PrivilegeData>>


}