package com.gwchina.parent.profile.presentation.child

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.util.Log
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.dagger.ContextType
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.kotlin.ifNonNull
import com.android.base.rx.subscribeIgnoreError
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.profile.data.ProfileRepository
import com.gwchina.parent.profile.data.TimeGuardPlan
import com.gwchina.sdk.base.app.ErrorHandler
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.*
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Wangwb
 *      Email: 253123123@qq.com
 *      Date : 2019-01-01 18:27
 */
class ChildInfoViewModel @Inject constructor(
        @ContextType private val context: Context,
        private val profileRepository: ProfileRepository,
        private val errorHandler: ErrorHandler,
        val appDataSource: AppDataSource,
        @Named(CHILD_USER_ID_KEY) private val childUserId: String
) : ArchViewModel() {

    private var timeRuleLists: List<TimeGuardPlan>? = null

    private val _child = MutableLiveData<Child>()
    val child: LiveData<Child>
        get() = _child

    private val _open: MutableLiveData<OperationStatus> = MutableLiveData()
    val open: LiveData<OperationStatus>
        get() = _open


    private val _close: MutableLiveData<OperationStatus> = MutableLiveData()
    val close: LiveData<OperationStatus>
        get() = _close


    init {
        appDataSource.observableUser()
                .autoDispose()
                .subscribeIgnoreError {
                    val target = if (childUserId.isEmpty()) {
                        it.currentChild
                    } else {
                        it.childList?.find { child -> child.child_user_id == childUserId }
                    }
                    //如果孩子设备列表不为null，则获取孩子设备列表的时间规则
                    target?.device_list.ifNonNull {
                        getChildDeviceTimeRule()
                    }
                    _child.postValue(target)
                }
    }

    fun updateChildInfo(childInfo: ChildInfo): LiveData<Resource<Any>> {
        val childUserId = _child.value?.child_user_id ?: ""
        return profileRepository.updateChildInfo(childUserId, childInfo.nickname, childInfo.sex.toString(), childInfo.birthday, childInfo.grade.toString())
                .toResourceLiveData()
    }

    fun getNextUsablePeriodTime(devideId: String): NextUsablePeriodTime {
        val timePeriodRule = timeRuleLists?.find {
            it.device_id.equals(devideId)
        }?.rule_time
        Log.d("ChildInfoViewModel", "getNextUsablePeriodTime$timePeriodRule")
        return getNextUsablePeriodTime(timePeriodRule)
    }

    fun getNextLookScreenTime(devideId: String): NextUsablePeriodTime {
        val timePeriodRule = timeRuleLists?.find {
            it.device_id.equals(devideId)
        }?.rule_time
        Log.d("ChildInfoViewModel", "getNextLookScreenTime$timePeriodRule")
        return getNextLookScreenTime(timePeriodRule)
    }

    fun deviceIsTimeRule(deviceId: String): Boolean {
        val timePeriodRule = timeRuleLists?.find {
            it.device_id.equals(deviceId)
        }?.rule_time
        Log.d("ChildInfoViewModel", "getNextLookScreenTime$timePeriodRule")
        return !timePeriodRule.isNullOrEmpty()
    }

    private fun updateDeviceInfo(deviceInfo: Device) {
        profileRepository.updateDeviceInfo(deviceInfo)
    }

    private fun getChildDeviceTimeRule() {
        profileRepository.getChildDeviceTimeRule(childUserId)
                .autoDispose()
                .subscribe(
                        {
                            timeRuleLists = it.get()
                        },
                        {
                            errorHandler.handleError(it)
                        }
                )
    }

    fun addTemporarilyPlan(tempUsableMinutes: Int, childDeviceId: String, mode: Int) {
        _open.postValue(OperationStatus(showLoading = true))
        profileRepository.setTempUsable(tempUsableMinutes, childUserId, childDeviceId, mode)
                .autoDispose()
                .subscribe(
                        {
                            if (it.isPresent) {
                                _open.postValue(OperationStatus(message = context.getString(if (mode == ChildInfoFragment.LOCK_SCREEN) R.string.temp_look_screen_send else R.string.temp_availability_send)))

                                //更新缓存数据
                                child.value?.findDevice(childDeviceId).ifNonNull {
                                    val usableResponse = it.get()
                                    val newDeviceInfo = this.copy(
                                            temp_usable_time = TempTimePeriodRule(usableResponse.rule_id, mode, usableResponse.usable_begin_time, usableResponse.usable_end_time)
                                    )
                                    updateDeviceInfo(newDeviceInfo)
                                }
                            } else {
                                _open.postValue(OperationStatus())
                            }
                        },
                        {
                            _open.postValue(OperationStatus(message = errorHandler.createMessage(it)))
                        }
                )
    }

    fun deleteTemporarilyPlan(tempUsableId: String, childDeviceId: String) {
        _close.postValue(OperationStatus(showLoading = true))
        profileRepository.deleteTempUsable(tempUsableId, childUserId, childDeviceId)
                .autoDispose()
                .subscribe(
                        {
                            _close.postValue(OperationStatus(message = context.getString(R.string.close_success)))

                            //更新缓存数据
                            child.value?.findDevice(childDeviceId).ifNonNull {
                                val newDeviceInfo = this.copy(
                                        temp_usable_time = null
                                )
                                updateDeviceInfo(newDeviceInfo)
                            }
                        },
                        {
                            _close.postValue(OperationStatus(message = errorHandler.createMessage(it)))
                        }
                )
    }

    data class ChildInfo(
            var nickname: String = "",
            var sex: Int = 0,
            var birthday: String = "",
            var grade: Int = 0
    )

    data class OperationStatus(
            val showLoading: Boolean = false,
            val message: CharSequence? = null
    )
}

