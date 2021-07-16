package com.gwchina.parent.times.data

import com.gwchina.sdk.base.data.api.FLAG_NEGATIVE
import com.gwchina.sdk.base.data.models.TimePart

data class TimeGuardPlanId(val rule_id: String, val what_day: Int)

data class TimeGuardDailyPlan(
        /**规则id*/
        var rule_id: String = "",
        /**星期几，1-7*/
        val what_day: Int = 0,
        /**单位秒*/
        val enabled_time: Int = 0,
        /**时间守护片段*/
        val time_parts: List<TimePart>? = null,//传参时服务器需要的时间守护片段字段名
        val rule_time_fragment: List<TimePart>? = null//为了兼容服务器返回的 rule_time_fragment 字段名
) {

    fun getTimeParts(): List<TimePart>? {
        if (time_parts == null) {
            return rule_time_fragment
        }
        return time_parts
    }

}

//ios设备不需要上传enabled_time
data class TimeGuardDailyPlanIos(
        /**规则id*/
        var rule_id: String = "",
        /**星期几，1-7*/
        val what_day: Int = 0,
        /**时间守护片段*/
        private val time_parts: List<TimePart>? = null,//传参时服务器需要的时间守护片段字段名
        private val rule_time_fragment: List<TimePart>? = null//为了兼容服务器返回的 rule_time_fragment 字段名
)

data class TimeGuardPlan(
        val batch_id: String = "",
        val batch_name: String? = "",
        //备用计划在多少设备上启动
        val batch_tag: Int = 0,
        val weekly_time: Int = 0,
        val enabled: Int = FLAG_NEGATIVE,
        /*备用计划时*/
        private val batch_list: List<TimeGuardDailyPlan>? = null,
        /*时间表时*/
        private val rule_time: List<TimeGuardDailyPlan>? = null
) {

    fun getDailyPlans(): List<TimeGuardDailyPlan>? {
        if (batch_list == null) {
            return rule_time
        }
        return batch_list
    }

}
