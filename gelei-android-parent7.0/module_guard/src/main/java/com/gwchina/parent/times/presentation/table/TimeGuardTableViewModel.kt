package com.gwchina.parent.times.presentation.table

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.android.base.app.aac.subscribeWithLiveData
import com.android.base.app.dagger.ContextType
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.times.common.TimeGuardDailyPlanVO
import com.gwchina.parent.times.common.TimeGuardWeeklyPlanVO
import com.gwchina.parent.times.common.TimeMapper
import com.gwchina.parent.times.data.TimeGuardRepository
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.data.models.isAndroid
import com.gwchina.sdk.base.utils.generateDeviceFlag
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-15 14:06
 */
class TimeGuardTableViewModel @Inject constructor(
        private val timeGuardRepository: TimeGuardRepository,
        val appDataSource: AppDataSource,
        private val timeMapper: TimeMapper,
        @ContextType private val context: Context,
        @Named(CHILD_USER_ID_KEY) val childUserId: String,
        @Named(DEVICE_ID_KEY) val childDeviceId: String
) : ArchViewModel() {

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser())

    val childDeviceIsAndroid = appDataSource.user().findDevice(childDeviceId).isAndroid()

    private val _timeGuardRuses = MutableLiveData<Resource<TimeGuardWeeklyPlanVO>>()
    val timeGuardRuses: LiveData<Resource<TimeGuardWeeklyPlanVO>>
        get() = _timeGuardRuses

    private val _updateTimeGuardPlan = MutableLiveData<Resource<Any>>()
    val updateTimeGuardPlan: LiveData<Resource<Any>>
        get() = _updateTimeGuardPlan

    private val _copyPlans = MutableLiveData<Resource<Any>>()
    val copyPlans: LiveData<Resource<Any>>
        get() = _copyPlans

    private val _deletePlans = MutableLiveData<Resource<Any>>()
    val deletePlans: LiveData<Resource<Any>>
        get() = _deletePlans

    val deviceFlag = LiveDataReactiveStreams.fromPublisher(
            appDataSource.observableUser()
                    .map { generateDeviceFlag(context, it, childDeviceId) }
    )

    init {
        loadTimeGuardPlans()
    }

    fun loadTimeGuardPlans() {
        _timeGuardRuses.postValue(Resource.loading())

        timeGuardRepository.timeGuardPlans(childUserId, childDeviceId)
                .autoDispose()
                .subscribe(
                        {
                            if (it.isPresent) {
                                _timeGuardRuses.postValue(Resource.success(timeMapper.toTimeGuardWeeklyPlanVO(it.get())))
                            } else {
                                _timeGuardRuses.postValue(Resource.success(null))
                            }
                        },
                        {
                            _timeGuardRuses.postValue(Resource.error(it))
                        }
                )
    }

    fun updateTimeGuardPlans(timeGuardRules: List<TimeGuardDailyPlanVO>) {
        timeGuardRepository.addUpdateTimeGuardPlan(childUserId, childDeviceId, timeMapper.toTimeGuardDailyPlanList(timeGuardRules))
                .ignoreElements()
                .doOnComplete { loadTimeGuardPlans() }
                .subscribeWithLiveData(_updateTimeGuardPlan)
    }

    fun copyPlans(id: String, whatDays: List<Int>) {
        timeGuardRepository.copyTimePlans(childUserId, childDeviceId, id, whatDays)
                .doOnComplete { loadTimeGuardPlans() }
                .subscribeWithLiveData(_copyPlans)
    }

    fun deletePlans(days: List<Int>) {
        timeGuardRepository.deleteTimePlans(childUserId, childDeviceId, days)
                .doOnComplete { loadTimeGuardPlans() }
                .subscribeWithLiveData(_deletePlans)
    }

}