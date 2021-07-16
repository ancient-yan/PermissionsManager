package com.gwchina.parent.apps.data

import com.android.base.app.dagger.ActivityScope
import com.android.base.rx.SchedulerProvider
import com.android.sdk.cache.flowableOptional
import com.android.sdk.net.kit.concatMultiSource
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.apps.common.generateRuleIds
import com.gwchina.sdk.base.data.api.HttpResult
import com.gwchina.sdk.base.data.api.YES_FLAG
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.app.StorageManager
import com.gwchina.sdk.base.data.models.TimePart
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.data.utils.JsonUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Inject

/**
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-28 15:29
 */
@ActivityScope
class AppGuardRepository @Inject constructor(
        private val guardApi: AppGuardApi,
        private val storageManager: StorageManager,
        private val schedulerProvider: SchedulerProvider,
        private val appDataSource: AppDataSource
) {

    companion object {
        private const val APPS_GUARD_RULE_KEY = "apps_guard_rulekey"
    }

    /**获取设置流程的 app 列表*/
    fun appRulesGuideList(childUserId: String, childDeviceId: String): Observable<Optional<GuideAppListResponse>> {
        return guardApi.loadGuideSetAppList(childUserId, childDeviceId)
                .optionalExtractor()
    }

    fun saveGuideFlowAppRules(childUserId: String, childDeviceId: String, appRulesRequest: SetAppRulesRequest): Completable {
        return guardApi.saveAppRules(
                childUserId,
                childDeviceId,
                appRulesRequest.freelyRuleIds ?: "",
                appRulesRequest.forbidRuleIds ?: "",
                JsonUtils.toJson(appRulesRequest.singleRuleList),
                JsonUtils.toJson(appRulesRequest.groupRuleList)
        )
                .resultChecker<Unit?, HttpResult<Unit>>()
                .doOnComplete {
                    appDataSource.user().findDevice(childDeviceId)?.let {
                        val copy = it.copy(setting_soft_flag = YES_FLAG)
                        appDataSource.updateDevice(copy)
                    }
                }
                .ignoreElements()
    }

    fun appRules(childUserId: String, childDeviceId: String): Flowable<Optional<AppListResponse>> {
        val remote = guardApi.loadAppList(childUserId, childDeviceId)
                .optionalExtractor()

        val local = storageManager.deviceStorage(childDeviceId)
                .flowableOptional<AppListResponse>(APPS_GUARD_RULE_KEY)
                .subscribeOn(schedulerProvider.io())

        return concatMultiSource(remote, local) {
            storageManager.deviceStorage(childDeviceId).putEntity(APPS_GUARD_RULE_KEY, it)
        }
    }

    /** 如果是设置限时可用，则需要传 [usedTimePerDay]。*/
    fun updateAppRule(
            childUserId: String,
            childDeviceId: String,
            ruleId: String,
            ruleType: String,
            usedTimePerDay: String = "",
            time_parts: List<TimePart> = emptyList(),
            isApproval: Boolean = false
    ): Completable {

        return guardApi.updateAppRule(
                childUserId,
                childDeviceId,
                ruleId,
                ruleType,
                usedTimePerDay,
                JsonUtils.toJson(time_parts),
                if (isApproval) 1 else 0/*1待审批设置，0更新修改；*/
        )
                .resultChecker()
                .ignoreElements()
    }

    /** 删除 APP 分组 */
    fun deleteAppGroup(childUserId: String, childDeviceId: String, groupId: String): Completable {
        return guardApi.deleteAppGroup(childUserId, childDeviceId, groupId)
                .resultChecker()
                .ignoreElements()
    }

    /** 添加 APP 分组 */
    fun addAppGroup(childUserId: String, childDeviceId: String, appGroup: AppGroup): Completable {
        return guardApi.addAppGroup(
                childUserId,
                childDeviceId,
                appGroup.soft_list.generateRuleIds(),
                appGroup.soft_group_name ?: ""/*never happen*/,
                appGroup.group_used_time_perday.toString(),
                JsonUtils.toJson(appGroup.group_fragment)
        )
                .resultChecker()
                .ignoreElements()
    }

    /** 添加 APP 分组 */
    fun updateAppGroup(childUserId: String, childDeviceId: String, appGroup: AppGroup): Completable {
        return guardApi.updateAppGroup(
                childUserId,
                childDeviceId,
                appGroup.soft_group_id/*never happen*/ ?: "",
                appGroup.soft_list.generateRuleIds(),
                appGroup.soft_group_name /*never happen*/ ?: "",
                appGroup.group_used_time_perday.toString(),
                JsonUtils.toJson(appGroup.group_fragment)
        )
                .resultChecker()
                .ignoreElements()
    }

    fun approvalRecordList(childUserId: String, childDeviceId: String, pageNo: Int, pageNum: Int): Flowable<Optional<List<App>>> {
        return guardApi.loadAppApprovalRecord(childUserId, childDeviceId, pageNo, pageNum)
                .optionalExtractor()
    }

}