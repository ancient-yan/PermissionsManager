package com.gwchina.parent.profile.data

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.dagger.ActivityScope
import javax.inject.Inject

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-29 19:37
 */
@ActivityScope
class RefreshPatriarchInfoEventCenter @Inject constructor() {

    private val _refreshPatriarchInfoEvent = SingleLiveData<Boolean>()

    fun setRefreshAddressEvent(show: Boolean) {
        _refreshPatriarchInfoEvent.postValue(show)
    }

    val getRefreshAddressEvent: LiveData<Boolean>
        get() = _refreshPatriarchInfoEvent

}