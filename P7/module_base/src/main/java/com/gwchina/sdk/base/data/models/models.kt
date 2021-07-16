package com.gwchina.sdk.base.data.models

import android.os.Parcelable
import com.gwchina.sdk.base.data.api.DEVICE_STATUS_OFFLINE
import com.gwchina.sdk.base.data.api.FLAG_NEGATIVE
import com.gwchina.sdk.base.data.api.isFlagPositive
import kotlinx.android.parcel.Parcelize

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-15 14:08
 */
data class InstructionState(
        val device_state: Int? = DEVICE_STATUS_OFFLINE,
        val syn_flag: Int? = FLAG_NEGATIVE
) {

    /*0 未同步 1 已同步*/
    fun isSynced() = isFlagPositive(syn_flag)

}

data class DeviceId(val device_id: String)

data class SyncChildrenResponse(val child_device_list: List<Child> = emptyList(), val default_child_user_id: String?, val default_child_device_id: String?)

data class AllInstructionState(
        val guard_level_syn: Int = FLAG_NEGATIVE,
        val guard_phone_syn: Int = FLAG_NEGATIVE,
        val guard_soft_syn: Int = FLAG_NEGATIVE,
        val guard_time_syn: Int = FLAG_NEGATIVE,
        val guard_url_syn: Int = FLAG_NEGATIVE
) {

    fun isAllStateSynced(): Boolean {
        return isFlagPositive(guard_level_syn)
                && isFlagPositive(guard_phone_syn)
                && isFlagPositive(guard_soft_syn)
                && isFlagPositive(guard_time_syn)
                && isFlagPositive(guard_url_syn)
    }

}

/**
 * 手机权限详情
 */
data class PrivilegeData(val childVersion:String?,val privilegeList:List<PermissionDetail>?)

@Parcelize
data class PermissionDetail(val privilege_name: String?, val state: Int, val is_must: Int) : Parcelable

