package com.gwchina.parent.times.data

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
 *      Date : 2018-12-12 17:29
 */
interface TimeGuardApi {

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/batch")
    fun addUpdateTimeGuardPlan(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("batch_time") batch_time: String
    ): Flowable<HttpResult<List<TimeGuardPlanId>>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/show")
    fun loadTimeGuardRules(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<TimeGuardPlan>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/delete/batch")
    fun deleteTimeGuardPlans(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("what_days") what_days: String
    ): Flowable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/copy/rule")
    fun copyTimeGuardPlans(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("rule_id") rule_id: String,
            @Field("copy_to_what_day") copy_to_what_day: String
    ): Flowable<HttpResult<List<TimeGuardPlanId>>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/show/planb")
    fun loadSparePlans(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<List<TimeGuardPlan>>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/batch/planb")
    fun addSparePlans(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("batch_name") batch_name: String,
            @Field("batch_time") batch_time: String
    ): Flowable<HttpResult<List<TimeGuardPlanId>>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/batch/planb")
    fun updateSparePlans(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("batch_id") batch_id: String,
            @Field("batch_name") batch_name: String,
            @Field("batch_time") batch_time: String,
            @Field("del_rule_ids") del_rule_ids: String
    ): Flowable<HttpResult<List<TimeGuardPlanId>>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/delete/planb")
    fun deleteSparePlan(@Field("batch_id") batch_id: String): Flowable<HttpResult<Unit>>

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/has/planb")
    fun hasSparePlan(): Flowable<HttpResult<Int>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruletime/startup/planb")
    fun startStopSparePlan(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("batch_id") batch_id: String,
            @Field("enabled") enabled: Int,/*0 disable, 1 enable*/
            @Field("mode") mode: Int,/*停止时不能为空，1保留最初计划；0更新最新计划*/
            @Field("is_used") is_used: Int/*是否设置为常用计划，1是；0否*/
    ): Flowable<HttpResult<Unit>>

}