package com.gwchina.parent.family.data

import com.gwchina.parent.family.data.model.Approval
import com.gwchina.parent.family.data.model.FamilyPhoneInfo
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface FamilyPhoneApi {

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/set/rule")
    fun setFamilyPhoneGuard(
            @Field("child_user_id") child_user_id: String,
            @Field("enabled") enabled: String?,
            @Field("is_call_out") is_call_out: String?,
            @Field("is_call_in") is_call_in: String?
    ): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/get/group/phone")
    fun getGroupPhone(@Field("child_user_id") child_user_id: String,
                      @Field("group_id") group_id: String): Observable<HttpResult<GroupPhone>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/get/group/phone")
    fun getAllGroupPhone(@Field("child_user_id") child_user_id: String): Flowable<HttpResult<List<GroupPhone>>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/add/phone")
    fun addFamilyPhone(@Field("phone") phone: String,
                       @Field("phone_remark") phone_remark: String,
                       @Field("group_name") group_name: String?,
                       @Field("group_id") group_id: String?,
                       @Field("child_user_id") child_user_id: String): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/update/phone")
    fun editFamilyPhone(
            @Field("phone") phone: String,
            @Field("phone_remark") phone_remark: String,
            @Field("group_id") group_id: String?,
            @Field("rule_id") rule_id: String,
            @Field("child_user_id") child_user_id: String
    ): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/delete/phone")
    fun delFamilyPhone(
            @Field("rule_id") rule_id: String,
            @Field("child_user_id") child_user_id: String
    ): Observable<HttpResult<Unit>>


    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/add/group")
    fun addGroup(
            @Field("group_name") group_name: String,
            @Field("child_user_id") child_user_id: String
    ): Observable<HttpResult<Unit>>


    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/delete/group")
    fun delGroup(
            @Field("group_id") group_ids: String,
            @Field("child_user_id") child_user_id: String
    ): Observable<HttpResult<Unit>>


    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/update/group")
    fun updateGroup(
            @Field("group_ids") group_ids: String,//分组ID  多个用“,”隔开
            @Field("group_name") group_name: String?,
            @Field("is_call_out") is_call_out: String?,// 是否限制呼出 1是0否
            @Field("is_call_in") is_call_in: String?,// 是否限制呼入
            @Field("child_user_id") child_user_id: String
    ): Observable<HttpResult<Unit>>


    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/approval/record")
    fun getApprovalRecord(
            @Field("child_user_id") child_user_id: String
    ): Flowable<HttpResult<List<Approval>>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/approval/phone")
    fun approvalPhone(
            @Field("record_id") record_id: String,
            @Field("rule_type") rule_type: String,
            @Field("child_user_id") child_user_id: String
    ): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/get/rule")
    fun getFamilyPhoneInfo(
            @Field("child_user_id") child_user_id: String
    ): Flowable<HttpResult<FamilyPhoneInfo>>

}

