package com.gwchina.parent.apps.presentation.guide

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.apps.common.FREELY_SYS_LIST
import com.gwchina.parent.apps.common.MAX_USABLE_DURATION_SECOND
import com.gwchina.parent.apps.common.generateRuleIds
import com.gwchina.parent.apps.common.isMustFreeUsable
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.data.AppGuardRepository
import com.gwchina.parent.apps.data.GuideAppListResponse
import com.gwchina.parent.apps.data.SetAppRulesRequest
import com.gwchina.parent.apps.data.SetAppRulesRequest.SingleRule
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.findDevice
import io.reactivex.Completable
import javax.inject.Inject
import javax.inject.Named

/**
 * 引导流程 ViewModel
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-24 14:28
 */
class GuideFlowViewModel @Inject constructor(
        private val appGuardRepository: AppGuardRepository,
        private val appDataSource: AppDataSource,
        @Named(CHILD_USER_ID_KEY) val childUserId: String,
        @Named(DEVICE_ID_KEY) val childDeviceId: String
) : ArchViewModel() {

    private val _appList = MutableLiveData<Resource<List<App>>>()

    val appList: LiveData<Resource<List<App>>>
        get() = _appList

    val device: Device
        get() = appDataSource.user().findDevice(childDeviceId) ?: throw NullPointerException("device cannot be found")

    val maxCount: Int
        get() = appDataSource.user().vipRule?.app_forbidden_timelimit_count ?: 0

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser())

    private val _freelyList = mutableListOf<App>()

    private val _limitedList = mutableListOf<App>()

    private val _disableList = mutableListOf<App>()

    init {
        loadGuideFlowData()
    }

    fun isLimitDisable(count: Int): Boolean {
        return maxCount != 0 && count >= maxCount
    }

    fun isLimit(count: Int): Boolean {
        return isLimitDisable(count + _disableList.size)
    }

    private fun loadGuideFlowData() {
        _appList.value = Resource.loading()

        appGuardRepository.appRulesGuideList(childUserId, childDeviceId)
                .map {
                    if (it.isPresent) {
                        flatMapAppListResponse(it.get())
                    } else {
                        emptyList()
                    }
                }
                .subscribe(
                        {
                            _appList.postValue(Resource.success(it))
                        },
                        {
                            _appList.postValue(Resource.error(it))
                        }
                )
    }

    private fun flatMapAppListResponse(appListResponse: GuideAppListResponse): List<App> {
        return appListResponse.freely_list
                .toMutableList()
                .apply {
                    forEach {
                        it.p_type = FREELY_SYS_LIST
                    }
                    addAll(appListResponse.soft_list.flatMap { it.group_list })
                }
    }

    fun setFreelyList(appList: List<App>) {
        _freelyList.clear()
        _freelyList.addAll(appList)
    }

    fun setLimitedList(appList: List<App>) {
        _limitedList.clear()
        _limitedList.addAll(appList)
    }

    fun setDisableList(appList: List<App>) {
        _disableList.clear()
        _disableList.addAll(appList)
    }

    fun appListMustFreeUsable(): List<App> {
        return appList.value?.orElse(null)?.filter { it ->
            !it.isMustFreeUsable()
        }?: emptyList()
    }

    fun appListFree(): List<App> {
        return appList.value?.orElse(null)?.filter { it ->
            !isListLimitedSoftware(it) && !isDisableSoftware(it)
        }?: emptyList()
    }

    fun appListLimited(): List<App> {
        return appList.value?.orElse(null)?.filter {
            !isDisableSoftware(it) && !it.isMustFreeUsable()
        } ?: emptyList()
    }

    private fun isListLimitedSoftware(app: App) = _limitedList.contains(app)

    private fun isFreeSoftware(app: App) = _freelyList.contains(app)

    private fun isDisableSoftware(app: App) = _disableList.contains(app)

    fun submit(): Completable {
        val setAppRulesRequest = buildSetAppRulesRequest() ?: return Completable.complete()
        return appGuardRepository.saveGuideFlowAppRules(childUserId, childDeviceId, setAppRulesRequest)
    }

    private fun buildSetAppRulesRequest(): SetAppRulesRequest? {
        val setAppRulesRequest = SetAppRulesRequest()
        //禁止使用
        setAppRulesRequest.forbidRuleIds = _disableList.generateRuleIds()
        //分组限时可用
        setAppRulesRequest.groupRuleList = emptyList()
        //限时可用
        setAppRulesRequest.singleRuleList = _limitedList.map {
            SingleRule(it.rule_id, if (it.used_time_perday == 0) MAX_USABLE_DURATION_SECOND else it.used_time_perday)
        }

        //自由可用
        setAppRulesRequest.freelyRuleIds = appListFree().generateRuleIds()

        return setAppRulesRequest
    }

    fun reload() {
        loadGuideFlowData()
    }

}