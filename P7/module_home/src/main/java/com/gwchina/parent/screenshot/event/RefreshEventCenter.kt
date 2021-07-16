package com.gwchina.parent.screenshot.event

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
class RefreshEventCenter @Inject constructor() {

    private val _refreshDataEvent = SingleLiveData<List<String>>()

    fun setRefreshEvent(recordIds: List<String>) {
        _refreshDataEvent.postValue(recordIds)
    }

    /**
     * 记录本次被删除的recordId
     */
    fun getRefreshEvent(): LiveData<List<String>> {
        return _refreshDataEvent
    }

}