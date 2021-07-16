package com.gwchina.sdk.base.data.services

import com.android.sdk.net.kit.create
import com.android.sdk.net.kit.resultChecker
import com.android.sdk.net.kit.resultExtractor
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.data.models.AllInstructionState
import com.gwchina.sdk.base.data.models.InstructionState
import io.reactivex.Completable
import io.reactivex.Flowable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-23 10:39
 */
interface InstructionSyncService {

    /**获取指令同步状态。*/
    fun instructionSyncState(instructionType: String, childUserId: String, childDeviceId: String): Flowable<InstructionState>

    /**获取指令同步状态。*/
    fun allInstructionSyncState(childUserId: String, childDeviceId: String): Flowable<AllInstructionState>

    /**发生同步指令，取值：
     *
     * - [com.gwchina.sdk.base.data.api.INSTRUCTION_SYNC_LEVEL]
     * - [com.gwchina.sdk.base.data.api.INSTRUCTION_SYNC_TIME]
     * - [com.gwchina.sdk.base.data.api.INSTRUCTION_SYNC_APP]
     * - [com.gwchina.sdk.base.data.api.INSTRUCTION_SYNC_URL]
     * - [com.gwchina.sdk.base.data.api.INSTRUCTION_SYNC_PHONE]
     * - [com.gwchina.sdk.base.data.api.INSTRUCTION_IOS_SUPERVISED_FLAG]
     */
    fun sendSyncInstruction(instructionType: String, childUserId: String, childDeviceId: String): Completable

    /**获取设备是否开启的监督模式：[FLAG_NEGATIVE] 表示未开启，[FLAG_POSITIVE] 表示开启。*/
    fun iosDeviceSupervisedMode(childUserId: String, childDeviceId: String): Flowable<Int>

}

interface InstructionSyncApi {

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/sync/setting/get")
    fun loadInstructionState(
            @Field("type") type: String,
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<InstructionState>>

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/sync/setting/get/all")
    fun loadAllInstructionState(
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<AllInstructionState>>

    @Headers(ApiParameter.WITH_APP_TOKEN)
    @FormUrlEncoded
    @POST("patriarch/sync/anew/send")
    fun sendSyncInstruction(
            @Field("type") type: String,
            @Field("child_user_id") child_user_id: String,
            @Field("child_device_id") child_device_id: String
    ): Flowable<HttpResult<Unit>>
}

internal class InstructionSyncServiceImpl(
        serviceFactory: ServiceFactory
) : InstructionSyncService {

    private val instructionSyncApi = serviceFactory.create<InstructionSyncApi>()

    override fun sendSyncInstruction(instructionType: String, childUserId: String, childDeviceId: String): Completable {
        return instructionSyncApi.sendSyncInstruction(instructionType, childUserId, childDeviceId).resultChecker().ignoreElements()
    }

    override fun allInstructionSyncState(childUserId: String, childDeviceId: String): Flowable<AllInstructionState> {
        return instructionSyncApi.loadAllInstructionState(childUserId, childDeviceId).resultExtractor()
    }

    override fun iosDeviceSupervisedMode(childUserId: String, childDeviceId: String): Flowable<Int> {
        return instructionSyncState(INSTRUCTION_IOS_SUPERVISED_FLAG, childUserId, childDeviceId)
                .map { it.syn_flag ?: FLAG_NEGATIVE }
    }

    override fun instructionSyncState(instructionType: String, childUserId: String, childDeviceId: String): Flowable<InstructionState> {
        return instructionSyncApi.loadInstructionState(instructionType, childUserId, childDeviceId).resultExtractor()
    }

}