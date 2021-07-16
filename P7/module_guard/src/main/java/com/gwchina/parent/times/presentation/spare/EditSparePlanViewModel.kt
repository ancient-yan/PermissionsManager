package com.gwchina.parent.times.presentation.spare

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.subscribeWithLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.times.common.TimeGuardWeeklyPlanVO
import com.gwchina.parent.times.common.TimeMapper
import com.gwchina.parent.times.data.TimeGuardRepository
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.data.models.isAndroid
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-25 19:34
 */
class EditSparePlanViewModel @Inject constructor(
        private val timeGuardRepository: TimeGuardRepository,
        appDataSource: AppDataSource,
        private val timeMapper: TimeMapper,
        @Named(CHILD_USER_ID_KEY) private val childUserId: String,
        @Named(DEVICE_ID_KEY) private val childDeviceId: String
) : ArchViewModel() {

    private val _updateSparePlan = MutableLiveData<Resource<Any>>()

    val updateSparePlan: LiveData<Resource<Any>>
        get() = _updateSparePlan

    fun updateSparePlan(weeklyPlan: TimeGuardWeeklyPlanVO, toBeDeleted: List<String>) {
        timeGuardRepository.updateSparePlan(
                childUserId, childDeviceId,
                weeklyPlan.planId, weeklyPlan.planName,
                timeMapper.toTimeGuardDailyPlanList(weeklyPlan.dailyPlans),
                toBeDeleted
        )
                .ignoreElements()
                .subscribeWithLiveData(_updateSparePlan)
    }

    val childDeviceIsAndroid = appDataSource.user().findDevice(childDeviceId).isAndroid()

}