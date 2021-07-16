package com.gwchina.parent.level.presentation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.SchedulerProvider
import com.gwchina.parent.level.data.LevelRepository
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.api.GUARD_LEVEL_MILD
import com.gwchina.sdk.base.data.api.GUARD_LEVEL_MODERATE
import com.gwchina.sdk.base.data.api.GUARD_LEVEL_SEVERE
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.findChild
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.router.ChildDeviceInfo
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import javax.annotation.Nullable
import javax.inject.Inject
import javax.inject.Named

/**
 * todo：抽取基类，拆分 “设置守护等级” 和 “获取守护等级” 两个逻辑为不同的 ViewModel。
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-27 16:39
 */
class SetGuardLevelViewModel @Inject constructor(
        private val bindingRepository: LevelRepository,
        private val schedulerProvider: SchedulerProvider,
        private val mapper: Mapper,
        appDataSource: AppDataSource,
        //用于给指定的 childDeviceId 设置守护等级
        @Named(CHILD_USER_ID_KEY) private val childUserId: String,
        @Named(DEVICE_ID_KEY) private val childDeviceId: String,
        //用于获取守护等级
        @Nullable val childDeviceInfo: ChildDeviceInfo?,
        val forChoosingLevel: Boolean
) : ArchViewModel() {

    private val _levelItem = MutableLiveData<Resource<List<GuardLevelVO>>>()
    internal val guardLevelList: LiveData<Resource<List<GuardLevelVO>>>
        get() = _levelItem

    private val _setLevelResult = MutableLiveData<Resource<Any>>()
    internal val setLevelResult: LiveData<Resource<Any>>
        get() = _setLevelResult

    private val device: Device? by lazy { if (forChoosingLevel) null else appDataSource.user().findDevice(childDeviceId) }

    private val child: Child? by lazy { if (forChoosingLevel) null else appDataSource.user().findChild(childUserId) }

    internal val hasSelectedLevel: Boolean
        get() = if (forChoosingLevel) {
            false
        } else {
            device?.hasSetLevel() == true
        }

    internal val selectedLevel: Int?
        get() = if (forChoosingLevel) {
            null
        } else {
            device?.guard_level
        }

    internal val childAge: Int
        get() = if (forChoosingLevel) {
            childDeviceInfo?.age ?: 0
        } else {
            child?.age ?: 0
        }

    private val deviceType: String
        get() = if (forChoosingLevel) {
            childDeviceInfo?.deviceType ?: ""
        } else {
            device?.device_type ?: ""
        }

    init {
        loadGuardLevelList()
    }

    private fun loadGuardLevelList() {
        _levelItem.value = Resource.loading()

        bindingRepository.guardItems(childDeviceId, deviceType)
                .observeOn(schedulerProvider.computation())
                .map {
                    mapper.convertToLevelItemVO(it.orElse(null), device?.guard_level, device?.guard_item_list)
                }
                .autoDispose()
                .subscribe(
                        {
                            _levelItem.postValue(Resource.success(it))
                        },
                        {
                            _levelItem.postValue(Resource.error(it))
                        }
                )
    }

    internal fun setLevel(guardLevelVO: GuardLevelVO) {
        when {
            guardLevelVO.guardLevel == GUARD_LEVEL_MILD -> StatisticalManager.onEvent(UMEvent.ClickEvent.GROWTLEVEV_BTN_LIGHTGUARD)
            guardLevelVO.guardLevel == GUARD_LEVEL_MODERATE -> StatisticalManager.onEvent(UMEvent.ClickEvent.GROWTLEVEV_BTN_MODERATEGRARD)
            guardLevelVO.guardLevel == GUARD_LEVEL_SEVERE -> StatisticalManager.onEvent(UMEvent.ClickEvent.GROWTLEVEV_BTN_SERIOUSGRARD)
        }
        _setLevelResult.value = Resource.loading()

        val guardItemIdList = mapper.findItemIdList(guardLevelVO)

        bindingRepository.setLevel(childUserId, childDeviceId, guardLevelVO.id, guardItemIdList)
                .subscribe(
                        {
                            _setLevelResult.postValue(Resource.success())
                        },
                        {
                            _setLevelResult.postValue(Resource.error(it))
                        }
                )
    }

    internal fun hasDifference(guardLevelVO: GuardLevelVO): Boolean {
        val safeDevice = device ?: return true
        return !(guardLevelVO.guardLevel == safeDevice.guard_level /*等级对比*/
                && mapper.findItemCodeList(guardLevelVO).sorted() == safeDevice.guard_item_list?.map { it.guard_item_code }?.sorted()) /*项目对比*/
    }

    fun retryLoadGuardLevel() {
        loadGuardLevelList()
    }

}