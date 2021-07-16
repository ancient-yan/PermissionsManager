package com.gwchina.parent.apps.data

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
 *      Date : 2018-12-28 15:29
 */
interface AppGuardApi {

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/default/list")
    fun loadGuideSetAppList(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Observable<HttpResult<GuideAppListResponse>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/edit")
    fun updateAppRule(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("rule_ids") rule_ids: String,
            @Field("rule_type") rule_type: String,
            @Field("used_time_perday") used_time_perday: String,
            @Field("time_parts") time_parts: String,
            @Field("is_approval") is_approval: Int
    ): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/default/initial")
    fun saveAppRules(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("freely_rule_ids") freely_rule_ids: String,
            @Field("forbid_rule_ids") forbid_rule_ids: String,
            @Field("single_rule_list") single_rule_list: String,
            @Field("group_rule_list") group_rule_list: String
    ): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/type/list")
    fun loadAppList(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<AppListResponse>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/add/group")
    fun addAppGroup(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("rule_ids") rule_ids: String,
            @Field("soft_group_name") soft_group_name: String,
            @Field("used_time_perday") used_time_perday: String,
            @Field("time_parts") time_parts: String
    ): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/edit/group")
    fun updateAppGroup(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("soft_group_id") soft_group_id: String,
            @Field("rule_ids") rule_ids: String,
            @Field("soft_group_name") soft_group_name: String,
            @Field("used_time_perday") used_time_perday: String,
            @Field("time_parts") time_parts: String
    ): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/del/group")
    fun deleteAppGroup(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("soft_group_id") soft_group_id: String
    ): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/record/approval")
    fun loadAppApprovalRecord(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("pos") pos: Int,
            @Field("limit") limit: Int
    ): Flowable<HttpResult<List<App>>>

}