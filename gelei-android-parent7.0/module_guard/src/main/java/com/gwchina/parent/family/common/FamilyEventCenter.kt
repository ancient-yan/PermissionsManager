package com.gwchina.parent.family.common

import android.arch.lifecycle.LiveData
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.dagger.ActivityScope
import com.gwchina.parent.family.data.model.GroupPhone
import javax.inject.Inject

@ActivityScope
class FamilyEventCenter @Inject constructor() {
    private val groupPhoneListRefreshEvent = SingleLiveData<String>()

    private val groupListRefreshEvent = SingleLiveData<List<GroupPhone>>()

    fun notifyGroupPhoneListNeedRefresh() {
        groupPhoneListRefreshEvent.postValue("1")
    }

    fun groupPhoneListRefreshEvent(): LiveData<String> {
        return groupPhoneListRefreshEvent
    }

    fun notifyGroupListNeedRefresh(delGroupPhones: List<GroupPhone>) {
        groupListRefreshEvent.postValue(delGroupPhones)
    }

    fun groupListRefreshEvent(): LiveData<List<GroupPhone>> {
        return groupListRefreshEvent
    }
}