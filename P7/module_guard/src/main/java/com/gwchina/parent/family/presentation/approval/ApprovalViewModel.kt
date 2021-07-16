package com.gwchina.parent.family.presentation.approval

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.SchedulerProvider
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.family.data.FamilyPhoneRepository
import com.gwchina.parent.family.data.event.RefreshRedDotEventCenter
import com.gwchina.parent.family.data.model.Approval
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import io.reactivex.Flowable
import javax.inject.Inject
import javax.inject.Named

class ApprovalViewModel @Inject constructor(
        @Named(CHILD_USER_ID_KEY) private val childUserId: String,
        private val schedulerProvider: SchedulerProvider,
        private val familyPhoneRepository: FamilyPhoneRepository,
        internal val refreshRedDotEventCenter: RefreshRedDotEventCenter
) : ArchViewModel() {

    fun getApprovalRecord(): Flowable<Optional<List<Approval>>> {
        return familyPhoneRepository.getApprovalRecord(childUserId)
                .observeOn(schedulerProvider.ui())
    }

    fun approvalPhone(record_id: String, rule_type: String): LiveData<Resource<Any>> {
        return familyPhoneRepository.approvalPhone(record_id, rule_type, childUserId)
                .subscribeOn(schedulerProvider.io())
                .toResourceLiveData()
    }
}