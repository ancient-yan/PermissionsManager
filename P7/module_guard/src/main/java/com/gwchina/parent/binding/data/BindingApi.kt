package com.gwchina.parent.binding.data

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Flowable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface BindingApi {

    /**绑定孩子设备*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/user/bind/child_and_device")
    fun bindChildDevice(
            @Field("name") name: String,
            @Field("sex") sex: Int,
            @Field("birthday") birthday: String,
            @Field("grade") grade: Int,
            @Field("relationship_code") relationship_code: Int,
            @Field("device_id") device_id: String,/*孩子端设备id*/
            @Field("manufacture") manufacture: String,/*厂家*/
            @Field("os_version") os_version: String,/*系统版本*/
            @Field("rom_version") rom_version: String/*rom版本*/
    ): Flowable<HttpResult<BindingNewChildResponse>>

    /**为孩子绑定设备*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/device/bind")
    fun bindDeviceForChild(
            @Field("child_user_id") child_user_id: String,
            @Field("device_id") device_id: String,/*孩子端设备id*/
            @Field("manufacture") manufacture: String,/*厂家*/
            @Field("os_version") os_version: String,/*系统版本*/
            @Field("rom_version") rom_version: String/*rom版本*/
    ): Flowable<HttpResult<BindingResponse>>

}