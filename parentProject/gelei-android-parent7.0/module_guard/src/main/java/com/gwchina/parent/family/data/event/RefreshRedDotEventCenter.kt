package com.gwchina.parent.family.data.event

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
class RefreshRedDotEventCenter @Inject constructor() {

    private val _refreshRedDotEvent = SingleLiveData<Boolean>()

    fun setRefreshRedDotEvent(show: Boolean) {
        _refreshRedDotEvent.postValue(show)
    }

    val getRefreshRedDotEvent: LiveData<Boolean>
        get() = _refreshRedDotEvent

}