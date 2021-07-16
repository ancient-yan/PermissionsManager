package com.gwchina.parent.main.data

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import com.gwchina.sdk.base.data.models.PermissionDetail
import com.gwchina.sdk.base.data.models.PrivilegeData
import io.reactivex.Flowable
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-02 17:42
 */

interface MainApi {

    /**首页数据*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/index/home/glapp")
    fun loadHomePageData(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<HomeResponse>>

    /**添加临时可用*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/usabletemp/add")
    fun setTempUsable(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
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

    /**禁用待批准软件*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulesoft/edit")
    fun forbidApp(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("rule_ids") rule_ids: String,
            @Field("rule_type") rule_type: String,
            @Field("is_approval") is_approval: String,
            @Field("used_time_perday") used_time_perday: String
    ): Flowable<HttpResult<Unit>>

    /**加载所有待审批应用列表（所有孩子，所有设备）*/
    @POST("patriarch/rulesoft/all/noapproval")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun loadAllPendingApprovalAppList(): Flowable<HttpResult<SoftApproval>>

    /**加载所有待审批号码列表（所有孩子，所有设备）*/
    @POST("patriarch/rulephone/approval/record")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun loadAllPendingApprovalPhoneList(): Flowable<HttpResult<List<PhoneApprovalInfo>>>

    @FormUrlEncoded
    @POST("patriarch/recommend/soft/install")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun installAppForChild(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("soft_name") soft_name: String,
            @Field("bundle_id") bundle_id: String,
            @Field("rec_source ") rec_source: String
    ): Flowable<HttpResult<Unit>>

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/index/child/location")
    @FormUrlEncoded
    fun getChildLocation(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<ChildLocation>>

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/index/child/location/sync")
    @FormUrlEncoded
    fun sendSyncChildLocationInstruction(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<Unit>>

    /**我的界面信息*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/user/info")
    fun loadMinePageInfo(): Flowable<HttpResult<MineResponse>>

    /**为孩子解绑设备*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/device/unbind")
    fun unBindChild(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("code") smsCode: String
    ): Flowable<HttpResult<NewDeviceId>>

    data class NewDeviceId(val default_device_id: String?)

    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/rulephone/approval/phone")
    fun approvalPhone(
            @Field("record_id") record_id: String,
            @Field("rule_type") rule_type: String,
            @Field("child_user_id") child_user_id: String
    ): Observable<HttpResult<Unit>>

    /**加载周报列表数据*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/guard/report/record/list")
    fun loadWeeklyListData(): Observable<HttpResult<List<WeeklyInfo>>>

    /**上传首页头图*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/index/home/up/top/img")
    fun upHomeTopImg(@Field("child_user_id") child_user_id: String, @Field("home_top_img") home_top_img: String): Observable<HttpResult<String>>

    /**首页使用记录*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/index/home/time/line/record")
    fun homePageUsingRecord(@Field("child_user_id") child_user_id: String, @Field("child_device_id") child_device_id: String): Observable<HttpResult<UsingRecord>>

    /**检查孩子端设备权限*/
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/privilege/list")
    fun childDevicePermissionChecker(@Field("child_user_id") child_user_id: String, @Field("child_device_id") child_device_id: String):Flowable<HttpResult<PrivilegeData>>

}