package com.gwchina.sdk.base.router

import android.os.Parcelable
import com.gwchina.sdk.base.data.api.APP_RULE_TYPE_DEFAULT
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppInfo(
        val bundle_id: String = "",
        val rule_id: String = "",
        val rule_type: Int = APP_RULE_TYPE_DEFAULT,
        val app_icon: String? = null,
        val app_name: String? = null,
        val type_name: String? = null,
        val used_time_per_day: Int = 0
) : Parcelable

@Parcelize
data class ChildDeviceInfo(
        val sessionId: String,
        val age: Int,
        val deviceType: String
) : Parcelable

@Parcelize
data class SelectedLevelInfo(
        val sessionId: String,
        val level: Int,
        val guardItem: List<String>
) : Parcelable

@Parcelize
data class MigrationInfo(
        val greed_box_init_setting: String,
        val not_upgrade_device_list: List<String>
) : Parcelable

