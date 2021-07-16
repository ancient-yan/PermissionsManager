package com.gwchina.parent.profile.data

import android.os.Parcelable
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.data.models.Patriarch
import com.gwchina.sdk.base.data.models.TimePeriodRule
import kotlinx.android.parcel.Parcelize

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-11 10:53
 */
@Parcelize
data class DeliveryAddress(
        val address: String? = null,
        val city_area_code: String? = null,
        val district_area_code: String? = null,
        val is_default: String? = null,
        val province_area_code: String? = null,
        val receiver_name: String? = null,
        val receiver_phone: String? = null,
        val record_id: String? = null, //邮编
        val zip_code: String? = null
) : Parcelable

data class PatriarchData(val patriarchDetail: Optional<Patriarch>? = null, val deliveryAddress: Optional<List<DeliveryAddress>>? = null)

/**添加临时可用响应*/
data class TempUsable(
        val device_id: String,
        val enabled: String,
        val rule_id: String,
        val update_time: Any,
        val usable_begin_time: Long,
        val usable_end_time: Long,
        val mode: Int,
        val user_id: String
)

data class TimeGuardPlan(
        val batch_id: String = "",
        val batch_name: String? = "",
        val device_id: String? = "",
        //备用计划在多少设备上启动
        val batch_tag: Int = 0,
        val weekly_time: Int = 0,
        /*时间表时*/
        val rule_time: List<TimePeriodRule>? = null
)
