package com.gwchina.parent.times.presentation.table

import com.gwchina.parent.times.common.TimeGuardDailyPlanVO
import com.gwchina.parent.times.common.calculateWeeklyTotalUsableDuration
import com.gwchina.parent.times.common.checkUsableDurationWhenSettingTimePlan

internal class PlansManager(private val isAndroid: Boolean) {

    private var plans: List<TimeGuardDailyPlanVO> = emptyList()

    fun clearPlan() {
        plans = emptyList()
    }

    val daysPlaned: List<Int>
        get() = plans.map { it.dayOfWeek }

    val daysNotPlaned: List<Int>
        get() = (1..7).toMutableList().apply { removeAll(daysPlaned) }

    fun setPlans(rules: List<TimeGuardDailyPlanVO>) {
        this.plans = rules
    }


    fun get(dayOfWeek: Int): TimeGuardDailyPlanVO? {
        return plans.find {
            it.dayOfWeek == dayOfWeek
        }
    }

    fun calculateTotalUsableDuration(): Int {
        return calculateWeeklyTotalUsableDuration(plans, isAndroid)
    }

    fun plansCanBeSaved(): List<TimeGuardDailyPlanVO> {
        return plans.map {
            it.copy(enabledTime = checkUsableDurationWhenSettingTimePlan(it.enabledTime, it.timePeriodList))
        }
    }

    fun hasPlan(): Boolean {
        return plans.isNotEmpty()
    }

}
