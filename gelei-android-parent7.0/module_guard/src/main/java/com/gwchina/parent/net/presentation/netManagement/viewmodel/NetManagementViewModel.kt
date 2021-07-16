package com.gwchina.parent.net.presentation.netManagement.viewmodel

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.SchedulerProvider
import com.android.sdk.net.kit.optionalExtractor
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.net.data.NetGuardRepository
import com.gwchina.parent.net.data.model.GuardRecordMapper
import com.gwchina.parent.net.data.model.SiteInfo
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEFAULT_PAGE_SIZE
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Named

class NetManagementViewModel @Inject constructor(
        private val netGuardRepository: NetGuardRepository,
        private val schedulerProvider: SchedulerProvider,
        @Named(CHILD_USER_ID_KEY) private val childUserId: String,
        @Named(DEVICE_ID_KEY) private val childDeviceId: String
) : ArchViewModel() {


    fun getSiteList(list_type: String,
                    pageStart: Int = 1,
                    pageNum: Int = DEFAULT_PAGE_SIZE): Flowable<Optional<List<SiteInfo>>> {

        return netGuardRepository.getSiteList(list_type, pageStart, pageNum, childUserId, childDeviceId)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
    }

    fun addUrl(rule_type: String,
               url: String,
               url_name: String?,
               list_type: String): LiveData<Resource<Any>> {
        return netGuardRepository.addUrl(rule_type, url, url_name, list_type, childUserId, childDeviceId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()
    }


    fun updateUrl(rule_type: String,
                  url: String,
                  url_name: String?,
                  rule_id: String,
                  list_type: String): LiveData<Resource<Any>> {

        return netGuardRepository.updateUrl(rule_type, url, url_name, rule_id, list_type, childUserId, childDeviceId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()

    }


    fun deleteUrls(rule_id_list: List<String>): LiveData<Resource<Any>> {

        return netGuardRepository.deleteUrls(rule_id_list, childUserId, childDeviceId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()
    }


}