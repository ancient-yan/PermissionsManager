package com.gwchina.parent.profile.data

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import com.gwchina.sdk.base.data.models.Patriarch
import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-03-18 15:45
 */
interface ProfileApi {

    /**
     *获取家长信息
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/user/details")
    fun getPatriarchDetail(): Observable<HttpResult<Patriarch>>


    /**  更新个人信息（家长），NOTE： 昵称仅支持最长10个字符，不能有非法字符*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/user/updatedetails")
    fun updatePatriarchNickName(
            @Field("nick_name") nick_name: String, @Field("birthdate") birthday: String, @Field("area_code") area_code: String
    ): Observable<HttpResult<Unit>>

    /** 更新个人信息（孩子）*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/user/update/child")
    fun updateChildInfo(
            @Field("child_user_id") child_user_id: String,
            @Field("nick_name") nick_name: String,
            @Field("sex") sex: String,
            @Field("birthdate") birthdate: String,
            @Field("grade") grade: String
    ): Observable<HttpResult<Unit>>

    /**
     * 获取收货地址
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/user/address/list")
    fun getDeliveryAddress(): Observable<HttpResult<List<DeliveryAddress>>>

    /**
     * 添加收货地址
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/user/address/add")
    fun addDeliveryAddress(@Field("receiver_name") receiver_name: String,
                           @Field("receiver_phone") receiver_phone: String,
                           @Field("province_area_code") provinceCode: String,
                           @Field("city_area_code") cityCode: String,
                           @Field("district_area_code") districtCode: String,
                           @Field("address") address: String,
                           @Field("is_default") is_default: String = "1"
    ): Observable<HttpResult<Any>>

    /**
     * 编辑(修改)收货地址
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/user/address/edit")
    fun updateDeliveryAddress(
            @Field("record_id") record_id: String,
            @Field("receiver_name") receiver_name: String,
            @Field("receiver_phone") receiver_phone: String,
            @Field("province_area_code") provinceCode: String,
            @Field("city_area_code") cityCode: String,
            @Field("district_area_code") districtCode: String,
            @Field("address") address: String,
            @Field("is_default") is_default: String = "1"): Observable<HttpResult<Any>>

    /**
     * 删除收货地址
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/user/address/delete")
    fun deleteDeliveryAddress(@Field("record_id") record_id: String): Observable<HttpResult<Any>>


    /**添加临时可用*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/usabletemp/add")
    fun setTempUsable(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("mode") mode: Int,
            @Field("enabled_time") enabled_time: Int
    ): Flowable<HttpResult<TempUsable>>

    /**删除临时可用*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/usabletemp/delete")
    fun deleteTempUsable(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("rule_id") rule_id: String
    ): Flowable<HttpResult<Unit>>

    /**获取孩子设备的守护时间规则*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/ruletime/list")
    fun getChildDeviceTimeRule(@Field("child_user_id") child_user_id: String): Flowable<HttpResult<List<TimeGuardPlan>>>
}