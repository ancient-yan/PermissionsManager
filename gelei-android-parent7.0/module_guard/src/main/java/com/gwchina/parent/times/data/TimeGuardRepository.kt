package com.gwchina.parent.times.data

import com.android.base.app.dagger.ActivityScope
import com.android.base.rx.SchedulerProvider
import com.android.sdk.cache.flowableOptional
import com.android.sdk.cache.getEntity
import com.android.sdk.net.kit.concatMultiSource
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.android.sdk.net.kit.resultExtractor
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.times.common.TimeGuardDailyPlanVO
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.app.StorageManager
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.data.models.isAndroid
import com.gwchina.sdk.base.data.utils.JsonUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-12 17:28
 */
@ActivityScope
class TimeGuardRepository @Inject constructor(
        private val timeGuardApi: TimeGuardApi,
        private val storageManager: StorageManager,
        private val appDataSource: AppDataSource,
        private val schedulerProvider: SchedulerProvider
) {

    companion object {
        //cache key
        private const val TIME_GUARD_PLAN_KEY = "time_guard_plan_key key_v3"
    }

    /**添加更新时间守护计划*/
    fun addUpdateTimeGuardPlan(childUserId: String, childDeviceId: String, plans: List<TimeGuardDailyPlan>): Flowable<Optional<List<TimeGuardPlanId>>> {
        val childDeviceIsAndroid = appDataSource.user().findDevice(childDeviceId).isAndroid()
        val plansJson:String
        plansJson = if (!childDeviceIsAndroid) {
            //如果是ios设备，不需要上传可用时长字段
            val iosMap = plans.map {
                TimeGuardDailyPlanIos(
                        it.rule_id,
                        it.what_day,
                        it.time_parts,
                        it.rule_time_fragment
                )
            }
            JsonUtils.toJson(iosMap)
        } else {
            JsonUtils.toJson(plans)
        }
        return timeGuardApi.addUpdateTimeGuardPlan(childUserId, childDeviceId, plansJson)
                .optionalExtractor()
                .doOnNext {

                    /*更新时间计划标识*/
                    appDataSource.user().findDevice(childDeviceId)?.let { device ->
                        val copy = device.copy(setting_time_flag = YES_FLAG)
                        appDataSource.updateDevice(copy)
                    }

                    /*更新本地缓存*/
                    if (it.isPresent) {
                        updateCache(childDeviceId, it.get(), plans)
                    }
                }
    }

    private fun updateCache(childDeviceId: String, timeGuardPlanId: List<TimeGuardPlanId>, newPlans: List<TimeGuardDailyPlan>) {
        val oldPlan = loadTimePlanFormLocalDirectly(childDeviceId) ?: return
        val oldDailyPlans = oldPlan.getDailyPlans()?.toMutableList() ?: return

        timeGuardPlanId.forEach { id ->
            newPlans.find { it.what_day == id.what_day }?.rule_id = id.rule_id
        }

        for (newRule in newPlans) {
            val oldRule = oldDailyPlans.find { it.what_day == newRule.what_day }
            if (oldRule != null) {
                oldDailyPlans.remove(oldRule)
                oldDailyPlans.add(newRule)
            } else {
                oldDailyPlans.add(newRule)
            }
        }

        storageManager.deviceStorage(childDeviceId).putEntity(TIME_GUARD_PLAN_KEY, oldPlan.copy(batch_list = oldDailyPlans))
    }

    /**获取时间守护计划*/
    fun timeGuardPlans(childUserId: String, childDeviceId: String): Flowable<Optional<TimeGuardPlan>> {

        val remote = timeGuardApi.loadTimeGuardRules(childUserId, childDeviceId)
                .optionalExtractor()

        val local = storageManager
                .deviceStorage(childDeviceId)
                .flowableOptional<TimeGuardPlan>(TIME_GUARD_PLAN_KEY)
                .subscribeOn(schedulerProvider.io())

        return concatMultiSource(remote, local, onNewData = {
            if (it != null && !it.getDailyPlans().isNullOrEmpty()) {
                storageManager.deviceStorage(childDeviceId).putEntity(TIME_GUARD_PLAN_KEY, it)
            } else {
                storageManager.deviceStorage(childDeviceId).remove(TIME_GUARD_PLAN_KEY)
            }
        })
    }

    private fun loadTimePlanFormLocalDirectly(childDeviceId: String): TimeGuardPlan? {
        return storageManager.deviceStorage(childDeviceId).getEntity<TimeGuardPlan>(TIME_GUARD_PLAN_KEY)
    }

    /**删除 [whatDays] 指定日期的守护计划*/
    fun deleteTimePlans(childUserId: String, childDeviceId: String, whatDays: List<Int>): Completable {
        return timeGuardApi.deleteTimeGuardPlans(childUserId, childDeviceId, whatDays.joinToString(","))
                .resultChecker()
                .doOnNext {
                    processOnDeleteSuccessfully(whatDays, childDeviceId)
                }
                .ignoreElements()
    }

    private fun processOnDeleteSuccessfully(whatDays: List<Int>, childDeviceId: String) {
        val oldPlan = loadTimePlanFormLocalDirectly(childDeviceId) ?: return
        val oldDailyPlans = oldPlan.getDailyPlans()?.toMutableList() ?: return

        whatDays.forEach {
            oldDailyPlans.removeAt(oldDailyPlans.indexOfFirst { plan -> plan.what_day == it })
        }

        storageManager.deviceStorage(childDeviceId).putEntity(TIME_GUARD_PLAN_KEY, oldPlan.copy(batch_list = oldDailyPlans))
    }

    /**复制[planId]对应的计划到[whatDays]指定的日期*/
    fun copyTimePlans(childUserId: String, childDeviceId: String, planId: String, whatDays: List<Int>): Completable {
        return timeGuardApi.copyTimeGuardPlans(childUserId, childDeviceId, planId, whatDays.joinToString(","))
                .optionalExtractor()
                .doOnNext {
                    updateOnCopySuccessfully(it, planId, childDeviceId)
                }
                .ignoreElements()
    }

    private fun updateOnCopySuccessfully(optionalPlanIds: Optional<List<TimeGuardPlanId>>, planId: String, childDeviceId: String) {
        val newIdList = optionalPlanIds.orElse(null) ?: return

        val oldPlan = loadTimePlanFormLocalDirectly(childDeviceId) ?: return
        val oldDailyPlans = oldPlan.getDailyPlans()?.toMutableList() ?: return

        val copied = oldDailyPlans.find { plan -> planId == plan.rule_id } ?: return

        val getIndex = fun(id: TimeGuardPlanId): Int {
            return oldDailyPlans.indexOfFirst { plan ->
                plan.what_day == id.what_day
            }
        }

        newIdList.forEach { newId ->
            val index = getIndex(newId)
            if (index != -1) {
                oldDailyPlans[index] = copied.copy(what_day = newId.what_day, rule_id = newId.rule_id)
            }
        }

        storageManager.deviceStorage(childDeviceId).putEntity(TIME_GUARD_PLAN_KEY, oldPlan.copy(batch_list = oldDailyPlans))
    }

    /**备用计划*/
    fun sparePlans(childUserId: String, childDeviceId: String): Flowable<Optional<List<TimeGuardPlan>>> {
        return timeGuardApi.loadSparePlans(childUserId, childDeviceId)
                .optionalExtractor()
    }

    /**添加备用计划*/
    fun addSparePlan(childUserId: String, childDeviceId: String, planName: String, plans: List<TimeGuardDailyPlan>): Flowable<Optional<List<TimeGuardPlanId>>> {
        return timeGuardApi.addSparePlans(childUserId, childDeviceId, planName, JsonUtils.toJson(plans)).optionalExtractor()
    }

    /**更新备用计划*/
    fun updateSparePlan(
            childUserId: String,
            childDeviceId: String,
            planId: String, planName: String,
            plans: List<TimeGuardDailyPlan>,
            deleteDailyPlanIds: List<String>
    ): Flowable<Optional<List<TimeGuardPlanId>>> {
        return timeGuardApi.updateSparePlans(
                childUserId, childDeviceId,
                planId, planName,
                JsonUtils.toJson(plans),
                deleteDailyPlanIds.joinToString(",")
        ).optionalExtractor()
    }

    /**删除备用计划*/
    fun deleteSparePlan(batchId: String): Completable {
        return timeGuardApi.deleteSparePlan(batchId)
                .resultChecker()
                .ignoreElements()
    }

    /**开启备用计划*/
    fun startSparePlan(childUserId: String, childDeviceId: String, batchId: String, setAsDefaultPlan: Boolean): Completable {
        return timeGuardApi.startStopSparePlan(childUserId, childDeviceId, batchId, FLAG_POSITIVE, FLAG_NEGATIVE, makeFlag(setAsDefaultPlan))
                .resultChecker()
                .doOnNext {

                    //更新device标识
                    if (setAsDefaultPlan) {
                        appDataSource.user().findDevice(childDeviceId)?.let { device ->
                            val copy = device.copy(setting_time_flag = YES_FLAG)
                            appDataSource.updateDevice(copy)
                        }
                    }
                    //清空时间表缓存
                    clearTimeTableCache(childDeviceId)

                }
                .ignoreElements()
    }

    /**停用备用计划*/
    fun stopSparePlan(childUserId: String, childDeviceId: String, batchId: String, update: Boolean): Completable {
        return timeGuardApi.startStopSparePlan(childUserId, childDeviceId, batchId, FLAG_NEGATIVE, if (update) 0 else 1 /*1保留最初计划；0更新最新计划*/, FLAG_NEGATIVE)
                .resultChecker()
                .doOnComplete { clearTimeTableCache(childDeviceId) }
                .ignoreElements()
    }

    private fun clearTimeTableCache(childDeviceId: String) {
        storageManager.deviceStorage(childDeviceId).remove(TIME_GUARD_PLAN_KEY)
    }

    fun hasSparePlan(): Flowable<Boolean> {
        return timeGuardApi.hasSparePlan()
                .resultExtractor()
                .map { isFlagPositive(it) }
    }

}