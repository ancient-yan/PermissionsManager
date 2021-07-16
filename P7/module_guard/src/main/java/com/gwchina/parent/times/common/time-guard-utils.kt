package com.gwchina.parent.times.common

import android.content.Context
import com.android.base.kotlin.removeWhich
import com.android.base.utils.android.ResourceUtils.getString
import com.android.base.utils.android.ResourceUtils.getStringArray
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.utils.*
import com.gwchina.sdk.base.widget.dialog.showSelectDurationDialog

/**代表未设置的可用时长*/
internal const val NOT_SET_ENABLE_TIME = -1

/**从选择的收入日[days]生成对应的文字描述，比如 `[1,2,3,4]` 对应`[周一/周二/周三/周四] `，注意，[days] 必须是已经按照 1-7 排好序的*/
internal fun generateDaysOfWeekText(days: List<Int>): CharSequence {
    if (days.isEmpty()) {
        return ""
    }

    val dayOfWeek = getStringArray(R.array.guard_day_of_week)

    return days.joinToString(separator = "/") {
        getString(R.string.week) + dayOfWeek[it - 1]
    }

}

/**计算可用时段总时长，单位是分钟*/
fun calculateUsablePeriodTotalMinutes(list: List<TimePeriod>): Int {
    if (list.isEmpty()) {
        return 0
    }
    var totalTime = 0
    for (guardPeriod in list) {
        totalTime += guardPeriod.end.timeQuantity() - guardPeriod.start.timeQuantity()
    }
    return totalTime * DEFAULT_GUARD_TIME_UNIT
}

/**计算可用时段总时长，单位是秒*/
fun calculateUsablePeriodTotalSeconds(list: List<TimePeriod>?): Int {
    if (list.isNullOrEmpty()) {
        return 0
    }
    return calculateUsablePeriodTotalMinutes(list) * 60
}

/**提交时间计划时，不管是否设置了可用时长，都要要确保可用时长不超过时段时长，且如果可用时长为0，应该将其设置为时段时长*/
internal fun checkUsableDurationWhenSettingTimePlan(usableDurationSeconds: Int, selectPeriod: List<TimePeriod>): Int {
    val totalSegmentDurationSeconds = calculateUsablePeriodTotalSeconds(selectPeriod)
    return if (totalSegmentDurationSeconds == 0) {
        if (usableDurationSeconds == NOT_SET_ENABLE_TIME) {
            0
        } else {
            usableDurationSeconds
        }
    } else {
        if (usableDurationSeconds > totalSegmentDurationSeconds || usableDurationSeconds == NOT_SET_ENABLE_TIME) {
            totalSegmentDurationSeconds
        } else {
            usableDurationSeconds
        }
    }
}


/**[dayName]可能是{一、二、...、六、日、今天}，如果[dayName] == "今天"，则直接返回，否则在[dayName]前加上 ”周“ 前缀*/
internal fun checkDayNameIfToday(dayName: String): String {
    return if (dayName == getString(R.string.today)) {
        dayName
    } else {
        getString(R.string.week_mask, dayName)
    }
}

internal fun getDayOfWeekName(dayOfWeek: Int): String {
    if (dayOfWeek in 1..7) {
        return getString(R.string.week) + getStringArray(R.array.guard_day_of_week)[dayOfWeek - 1]
    }
    return ""
}

/**选择可用时长时，如果没有初始值，则给一个默认的时长，目前时 2 小时*/
private fun defaultDuration(initUsableDuration: Int): Time {
    return with(initUsableDuration) {
        if (this >= 0) {
            Time.fromSeconds(this)
        } else {
            //默认时长，两个小时
            Time(2, 0)
        }
    }
}

/**
 * 选择可用时长弹框，通用逻辑，可用于：
 *
 * - 制定计划
 * - 时间表
 * - 编辑备用计划
 *
 * 参数：
 *
 * - [selectedDuration] 已经设置的可用时长，没有则传 0。
 * - [timePeriods] 设置的时间段，没有则传空。
 *
 * 具体逻辑：如果设置了可用时段，则选择的可用时长不允许超过设置的可用时段的总时长。
 */
internal fun showSelectingUsableDurationDialogForTimePlan(context: Context, selectedDuration: Int, timePeriods: List<TimePeriod>?, onNewDuration: (Int) -> Unit) {
    val usableDurationSeconds = defaultDuration(selectedDuration)
    val periodSeconds = calculateUsablePeriodTotalSeconds(timePeriods)

    showSelectDurationDialog(context) {
        tips = if (periodSeconds > 0) {
            context.getString(R.string.usable_period_total_duration_mask, formatSecondsToTimeText(periodSeconds))
        } else {
            ""
        }
        initDuration = usableDurationSeconds
        positiveListener = { _, time ->
            val checkedResult = time.toSeconds()
            onNewDuration(checkedResult)
        }
    }
}

/**计算一周总的可用时长：
 *
 * - [plans] 一周中每天的计划
 * - [isAndroid] true 表示 android，false 表示 IOS
 */
fun calculateWeeklyTotalUsableDuration(plans: List<TimeGuardDailyPlanVO>, isAndroid: Boolean): Int {
    val daysNotPlaned = (1..7).toMutableList().apply {
        val planedDays = plans.map { it.dayOfWeek }
        removeWhich { planedDays.contains(it) }
    }

    return if (isAndroid) {
        plans.fold(0, { acc, plan -> acc + plan.enabledTime })
    } else {
        plans.fold(0, { acc, plan -> acc + calculateUsablePeriodTotalSeconds(plan.timePeriodList) })
    } + (daysNotPlaned.size * TOTAL_SECONDS_OF_ONE_DAY)

}