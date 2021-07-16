package com.gwchina.parent.daily.event

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.dagger.ActivityScope
import javax.inject.Inject

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-16 16:28
 */
@ActivityScope
class RefreshEventCenter @Inject constructor(){
    private val _refreshDailyEvent = SingleLiveData<Int>()

    fun setRefreshDailyEvent(show: Int) {
        _refreshDailyEvent.postValue(show)
    }

    val getRefreshDailyEvent: LiveData<Int>
        get() = _refreshDailyEvent
}