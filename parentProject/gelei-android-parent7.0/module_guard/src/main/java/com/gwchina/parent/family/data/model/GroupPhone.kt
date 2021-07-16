package com.gwchina.parent.family.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GroupPhone(
        var count: Int = 0,
        var device_id: String = "",
        var group_id: String = "", //分组ID
        var group_name: String? = "", //分组名称
        var is_call_in: String? = "",//是否限制呼入 1是0否
        var is_call_out: String? = "",//是否限制呼出 1是0否
        var is_default: String? = "",//是否系统默认 1是0否
        var phone_list: List<Phone>? = null,// 分组的号码列表
        var update_time: Long = 0, //分组的更新时间
        var user_id: String = "", //用户ID
        var isSelected: Boolean = false
) : Parcelable

@Parcelize
data class Phone(
        val device_id: String = "",
        val group_id: String = "",//分组ID
        val is_call_in: String? = "",
        val is_call_out: String? = "",
        val phone: String? = "",
        val phone_remark: String? = "",//号码备注
        val record_id: String = "",//分组号码关联ID
        val rule_id: String = "",
        val update_time: Long = 0,
        val user_id: String = "",
        val ping_yin: String? = "" //汉子首字母
) : Parcelable