package com.gwchina.parent.binding.common

import android.os.Bundle
import android.os.Parcelable
import com.gwchina.parent.binding.presentation.ChildInfo
import kotlinx.android.parcel.Parcelize

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-12 16:57
 */
@Parcelize
data class BindingProcessStatus(
        var newChildInfo: ChildInfo? = null,
        var selectedChildId: String? = null
) : Parcelable

class BindingProcessStatusKeeper {

    companion object {
        private const val KEY_FOR_STATUS = "key_for_status"
    }

    private lateinit var bindingProcessStatus: BindingProcessStatus

    fun restore(savedInstanceState: Bundle?) {
        bindingProcessStatus = savedInstanceState?.getParcelable(KEY_FOR_STATUS) ?: BindingProcessStatus()
    }

    fun save(outState: Bundle?) {
        outState?.putParcelable(KEY_FOR_STATUS, bindingProcessStatus)
    }

    fun setAddingDeviceForExistChild(childUserId: String) {
        bindingProcessStatus.newChildInfo = null
        bindingProcessStatus.selectedChildId = childUserId
    }

    fun setAddingDeviceForNewChild(childInfo: ChildInfo) {
        bindingProcessStatus.newChildInfo = childInfo
        bindingProcessStatus.selectedChildId = null
    }

    val newChildInfo: ChildInfo?
        get() = bindingProcessStatus.newChildInfo

    val selectedChildId: String?
        get() = bindingProcessStatus.selectedChildId

}