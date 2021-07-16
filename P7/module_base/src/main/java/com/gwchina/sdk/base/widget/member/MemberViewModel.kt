package com.gwchina.sdk.base.widget.member

import android.arch.lifecycle.*
import com.android.base.app.aac.subscribeWithLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.app.AppDataSource

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-28 14:14
 */
internal class MemberViewModel(
        private val memberRepository: MemberRepository,
        appDataSource: AppDataSource
) : ArchViewModel() {

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser().distinct())

    private val _setRetainedDevice = MutableLiveData<Resource<Any>>()

    val settingRetainedDeviceStatus: LiveData<Resource<Any>>
        get() = _setRetainedDevice

    fun setRetainedDevice(deviceId: String) {
        memberRepository.setRetainedDevice(deviceId)
                .subscribeWithLiveData(_setRetainedDevice)
    }

}

class MemberViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        require(modelClass == MemberViewModel::class.java) { "support MemberViewModel only" }

        return MemberViewModel(
                MemberRepository(
                        AppContext.serviceFactory().create(MemberApi::class.java),
                        AppContext.appDataSource()),
                AppContext.appDataSource()) as T
    }

}