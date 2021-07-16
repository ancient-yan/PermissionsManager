package com.gwchina.sdk.base.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Advertising(
        val ad_id: String? = "",
        val ad_name: String? = "",
        val ad_photo_url: String? = "",
        val ad_position: String? = "",
        val ad_position_label: String? = "",
        val ad_type: String? = "",
        val ad_type_label: String? = "",
        val begin_time: Long = 0,
        val create_time: Long = 0,
        val end_time: Long = 0,
        val is_listing: String? = "",
        val is_listing_label: String? = "",
        val jump_args: String = "",
        val jump_target: String? = "",
        val row_order: Int = 0,
        val status: String? = ""
) : Parcelable