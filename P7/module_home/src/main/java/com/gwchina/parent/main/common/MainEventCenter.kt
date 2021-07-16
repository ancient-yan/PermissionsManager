package com.gwchina.parent.main.common

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.dagger.ActivityScope
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-12 17:57
 */
@ActivityScope
class MainEventCenter @Inject constructor() {

    private val _showMineTabRedDot = SingleLiveData<Boolean>()

    fun setMineTabRedDotVisible(show: Boolean) {
        _showMineTabRedDot.postValue(show)
    }

    val showMineTabRedDotEvent: LiveData<Boolean>
        get() = _showMineTabRedDot

}