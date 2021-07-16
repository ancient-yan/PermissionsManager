package com.gwchina.parent.apps.presentation.approval

import com.android.base.app.mvvm.ArchViewModel
import com.android.base.rx.SchedulerProvider
import com.gwchina.parent.apps.data.AppGuardRepository
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEFAULT_PAGE_SIZE
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-05-08 17:01
 */
class AppApprovalRecordViewModel @Inject constructor(
        private val appGuardRepository: AppGuardRepository,
        private val schedulerProvider: SchedulerProvider,
        private val appApprovalRecordMapper: AppApprovalRecordMapper,
        @Named(CHILD_USER_ID_KEY) private val childUserId: String,
        @Named(DEVICE_ID_KEY) private val childDeviceId: String
) : ArchViewModel() {

    fun loadApprovalList(pageStart: Int = 1, pageNum: Int = DEFAULT_PAGE_SIZE): Flowable<List<AppRecordWrapper>> {
        return appGuardRepository.approvalRecordList(childUserId, childDeviceId, pageStart, pageNum)
                .map {
                    appApprovalRecordMapper.transformToAppWrapperList(it.orElse(null))
                }
                .observeOn(schedulerProvider.ui())
    }

}