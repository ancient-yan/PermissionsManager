package com.gwchina.parent.main.presentation.device

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.main.data.MainRepository
import com.gwchina.sdk.base.data.app.AppDataSource
import javax.inject.Inject

/**
 *
 *@author Wangwb
 *        Date : 2018/12/24 9:13 PM
 */
open class DeviceViewModel @Inject constructor(
        private val mainRepository: MainRepository,
        appDataSource: AppDataSource
) : ArchViewModel() {

    private val _sendSmsCode: MutableLiveData<Resource<Any>> = MutableLiveData()
    val sendSmsCode: LiveData<Resource<Any>>
        get() = _sendSmsCode

    private val _unBindChild: MutableLiveData<Resource<Any>> = MutableLiveData()
    val unBindChild: LiveData<Resource<Any>>
        get() = _unBindChild

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser().distinct())

    fun sendSmsCode(phoneNumber: String) {
        if (_sendSmsCode.value?.isLoading == true) {
            return
        }

        _sendSmsCode.value = Resource.loading()

        mainRepository.sendUnbindSmsCode(phoneNumber)
                .subscribe({
                    _sendSmsCode.postValue(Resource.success())
                }, {
                    _sendSmsCode.postValue(Resource.error(it))
                })

    }

    fun unbindDevice(smsCode: String = "", childUserId: String, childDeviceId: String) {
        _unBindChild.value = Resource.loading()

        mainRepository.unbindDevice(childUserId, childDeviceId, smsCode)
                .autoDispose()
                .subscribe(
                        {
                            _unBindChild.postValue(Resource.success())
                        },
                        {
                            _unBindChild.postValue(Resource.error(it))
                        })
    }

}