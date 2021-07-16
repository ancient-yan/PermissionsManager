package com.gwchina.sdk.base.utils

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.data.Resource
import com.android.base.rx.subscribed
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.INSTRUCTION_IOS_SUPERVISED_FLAG
import com.gwchina.sdk.base.data.api.isFlagPositive

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-26 14:14
 */
class SuperviseModeSynchronizer(
        private val childUserId: String,
        private val childDeviceId: String
) {

    @Volatile private lateinit var resource: Resource<Boolean>

    @Volatile private var isConnected: Boolean = false

    private val _state = SingleLiveData<Resource<Boolean>>()

    val state: LiveData<Resource<Boolean>>
        get() = _state

    private val instructionSyncService = AppContext.serviceManager().instructionSyncService

    init {
        loadSuperviseModeState()
    }

    fun startSync() {
        if (isConnected) {
            if (resource.isSuccess && resource.get() == true) {
                _state.postValue(resource)
            } else if (!resource.isLoading) {
                loadSuperviseModeState()
            }
        } else {
            isConnected = true
            if (resource.isError) {
                loadSuperviseModeState()
            } else {
                _state.postValue(resource)
            }
        }
    }

    private fun syncIosDeviceStatus() {
        instructionSyncService.sendSyncInstruction(INSTRUCTION_IOS_SUPERVISED_FLAG, childUserId, childDeviceId).subscribed()
    }

    private fun loadSuperviseModeState() {
        resource = Resource.loading()
        setToLiveData()

        instructionSyncService.iosDeviceSupervisedMode(childUserId, childDeviceId)
                .subscribe(
                        {
                            resource = Resource.success(isFlagPositive(it))
                            setToLiveData()
                            if (!resource.data()) {
                                syncIosDeviceStatus()
                            }
                        },
                        {
                            resource = Resource.error(it)
                            setToLiveData()
                        }
                )
    }

    private fun setToLiveData() {
        if (isConnected) {
            _state.postValue(resource)
        }
    }

}