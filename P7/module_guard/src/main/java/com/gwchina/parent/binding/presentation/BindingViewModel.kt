package com.gwchina.parent.binding.presentation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.subscribeIgnoreError
import com.gwchina.parent.binding.common.BindingProcessStatusKeeper
import com.gwchina.parent.binding.data.BindingRepository
import com.gwchina.sdk.base.data.api.AD_POSITION_BINDING
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.api.isIOS
import com.gwchina.sdk.base.data.api.isYes
import com.gwchina.sdk.base.data.app.AdvertisingFilter
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Advertising
import com.gwchina.sdk.base.data.models.VipRule
import com.gwchina.sdk.base.data.models.isMember
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-23 11:43
 */
class BindingViewModel @Inject internal constructor(
        private val bindingRepository: BindingRepository,
        private val appDataSource: AppDataSource,
        private val keeper: BindingProcessStatusKeeper
) : ArchViewModel() {

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser())

    val vipRule: VipRule?
        get() = appDataSource.user().vipRule

    val isMember: Boolean
        get() = appDataSource.user().vipRule?.home_mine_add_device_enabled_minimum_level == FLAG_POSITIVE_ACTION

    private val _ad = MutableLiveData<List<Advertising>>()

    val advertising: LiveData<List<Advertising>?>
        get() = _ad

    private val _bindingResult = SingleLiveData<Resource<BindingResult>>()

    internal val bindingResult: LiveData<Resource<BindingResult>>
        get() = _bindingResult

    private val advertisingFilter = object : AdvertisingFilter {
        override fun filter(ad: Advertising): Boolean {
            return ad.ad_position == AD_POSITION_BINDING
        }
    }

    init {
        appDataSource.advertisingList(advertisingFilter)
                .autoDispose()
                .subscribeIgnoreError {
                    _ad.postValue(it.orElse(null))
                }
    }

    fun bindDevice(childDeviceInfo: DeviceInfo) {
        if (bindingResult.value?.isLoading == true) {
            return
        }

        _bindingResult.value = Resource.loading()

        val newChildInfo = keeper.newChildInfo
        val selectedChildId = keeper.selectedChildId

        when {
            newChildInfo != null -> bindingRepository.bindNewChild(newChildInfo, childDeviceInfo)
            selectedChildId != null -> bindingRepository.addDeviceForChild(selectedChildId, childDeviceInfo)
            else -> throw NullPointerException()
        }.subscribe(
                {
                    _bindingResult.postValue(Resource.success(BindingResult(childDeviceInfo.deviceSn, isIOS(it.device_type), isYes(it.first_bind_device), isYes(it.custom_device_flag))))
                },
                {
                    _bindingResult.postValue(Resource.error(it))
                }
        )

    }


}

data class BindingResult(
        val deviceSn: String,
        val isIOS: Boolean,
        val isFirstBinding: Boolean,
        val isCustomDevice: Boolean
)