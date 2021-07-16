package com.gwchina.parent.family.presentation.home

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.SchedulerProvider
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.family.common.FamilyEventCenter
import com.gwchina.parent.family.data.FamilyPhoneRepository
import com.gwchina.parent.family.data.event.RefreshRedDotEventCenter
import com.gwchina.parent.family.data.model.FamilyPhoneInfo
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.app.AppDataSource
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

class FamilyPhoneViewModel @Inject constructor(
        @Named(CHILD_USER_ID_KEY) val childUserId: String,
        @Named(DEVICE_ID_KEY) val childDeviceId: String,
        private val schedulerProvider: SchedulerProvider,
        val appDataSource: AppDataSource,
        private val familyPhoneRepository: FamilyPhoneRepository,
        internal val refreshRedDotEventCenter: RefreshRedDotEventCenter,
        internal val familyEventCenter: FamilyEventCenter
) : ArchViewModel() {

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser())

    val groupPhones: MutableList<GroupPhone> = mutableListOf()
    var has_no_approved: String = "0"//是否存在待审批
    var is_call_in: String = "0"//是否限制呼入（可接听范围受限制）
    var is_call_out: String = "0"//是否限制呼出（可拨打范围受限制）

    private val _familyPhoneInfoLiveData = MutableLiveData<Resource<FamilyPhoneInfo>>()
    internal val familyPhoneInfoLiveData: LiveData<Resource<FamilyPhoneInfo>>
        get() = _familyPhoneInfoLiveData

    private val _groupPhoneLiveData = MutableLiveData<Resource<List<GroupPhone>>>()
    internal val groupPhoneLiveData: LiveData<Resource<List<GroupPhone>>>
        get() = _groupPhoneLiveData

    fun setFamilyPhoneGuard(enabled: Boolean?,
                            is_call_out: Boolean?,
                            is_call_in: Boolean?): LiveData<Resource<Any>> {

        return familyPhoneRepository.setFamilyPhoneGuard(childUserId, enabled, is_call_out, is_call_in)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()

    }

    fun getAllGroupPhone(showLoading: Boolean = false) {
        if (showLoading)
            _groupPhoneLiveData.value = Resource.loading()
        familyPhoneRepository.getAllGroupPhone(childUserId)
                .subscribeOn(schedulerProvider.io())
                .autoDispose()
                .subscribe({
                    val tempDatas = it.orElse(listOf())
                    groupPhones.clear()
                    groupPhones.addAll(tempDatas)
                    _groupPhoneLiveData.postValue(Resource.success(tempDatas))
                }, {
                    _groupPhoneLiveData.postValue(Resource.error(it))
                })

    }


    fun getGroupPhone(group_id: String): Observable<Optional<GroupPhone>> {
        return familyPhoneRepository.getGroupPhone(childUserId, group_id)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
    }

    fun addFamilyPhone(phone: String,
                       phone_remark: String,
                       group_name: String?,
                       group_id: String?): LiveData<Resource<Any>> {

        return familyPhoneRepository.addFamilyPhone(phone, phone_remark, group_name, group_id, childUserId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()

    }


    fun editFamilyPhone(phone: String,
                        phone_remark: String,
                        group_id: String?,
                        rule_id: String): LiveData<Resource<Any>> {

        return familyPhoneRepository.editFamilyPhone(phone, phone_remark, group_id, rule_id, childUserId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()

    }


    fun delFamilyPhone(rule_id: String): LiveData<Resource<Any>> {
        return familyPhoneRepository.delFamilyPhone(rule_id, childUserId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()
    }


    fun addGroupAndRefreshGroupList(group_name: String): LiveData<Resource<List<GroupPhone>>> {
        return familyPhoneRepository.addGroup(group_name, childUserId)
                .subscribeOn(schedulerProvider.io())
                .flatMap {
                    familyPhoneRepository.getAllGroupPhone(childUserId)
                            .map {
                                val datas = it.orElse(listOf())
                                groupPhones.clear()
                                groupPhones.addAll(datas)
                                datas
                            }.toObservable()
                }
                .toResourceLiveData()

    }

    fun addGroup(group_name: String): LiveData<Resource<Any>> {
        return familyPhoneRepository.addGroup(group_name, childUserId)
                .ignoreElements()
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()
    }


    fun delGroup(group_ids: String): LiveData<Resource<Any>> {
        return familyPhoneRepository.delGroup(group_ids, childUserId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()

    }

    fun updateGroup(group_ids: String,//分组ID  多个用“,”隔开
                    is_call_out: String?,// 是否限制呼出 1是0否
                    is_call_in: String?,
                    group_name: String?): LiveData<Resource<Any>> {
        return familyPhoneRepository.updateGroup(group_ids, group_name, is_call_out, is_call_in, childUserId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()
    }


    fun getFamilyPhoneInfo(needRefreshGroupManager: Boolean = false) {
        _familyPhoneInfoLiveData.value = Resource.loading()
        familyPhoneRepository.getFamilyPhoneInfo(childUserId)
                .subscribeOn(schedulerProvider.io())
                .autoDispose()
                .subscribe({
                    val tempDatas = it.orElse(FamilyPhoneInfo())
                    has_no_approved = tempDatas.has_no_approved
                    is_call_in = tempDatas.is_call_in
                    is_call_out = tempDatas.is_call_out
                    groupPhones.clear()
                    groupPhones.addAll(tempDatas.group_phone_list)
                    _familyPhoneInfoLiveData.postValue(Resource.success(tempDatas))
                    if (needRefreshGroupManager) {
                        getAllGroupPhone(false)
                    }
                }, {
                    _familyPhoneInfoLiveData.postValue(Resource.error(it))
                })
    }
}