package com.gwchina.sdk.base.upgrade

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
 *      Date : 2019-02-22 10:24
 */
interface UpgradeApi {

    @FormUrlEncoded
    @POST("common/compare/version")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun loadLeastVersion(
            @Field("platform") platform: String,
            @Field("app_version") app_version: String
    ): Flowable<HttpResult<UpgradeResponse>>

}

data class UpgradeResponse(val app_version: UpgradeData?)

data class UpgradeData(
        val create_time: Long = 0,
        val file_size: Int = 0,
        val is_force: Int = 0,
        val is_remind: Int = 0,
        val platform: String = "",
        val publish_channel: String = "",
        val publish_time: Long = 0,
        val status: String = "",
        val stop_time: String = "",
        val update_desc: String = "",
        val update_title: String = "",
        val update_url: String = "",
        val version: String = "",
        val version_id: String = "")