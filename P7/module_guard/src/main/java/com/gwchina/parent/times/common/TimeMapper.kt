package com.gwchina.parent.times.common

import android.graphics.Color
import com.android.base.app.dagger.ActivityScope
import com.android.base.utils.android.ResourceUtils
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.data.TimeGuardDailyPlan
import com.gwchina.parent.times.data.TimeGuardPlan
import com.gwchina.parent.times.presentation.spare.SparePlanBottom
import com.gwchina.parent.times.presentation.spare.SparePlanContent
import com.gwchina.parent.times.presentation.spare.SparePlanHeader
import com.gwchina.sdk.base.data.api.isFlagPositive
import com.gwchina.sdk.base.data.models.TimePart
import com.gwchina.sdk.base.utils.TimePeriod
import com.gwchina.sdk.base.utils.bs24TextToGuardTime
import com.gwchina.sdk.base.utils.formatSecondsToTimeText
import com.gwchina.sdk.base.utils.formatTo24BSText
import javax.inject.Inject

@ActivityScope
class TimeMapper @Inject constructor() {

    fun toSparePlanVOList(isChoosingMode: Boolean, isAndroid: Boolean, list: List<TimeGuardPlan>?, isExpand: (planId: String) -> Boolean): List<Any> {

        if (list.isNullOrEmpty()) {
            return emptyList()
        }

        val result = mutableListOf<Any>()

        var tempContents: List<SparePlanContent>
        var tempIsExpand: Boolean

        list.forEach {

            tempContents = groupSparePlanItem(it, isChoosingMode, isAndroid)
            tempIsExpand = isExpand(it.batch_id)

            result.add(SparePlanHeader(
                    it.batch_id,
                    it.batch_name?:"",
                    generateSparePlanWeeklyDesc(it),
                    it.weekly_time,
                    isFlagPositive(it.enabled),
                    tempIsExpand,
                    tempContents
            ))

            if (tempIsExpand) {
                result.addAll(tempContents)
            }

            if (!isChoosingMode) {
                result.add(SparePlanBottom(it.batch_id, isFlagPositive(it.enabled)))
            }

        }

        result.add("TIPS")

        return result
    }

    private fun generateSparePlanWeeklyDesc(sparePlan: TimeGuardPlan): CharSequence {
        return ResourceUtils.getString(R.string.usable_duration_weekly_mask, formatSecondsToTimeText(sparePlan.weekly_time))
    }

    private fun generateSparePlanDayDesc(sparePlanItems: List<TimeGuardDailyPlan>, enableTime: Int, timePeriodList: List<TimePeriod>, isAndroid: Boolean): CharSequence {
        val days = sparePlanItems.map { it.what_day }.toMutableList()
        days.sort()

        return SpanUtils()
                .append(generateDaysOfWeekText(days))
                .append("    |    ")
                .setForegroundColor(Color.parseColor("#CCCCCC"))
                .setVerticalAlign(SpanUtils.ALIGN_CENTER)
                .setFontSize(10, true)
                .append(if (isAndroid) {
                    formatSecondsToTimeText(enableTime)
                } else {
                    formatSecondsToTimeText(calculateUsablePeriodTotalSeconds(timePeriodList))
                }).append("/天")
                .create()
    }

    private fun groupSparePlanItem(sparePlan: TimeGuardPlan, needBottom: Boolean, isAndroid: Boolean): List<SparePlanContent> {
        val grouped = sparePlan.getDailyPlans()?.groupBy {
            it.enabled_time.toString() + it.getTimeParts().toString()
        }?.values ?: emptyList()

        val lastIndex = grouped.size - 1

        return grouped.mapIndexed { index, list ->
            val timePeriod = toTimePeriods(list[0].getTimeParts())
            val enableTime = list[0].enabled_time
            SparePlanContent(
                    sparePlan.batch_id,
                    generateSparePlanDayDesc(list, enableTime, timePeriod, isAndroid),
                    timePeriod,
                    enableTime,
                    list.map { it.what_day },
                    list.map { it.rule_id },
                    needBottom && index == lastIndex
            )
        }
    }

