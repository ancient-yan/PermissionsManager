package com.gwchina.parent.times.presentation.guide

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.subscribeWithLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.times.data.TimeGuardRepository
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.findDevice
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-23 13:44
 */
class TimeGuardGuideViewModel @Inject constructor(
        private val timeGuardRepository: TimeGuardRepository,
        private val appDataSource: AppDataSource,
        @Named(CHILD_USER_ID_KEY) val childUserId: String,
        @Named(DEVICE_ID_KEY) val childDeviceId: String
) : ArchViewModel() {

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser())

    val device: Device
        get() = appDataSource.user().findDevice(childDeviceId) ?: throw NullPointerException("device cannot be found")

    private val _loadingSparePlanStatus = MutableLiveData<Resource<Boolean>>()

    val loadingSparePlanStatus: LiveData<Resource<Boolean>>
        get() = _loadingSparePlanStatus

    init {
        fetchSparePlanStatus()
    }

    fun fetchSparePlanStatus() {
        timeGuardRepository.hasSparePlan()
                .autoDispose()
                .subscribeWithLiveData(_loadingSparePlanStatus)
    }

}