package com.gwchina.parent.times.common

import android.os.Parcelable
import com.gwchina.sdk.base.utils.TimePeriod
import kotlinx.android.parcel.Parcelize

/**天计划*/
@Parcelize
data class TimeGuardDailyPlanVO(
        /**守护计划id*/
        var id: String = "",
        /**星期几*/
        var dayOfWeek: Int = 0,
        /**可用时长，单位是秒*/
        var enabledTime: Int = 0,
        /**可用时段，以十分钟为单位，比如 start = 10，end = 20，表示第 100 分钟到 200 分钟，即 1:40 到 3:20*/
        val timePeriodList: MutableList<TimePeriod> = mutableListOf()
) : Parcelable

/**周计划*/
@Parcelize
data class TimeGuardWeeklyPlanVO(
        val planId: String,
        var planName: String,
        /**是否为备用计划*/
        val isSparePlan: Boolean,
        /***/
        val isMultiDeviceUsing:Boolean,
        /**一周包含的计划*/
        val dailyPlans: MutableList<TimeGuardDailyPlanVO>
) : Parcelable
