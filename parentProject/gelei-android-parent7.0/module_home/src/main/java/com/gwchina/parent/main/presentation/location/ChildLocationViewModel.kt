package com.gwchina.parent.main.presentation.location

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import com.android.base.app.mvvm.ArchViewModel
import com.gwchina.parent.main.data.ChildLocation
import com.gwchina.parent.main.data.MainRepository
import com.gwchina.sdk.base.app.ErrorHandler
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.utils.timestampMillis
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

class ChildLocationViewModel @Inject constructor(
        private val mainRepository: MainRepository,
        private val appDataSource: AppDataSource,
        private val errorHandler: ErrorHandler
) : ArchViewModel() {

    companion object {
        private const val MAX_ALLOW_TIME_OFFSET = 2 * 60 * 1000
        private const val MAX_RETRY_TIMES = 2
        private const val DELAY_TIME = 10L
    }

    @Volatile private var retryTimes = 0

    private var compositeDisposable: CompositeDisposable? = null

    private val _showLocationSuccessMessage = MutableLiveData<Any>()

    val showLocationSuccessMessage: LiveData<Any>
        get() = _showLocationSuccessMessage

    private val _childLocation = MutableLiveData<LocationStatus>()

    val childLocation: LiveData<ChildLocation>
        get() = Transformations.map(_childLocation) {
            it.childLocation
        }

    val childWithDevice: ChildWithDevice
        get() {
            return ChildWithDevice(appDataSource.user().currentChild, appDataSource.user().currentDevice)
        }

    private val _locationStatus = MutableLiveData<LocationLoadingStatus>()
    val locationLoadingStatus: LiveData<LocationLoadingStatus>
        get() = _locationStatus

    private val childUserId: String
        get() = childWithDevice.child?.child_user_id ?: ""

    fun startPositioningForDevice(device: Device, previousLocation: ChildLocation?) {
        //reset
        resetStatus(device.device_id)

        //cache
        var lastLocation = mainRepository.lastLocation(device.device_id)
        //compare
        if (previousLocation != null && (lastLocation == null || lastLocation.upload_time < previousLocation.upload_time)) {
            lastLocation = previousLocation
            mainRepository.saveNewestLocation(device.device_id, previousLocation)
        }

        //check
        when {
            //没有之前的数据
            lastLocation == null -> {
                sendSyncPositionInstruction(device.device_id)
            }
            //大于允许的最大同步时间延迟
            abs(timestampMillis() - lastLocation.upload_time) > MAX_ALLOW_TIME_OFFSET -> {
                //show previous
                _childLocation.postValue(LocationStatus(device.device_id, lastLocation, false))
                //send sync
                sendSyncPositionInstruction(device.device_id)
            }
            //可用时间内，直接使用之前的位置信息
            else -> {
                _locationStatus.postValue(LocationLoadingStatus(isLoading = true, locationError = false))
                _childLocation.postValue(LocationStatus(device.device_id, lastLocation, isSyncSuccess = true))
            }
        }
    }

    private fun resetStatus(deviceId: String) {
        retryTimes = 0
        compositeDisposable?.dispose()
        compositeDisposable = CompositeDisposable()
        _locationStatus.value = LocationLoadingStatus(isLoading = true, locationError = false)
        _childLocation.value = LocationStatus(deviceId)
    }

    private fun sendSyncPositionInstruction(deviceId: String) {
        _locationStatus.postValue(LocationLoadingStatus(isLoading = true, locationError = false))

        mainRepository.sendSyncLocationInstruction(childUserId, deviceId)
                .subscribe(
                        {
                            loadNewestLocationLooped(deviceId)
                        },
                        {
                            _locationStatus.postValue(LocationLoadingStatus(isLoading = false, locationError = false))
                            errorHandler.handleError(it)
                        }
                ).apply {
                    compositeDisposable?.add(this)
                }
    }

    private fun loadNewestLocationLooped(deviceId: String) {
        retryTimes++

        mainRepository.newestChildLocation(childUserId, deviceId)
                .delaySubscription(DELAY_TIME, TimeUnit.SECONDS)
                .subscribe(
                        {
                            if (it.isPresent) {
                                checkNewestLocation(deviceId, it.get())
                            } else {
                                endLoop(locationError = false, showLoading = false)
                            }
                        },
                        {
                            endLoop(locationError = false, showLoading = false)
                            errorHandler.handleError(it)
                        }
                ).apply {
                    compositeDisposable?.add(this)
                }
    }

    private fun endLoop(locationError: Boolean, showLoading: Boolean) {
        retryTimes = 0
        _locationStatus.postValue(LocationLoadingStatus(isLoading = showLoading, locationError = locationError))
    }

    private fun checkNewestLocation(deviceId: String, newestLocation: ChildLocation) {
        _childLocation.value?.let {
            //返回的时间戳与最新的时间戳进行比较，若返回的时间戳与最新的时间戳相同，代表没有获取到最新的位置信息, 等待 10s 再次请求
            if (it.childLocation != null && it.childLocation.upload_time == newestLocation.upload_time) {

                //可能其他数据发送了变化，所以还是发给 UI 展示
                mainRepository.saveNewestLocation(deviceId, newestLocation)
                _childLocation.postValue(LocationStatus(deviceId, newestLocation, isSyncSuccess = false))

                if (retryTimes >= MAX_RETRY_TIMES) {
                    endLoop(true, showLoading = false)
                } else {
                    loadNewestLocationLooped(deviceId)
                }
            }
            /*若返回的时间戳不等于最新的时间戳，则证明同步位置成功，并结束位置同步*/
            else {
                mainRepository.saveNewestLocation(deviceId, newestLocation)
                _childLocation.postValue(LocationStatus(deviceId, newestLocation, isSyncSuccess = true))
                endLoop(false, showLoading = true)
            }
        }
    }

    fun locationEnded(success: Boolean) {
        //SDK 定位失败时，只有位置是 FromSyncSuccess 的才会影响 UI 状态。
        _childLocation.value?.let {
            if (it.isSyncSuccess) {
                _locationStatus.postValue(LocationLoadingStatus(isLoading = false, locationError = false))
                if (success) {
                    _showLocationSuccessMessage.postValue(1)
                }
            }
        }
    }

    fun refreshLocation() {
        _locationStatus.value.let {
            val value = _childLocation.value
            if ((it == null || !it.isLoading) && value != null) {
                sendSyncPositionInstruction(value.deviceId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable?.dispose()
    }

}

data class ChildWithDevice(
        val child: Child?,
        val device: Device?
)

data class LocationLoadingStatus(
        val isLoading: Boolean = false,
        val locationError: Boolean = false
)

private data class LocationStatus(
        val deviceId: String,
        val childLocation: ChildLocation? = null,
        val isSyncSuccess: Boolean = false
)