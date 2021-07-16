package com.gwchina.parent.net.presentation.home.viewmodel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.data.onSuccess
import com.android.base.rx.SchedulerProvider
import com.gwchina.parent.net.data.NetGuardRepository
import com.gwchina.parent.net.data.model.AppRuleInfo
import com.gwchina.parent.net.data.model.RuleUrlInfo
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.data.models.isAndroid
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

class NetGuardViewModel @Inject constructor(
        private val netGuardRepository: NetGuardRepository,
        private val schedulerProvider: SchedulerProvider,
        val appDataSource: AppDataSource,
        @Named(CHILD_USER_ID_KEY) val childUserId: String,
        @Named(DEVICE_ID_KEY) val childDeviceId: String
) : ArchViewModel() {

    val childDeviceIsAndroid = appDataSource.user().findDevice(childDeviceId).isAndroid()

    private val _ruleUrlInfo = MutableLiveData<Resource<RuleUrlInfo>>()
    internal val ruleUrlInfo: LiveData<Resource<RuleUrlInfo>>
        get() = _ruleUrlInfo

    init {

        getRuleUrlBaseInfo()
    }

    fun getRuleUrlBaseInfo() {
        _ruleUrlInfo.value = Resource.loading()
        netGuardRepository.getRuleUrlBaseInfo(childUserId, childDeviceId)
                .observeOn(schedulerProvider.computation())
                .map {
                    val error = it.error
                    if (error != null) {
                        Resource.error(error)
                    } else {
                        Resource.success(it.data)
                    }
                }
                .observeOn(schedulerProvider.ui())
                .autoDispose()
                .subscribe({
                    _ruleUrlInfo.value = it
                }, {
                    _ruleUrlInfo.postValue(Resource.error(it))
                })

    }

    fun updateIntelligentControll(isChecked: Boolean): LiveData<Resource<Any>> {

        var pattern_id = ""

        _ruleUrlInfo.value?.onSuccess {
            it?.let {
                pattern_id = it.pattern_id
            }
        }

        return netGuardRepository.updateRuleUrlPattern(pattern_id, isChecked, childUserId, childDeviceId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()
    }

    fun queryAppInfo(bundle_id: String = "com.browser.txtw"): Observable<AppRuleInfo> {
        return netGuardRepository.queryAppInfo(childUserId, childDeviceId, bundle_id)
                .subscribeOn(schedulerProvider.io())
                .map {
                    it.orElse(AppRuleInfo())
                }
                .observeOn(schedulerProvider.ui())

    }
}