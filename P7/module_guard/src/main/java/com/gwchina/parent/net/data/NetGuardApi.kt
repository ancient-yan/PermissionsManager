package com.gwchina.parent.net.data

import com.gwchina.parent.net.data.model.AppRuleInfo
import com.gwchina.parent.net.data.model.GuardRecord
import com.gwchina.parent.net.data.model.RuleUrlInfo
import com.gwchina.parent.net.data.model.SiteInfo
import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-15 17:31
 *      Desc : 上网守护模块请求
 */

interface NetGuardApi {

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruleurl/baseinfo")
    fun getRuleUrlBaseInfo(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<RuleUrlInfo>>


    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruleurl/pattern/update")
    fun updateRuleUrlPattern(@Field("pattern_id") pattern_id: String,
                             @Field("pattern_type") pattern_type: String,
                             @Field("child_user_id") child_user_id: String,
                             @Field("child_device_id") child_device_id: String): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruleurl/intercept/list")
    fun getGuardRecordList(@Field("pos") pos: Int,
                           @Field("limit") limit: Int,
                           @Field("child_user_id") child_user_id: String,
                           @Field("child_device_id") child_device_id: String): Observable<HttpResult<List<GuardRecord>>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruleurl/show")
    fun getSiteList(@Field("list_type") list_type: String,
                    @Field("pos") pos: Int,
                    @Field("limit") limit: Int,
                    @Field("child_user_id") child_user_id: String,
                    @Field("child_device_id") child_device_id: String): Flowable<HttpResult<List<SiteInfo>>>


    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruleurl/add")
    fun addUrl(@Field("rule_type") rule_type: String,
               @Field("url") url: String,
               @Field("url_name") url_name: String?,
               @Field("list_type") list_type: String,
               @Field("child_user_id") child_user_id: String,
               @Field("child_device_id") child_device_id: String): Observable<HttpResult<Unit>>


    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruleurl/update")
    fun updateUrl(@Field("rule_type") rule_type: String,
                  @Field("url") url: String,
                  @Field("url_name") url_name: String?,
                  @Field("rule_id") rule_id: String,
                  @Field("list_type") list_type: String,
                  @Field("child_user_id") child_user_id: String,
                  @Field("child_device_id") child_device_id: String): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/ruleurl/delete")
    fun deleteUrls(@Field("rule_id_list") rule_id_list: String,
                   @Field("child_user_id") child_user_id: String,
                   @Field("child_device_id") child_device_id: String): Observable<HttpResult<Unit>>

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/info")
    fun queryAppInfo(@Field("bundle_id") bundle_id: String,
                   @Field("child_user_id") child_user_id: String,
                   @Field("child_device_id") child_device_id: String): Observable<HttpResult<AppRuleInfo>>

}