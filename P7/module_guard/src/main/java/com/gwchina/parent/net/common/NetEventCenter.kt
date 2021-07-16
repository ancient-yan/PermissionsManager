package com.gwchina.parent.net.common

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.dagger.ActivityScope
import javax.inject.Inject

@ActivityScope
class NetEventCenter @Inject constructor() {

    private val netDeleteTypeEvent = SingleLiveData<String>()
    private val urlListRefreshEvent = SingleLiveData<String>()

    fun notifyNetDeleteTypeUpdated(type: String) {
        netDeleteTypeEvent.postValue(type)
    }

    fun netDeleteTypeEvent(): LiveData<String> {
        return netDeleteTypeEvent
    }

    fun notifyUrlListNeedRefresh(type: String) {
        urlListRefreshEvent.postValue(type)
    }

    fun urlListRefreshEvent(): LiveData<String> {
        return urlListRefreshEvent
    }
}