package com.gwchina.parent.apps.common

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.dagger.ActivityScope
import com.gwchina.parent.apps.data.App
import javax.inject.Inject


@ActivityScope
class AppEventCenter @Inject constructor() {

    private val appPropertiesUpdatedEvent = SingleLiveData<App>()
    private val appListRefreshEvent = SingleLiveData<Any>()

    fun notifyAppHasBeApproved(app:App) {
        appPropertiesUpdatedEvent.postValue(app)
    }

    fun appApprovedEvent(): LiveData<App> {
        return appPropertiesUpdatedEvent
    }

    fun notifyAppListNeedRefresh() {
        appListRefreshEvent.postValue(1)
    }

    fun appListRefreshEvent(): LiveData<Any> {
        return appListRefreshEvent
    }

}