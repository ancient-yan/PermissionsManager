package com.gwchina.parent.net.presentation.record.viewmodel

import com.android.base.app.mvvm.ArchViewModel
import com.android.base.rx.SchedulerProvider
import com.gwchina.parent.net.data.NetGuardRepository
import com.gwchina.parent.net.data.model.GuardRecord
import com.gwchina.parent.net.data.model.GuardRecordMapper
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEFAULT_PAGE_SIZE
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Named

class NetGuardRecordViewModel @Inject constructor(
        private val netGuardRepository: NetGuardRepository,
        private val schedulerProvider: SchedulerProvider,
        private val guardRecordMapper: GuardRecordMapper,
        @Named(CHILD_USER_ID_KEY) private val childUserId: String,
        @Named(DEVICE_ID_KEY) private val childDeviceId: String
) : ArchViewModel() {

    fun getGuardRecordList(pageStart: Int = 1, pageNum: Int = DEFAULT_PAGE_SIZE): Observable<List<GuardRecord>> {
        return netGuardRepository.getGuardRecordList(pageStart, pageNum, childUserId, childDeviceId)
                .subscribeOn(schedulerProvider.io())
                .map {
                    guardRecordMapper.transformToGuardRecordList(it.orElse(null))
                }
                .observeOn(schedulerProvider.ui())
    }


}