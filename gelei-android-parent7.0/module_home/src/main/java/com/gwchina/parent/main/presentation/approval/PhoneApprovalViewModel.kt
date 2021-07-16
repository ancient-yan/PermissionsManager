package com.gwchina.parent.main.presentation.approval

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.main.data.MainRepository
import com.gwchina.parent.main.data.PhoneApprovalInfo
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.childCount
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-22 14:05
 */
class PhoneApprovalViewModel @Inject constructor(
        private val mainRepository: MainRepository,
        private val appDataSource: AppDataSource
) : ArchViewModel() {

    val childCount: Int
        get() = appDataSource.user().childCount()

    private val _phoneApprovalList = MutableLiveData<Resource<List<PhoneApprovalInfo>>>()
    private val _approval = MutableLiveData<Resource<Any>>()

    val phoneApprovalList: LiveData<Resource<List<PhoneApprovalInfo>>>
        get() = _phoneApprovalList

    val approval: LiveData<Resource<Any>>
        get() = _approval

    fun refresh() {
        mainRepository.allPendingApprovalPhoneList()
                .subscribe(
                        {
                            _phoneApprovalList.postValue(Resource.success(it.orElse(null)))
                        },
                        {
                            _phoneApprovalList.postValue(Resource.error(it))
                        }
                )
    }

    fun approvalPhone(phoneApprovalInfo: PhoneApprovalInfo, allow: Boolean) {
        _approval.postValue(Resource.loading())

        mainRepository.approvalPhone(phoneApprovalInfo.record_id, allow, phoneApprovalInfo.user_id)
                .subscribe(
                        {
                            _approval.postValue(Resource.success())
                            refresh()
                        },
                        {
                            _approval.postValue(Resource.error(it))
                        }
                )
    }

}