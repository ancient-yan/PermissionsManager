package com.gwchina.parent.main.presentation.approval

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import com.android.base.app.dagger.ContextType
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.MainRepository
import com.gwchina.parent.main.data.SoftApproval
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO
import com.gwchina.parent.main.presentation.home.HomeMapper
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-22 14:05
 */
class AppApprovalViewModel @Inject constructor(
        private val mainRepository: MainRepository,
        private val homeDataMapper: HomeMapper,
        @ContextType private val context: Context
) : ArchViewModel() {

    private val _appApprovalList = MutableLiveData<Resource<List<Any>>>()
    private val _forbidding = MutableLiveData<Resource<String>>()

    val appApprovalList: LiveData<Resource<List<Any>>>
        get() = _appApprovalList

    val forbidding: LiveData<Resource<String>>
        get() = _forbidding

    fun refresh() {
        mainRepository.allPendingApprovalAppList()
                .subscribe(
                        {
                            _appApprovalList.postValue(Resource.success(transform(it.orElse(null))))
                        },
                        {
                            _appApprovalList.postValue(Resource.error(it))
                        }
                )
    }

    private fun transform(list: SoftApproval?): List<Any> {
        return homeDataMapper.convertSoftApproval(list)
    }

    fun forbidNewApp(app: ApprovalInfoVO.SoftWrapper) {
        _forbidding.postValue(Resource.loading())

        mainRepository.prohibitNewApp(app.soft.rule_id, app.childUserId, app.childDeviceId)
                .subscribe(
                        {
                            _forbidding.postValue(Resource.success(context.getString(R.string.x_add_to_y_list_mask, app.soft.soft_name, context.getString(R.string.disabled))))
                            refresh()
                        },
                        {
                            _forbidding.postValue(Resource.error(it))
                        }
                )
    }

}