    fun toTimeGuardDailyPlanList(list: List<TimeGuardDailyPlanVO>): List<TimeGuardDailyPlan> {
        return list.map { toTimeGuardDailyPlan(it) }
    }

    fun toTimeGuardWeeklyPlanVO(plan: TimeGuardPlan): TimeGuardWeeklyPlanVO {
        return TimeGuardWeeklyPlanVO(
                planId = plan.batch_id,
                planName = plan.batch_name?:"",
                isSparePlan = plan.batch_id.isNotEmpty(),
                isMultiDeviceUsing = plan.batch_tag > 1,
                dailyPlans = plan.getDailyPlans()?.map {
                    toTimeGuardDailyPlanVO(it)
                }?.toMutableList() ?: mutableListOf()
        )
    }

    fun toTimeGuardWeeklyPlanVO(sparePlanHeader: SparePlanHeader): TimeGuardWeeklyPlanVO {
        return TimeGuardWeeklyPlanVO(
                planId = sparePlanHeader.batchId,
                planName = sparePlanHeader.batchName,
                isSparePlan = sparePlanHeader.batchId.isNotEmpty(),
                isMultiDeviceUsing = false,
                dailyPlans = convertSparePlanContentListToTimeGuardDailyPlanVOList(sparePlanHeader.content)
        )
    }

    private fun convertSparePlanContentListToTimeGuardDailyPlanVOList(content: List<SparePlanContent>): MutableList<TimeGuardDailyPlanVO> {
        val plans = mutableListOf<TimeGuardDailyPlanVO>()
        content.forEach {
            it.days.forEachIndexed { index, day ->
                plans.add(TimeGuardDailyPlanVO(
                        id = it.dayIds[index],
                        dayOfWeek = day,
                        enabledTime = it.enableTime,
                        /*tips: no deep copy*/
                        timePeriodList = it.timePeriod.toMutableList()
                ))
            }
        }
        return plans
    }

    private fun toTimeGuardDailyPlanVO(timeGuardPlan: TimeGuardDailyPlan): TimeGuardDailyPlanVO {
        return TimeGuardDailyPlanVO(
                timeGuardPlan.rule_id,
                timeGuardPlan.what_day,
                timeGuardPlan.enabled_time,
                toTimePeriods(timeGuardPlan.getTimeParts())
        )
    }

    fun toTimePeriods(timeParts: List<TimePart>?): MutableList<TimePeriod> {
        if (timeParts == null || (timeParts.size == 1 && timeParts[0].is00to00())) {
            return mutableListOf(TimePeriod.fromTimes(0, 0, 24, 0))
        }
        return timeParts.map { timePartToPeriod(it) }.toMutableList()
    }

    private fun TimePart.is00to00(): Boolean {
        return begin_time.startsWith("00:00") && end_time.startsWith("00:00")
    }

    private fun timePartToPeriod(it: TimePart): TimePeriod {
        return TimePeriod(bs24TextToGuardTime(it.begin_time), bs24TextToGuardTime(it.end_time))
    }

    private fun toTimeGuardDailyPlan(timeGuardPlanVO: TimeGuardDailyPlanVO): TimeGuardDailyPlan {
        return TimeGuardDailyPlan(
                timeGuardPlanVO.id,
                timeGuardPlanVO.dayOfWeek,
                timeGuardPlanVO.enabledTime,
                toTimeParts(timeGuardPlanVO.timePeriodList)
        )
    }

    fun toTimeParts(timePeriodList: List<TimePeriod>): List<TimePart> {
        if (timePeriodList.isEmpty()) {
            return emptyList()
        }
        return timePeriodList.map {
            TimePart(it.start.formatTo24BSText(), it.end.formatTo24BSText())
        }
    }

}