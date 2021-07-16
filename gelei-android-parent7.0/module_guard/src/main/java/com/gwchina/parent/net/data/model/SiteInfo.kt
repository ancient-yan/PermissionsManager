package com.gwchina.parent.net.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SiteInfo(
        var device_id: String = "",
        var rule_id: String = "",
        var rule_type: String = "",
        var update_time: Long = 0,
        var url: String = "",
        var url_name: String = "",
        var user_id: String = "",
        var isSelected: Boolean = false
) : Parcelable