package com.gwchina.parent.times.presentation.make

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.subscribeWithLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.times.common.TimeGuardDailyPlanVO
import com.gwchina.parent.times.common.TimeMapper
import com.gwchina.parent.times.data.TimeGuardRepository
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.api.isAndroid
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.deviceCount
import com.gwchina.sdk.base.data.models.findDevice
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-09 13:58
 */
open class MakeTimeGuardPlanViewModel @Inject constructor(
        private val timeGuardRepository: TimeGuardRepository,
        appDataSource: AppDataSource,
        private val timeMapper: TimeMapper,
        @Named(CHILD_USER_ID_KEY) private val childUserId: String,
        @Named(DEVICE_ID_KEY) private val childDeviceId: String
) : ArchViewModel() {

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser())

    val childDeviceIsAndroid = isAndroid(appDataSource.user().findDevice(childDeviceId)?.device_type)

    val deviceCount = appDataSource.user().deviceCount()

    private val _addPlan = MutableLiveData<Resource<Any>>()

    val addPlan: LiveData<Resource<Any>>
        get() = _addPlan

    fun addTimeGuardPlans(plan: List<TimeGuardDailyPlanVO>) {
        removeTimePeriodWithType(plan)
        timeGuardRepository.addUpdateTimeGuardPlan(childUserId, childDeviceId, timeMapper.toTimeGuardDailyPlanList(plan))
                .ignoreElements()
                .subscribeWithLiveData(_addPlan)
    }

    fun addSparePlans(planName: String, plan: List<TimeGuardDailyPlanVO>) {
        removeTimePeriodWithType(plan)
        timeGuardRepository.addSparePlan(childUserId, childDeviceId, planName, timeMapper.toTimeGuardDailyPlanList(plan))
                .ignoreElements()
                .subscribeWithLiveData(_addPlan)
    }

    /**
     * 移除掉TimePeriod type为1的
     */
    private fun removeTimePeriodWithType(plan: List<TimeGuardDailyPlanVO>) {
        plan.forEach {
            it.timePeriodList.remove(
                    it.timePeriodList.find {timePeriod -> timePeriod.type == 1 }
            )
        }
    }
}