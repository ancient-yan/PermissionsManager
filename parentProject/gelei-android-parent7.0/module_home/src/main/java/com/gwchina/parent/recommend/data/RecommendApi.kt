package com.gwchina.parent.recommend.data

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
 *      Date : 2019-02-13 11:14
 */
interface RecommendApi {

    @FormUrlEncoded
    @POST("patriarch/recommend/group/detail")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun loadRecommendList(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String,
            @Field("rec_group_id") rec_group_id: String
    ): Flowable<HttpResult<RecommendResponse>>

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

    @FormUrlEncoded
    @POST("patriarch/recommend/subject/detail")
    @Headers(ApiParameter.WITH_APP_TOKEN)
    fun loadSubjectDetail(
            @Field("rec_subject_id") rec_subject_id: String,
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Observable<HttpResult<SubjectDetailResponse>>

}