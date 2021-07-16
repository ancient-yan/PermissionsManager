package com.gwchina.parent.times.common

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.dagger.ActivityScope
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-16 16:47
 */
@ActivityScope
class TimeEventCenter @Inject constructor() {

    private val _plansChangedEvent = SingleLiveData<Any>()
    private val _sparePlansChangedEvent = SingleLiveData<Any>()

    fun notifyPlansChanged() = _plansChangedEvent.postValue(1)

    fun notifySparePlansChanged() = _sparePlansChangedEvent.postValue(1)

    val onPlansChanged: LiveData<Any>
        get() = _plansChangedEvent

    val onSparePlansChanged: LiveData<Any>
        get() = _sparePlansChangedEvent

}