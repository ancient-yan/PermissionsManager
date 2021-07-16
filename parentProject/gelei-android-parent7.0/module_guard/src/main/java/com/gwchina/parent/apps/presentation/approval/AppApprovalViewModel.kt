package com.gwchina.parent.apps.presentation.approval

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.gwchina.parent.apps.data.AppGuardRepository
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.models.TimePart
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-01-02 15:26
 */
class AppApprovalViewModel @Inject constructor(
        private val appGuardRepository: AppGuardRepository,
        @Named(CHILD_USER_ID_KEY) private val childUserId: String,
        @Named(DEVICE_ID_KEY) private val childDeviceId: String
) : ArchViewModel() {

    /**禁止可用和限时可用最大个数限制*/
    val maxCount: Int
        get() = AppContext.appDataSource().user().vipRule?.app_forbidden_timelimit_count ?: 0

    fun updateAppRule(ruleId: String, ruleType: String, usableDuration: String, timeParts: List<TimePart>, forAppApproval: Boolean): LiveData<Resource<Any>> {
        return appGuardRepository.updateAppRule(childUserId, childDeviceId, ruleId, ruleType, usableDuration, timeParts, isApproval = forAppApproval).toResourceLiveData()
    }

}