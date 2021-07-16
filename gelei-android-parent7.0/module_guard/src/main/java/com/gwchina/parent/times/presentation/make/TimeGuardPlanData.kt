package com.gwchina.parent.times.presentation.make

import com.gwchina.parent.times.common.TimeGuardDailyPlanVO

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-28 14:41
 */
/**
 * [selectedGuardDays]已选中的星期 1..7
 * [state]展开还是隐藏
 * [hasSetTimeDuration]:是否设置过可用时长
 */
data class TimeGuardPlanData(var timeGuardDailyPlanVO: TimeGuardDailyPlanVO, var selectedGuardDays: List<Int> = emptyList(), var state: Int, var hasSetTimeDuration: Boolean = false) {

    val timePeriodList = timeGuardDailyPlanVO.timePeriodList
}

