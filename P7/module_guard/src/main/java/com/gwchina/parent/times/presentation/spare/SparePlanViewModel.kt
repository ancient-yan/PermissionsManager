package com.gwchina.parent.times.presentation.spare

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.aac.subscribeWithLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.times.common.TimeEventCenter
import com.gwchina.parent.times.common.TimeMapper
import com.gwchina.parent.times.data.TimeGuardRepository
import com.gwchina.parent.times.presentation.spare.OperationInfo.Companion.DELETE_PLAN
import com.gwchina.parent.times.presentation.spare.OperationInfo.Companion.START_PLAN
import com.gwchina.parent.times.presentation.spare.OperationInfo.Companion.STOP_AND_NO_UPDATE
import com.gwchina.parent.times.presentation.spare.OperationInfo.Companion.STOP_AND_UPDATE
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.api.isAndroid
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.findDevice
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-23 15:29
 */
class SparePlanViewModel @Inject constructor(
        private val timeGuardRepository: TimeGuardRepository,
        appDataSource: AppDataSource,
        private val timeMapper: TimeMapper,
        private val eventCenter: TimeEventCenter,
        @Named(CHILD_USER_ID_KEY) private val childUserId: String,
        @Named(DEVICE_ID_KEY) private val childDeviceId: String
) : ArchViewModel() {

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser())

    var isChoosingMode: Boolean = false

    private val childDeviceIsAndroid = isAndroid(appDataSource.user().findDevice(childDeviceId)?.device_type)

    private val _sparePlans = SingleLiveData<Resource<List<Any>>>()

    val sparePlans: LiveData<Resource<List<Any>>>
        get() = _sparePlans

    private val _operationStatus = MutableLiveData<Resource<OperationInfo>>()
    val operationStatus: LiveData<Resource<OperationInfo>>
        get() = _operationStatus

//    init {
//        loadSparePlans()
//    }

    fun loadSparePlans() {
        val value = _sparePlans.value?.get()
        timeGuardRepository.sparePlans(childUserId, childDeviceId)
                .map {

                    timeMapper.toSparePlanVOList(isChoosingMode, childDeviceIsAndroid, it.orElse(null)) { planId ->
                        checkIsExpand(planId, value)
                    }

                }
                .autoDispose()
                .subscribeWithLiveData(_sparePlans)
    }

    private fun checkIsExpand(it: String, list: List<Any>?): Boolean {
        list ?: return false

        val find = list.find { item ->
            item is SparePlanHeader && item.batchId == it
        } ?: return false

        return (find as SparePlanHeader).expanded
    }

    fun deletePlan(batchId: String) {
        timeGuardRepository.deleteSparePlan(batchId)
                .subscribeWithLiveData(_operationStatus) {
                    OperationInfo(DELETE_PLAN, batchId)
                }
    }

    fun startPlan(batchId: String) {
        timeGuardRepository.startSparePlan(childUserId, childDeviceId, batchId, isChoosingMode)
                .doOnComplete { eventCenter.notifyPlansChanged() }
                .subscribeWithLiveData(_operationStatus) {
                    OperationInfo(START_PLAN, batchId)
                }
    }

    fun stopPlan(batchId: String, update: Boolean) {
        timeGuardRepository.stopSparePlan(childUserId, childDeviceId, batchId, update)
                .doOnComplete { eventCenter.notifyPlansChanged() }
                .subscribeWithLiveData(_operationStatus) {
                    if (update) {
                        OperationInfo(STOP_AND_UPDATE, batchId)
                    } else {
                        OperationInfo(STOP_AND_NO_UPDATE, batchId)
                    }
                }
    }

}

class OperationInfo(
        val type: Int,
        val planId: String
) {
    companion object {
        internal const val STOP_AND_NO_UPDATE = 1
        internal const val STOP_AND_UPDATE = 2
        internal const val START_PLAN = 3
        internal const val DELETE_PLAN = 4
    }
}