package com.gwchina.parent.migration.data

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
 *      Date : 2019-08-28 14:13
 */
interface MigrationApi {

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/user/migration/init")
    fun deviceList(): Flowable<HttpResult<MigrationResponse>>

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/migration/upgrade/choose")
    fun setMigrationFlag(
            @Field("migration_upgrade_choose_flag") migration_upgrade_choose_flag: Int
    ): Flowable<HttpResult<Unit>>

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/user/bind/child_and_device/batch")
    fun batchBindChildDevice(
            @Field("child_list") child_list: String
    ): Flowable<HttpResult<Unit>>

}