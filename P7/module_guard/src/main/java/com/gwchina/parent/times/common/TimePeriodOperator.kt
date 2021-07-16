package com.gwchina.parent.times.common

import com.gwchina.sdk.base.utils.TOTAL_TIME_UNIT_COUNT
import com.gwchina.sdk.base.utils.Time
import com.gwchina.sdk.base.utils.TimePeriod
import timber.log.Timber

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-11 17:27
 */
internal class TimePeriodOperator(private var timePeriodList: MutableList<TimePeriod> = mutableListOf()) {

    val result: MutableList<TimePeriod>
        get() = timePeriodList

    /**替换操作的时间段*/
    fun replace(timePeriodList: MutableList<TimePeriod>) {
        this.timePeriodList = timePeriodList
    }

    private fun addGuardPeriod(timePeriod: TimePeriod) {
        if (timePeriod.startHour() == 0 && timePeriod.startMinute() == 0 && timePeriod.endHour() == 0 && timePeriod.endMinute() == 0) {
            return
        }

        if (timePeriod.isEmpty()) {
            return
        }

        val start = timePeriod.start.timeQuantity()
        var end = timePeriod.end.timeQuantity()

        if (start != 0 && end == 0) {
            end = TOTAL_TIME_UNIT_COUNT
            addGuardPeriod(start, end)
        } else if (start > end) {
            addGuardPeriod(end, start)
        } else {
            addGuardPeriod(start, end)
        }

        for (item in timePeriodList) {
            Timber.d(item.toString())
        }
    }

    /**
     * 添加一个已选的区域。
     * <pre>
     *     比如整体时间为 0 点 - 6 点共 6 个小时，最小分割单位为 10 ,
     *
     *          如果选择 0 点到 1 点，即第 0 - 60 分钟。则：start = 0， end = 6
     *          如果选择 1 点到 2 点，即第 60 - 120 分钟。则：start = 6， end = 12
     * </pre>
     *
     * @param startUnitTimeCount 开始时间，为 {@link #mTimeUnit} 的整数倍。
     * @param endUnitTimeCount   结束时间，为 {@link #mTimeUnit} 的整数倍。
     */
    private fun addGuardPeriod(startUnitTimeCount: Int, endUnitTimeCount: Int) {
        val guardPeriod = timePeriodList

        var newSelectedPeriod = TimePeriod(Time.fromTimeQuantity(startUnitTimeCount), Time.fromTimeQuantity(endUnitTimeCount))

        //没有数据
        if (guardPeriod.isEmpty()) {
            guardPeriod.add(newSelectedPeriod)
            return
        }

        //已经有了, IntArray.eq(IntArray) 可以比较
        if (guardPeriod.contains(newSelectedPeriod)) {
            return
        }

        val newList = mutableListOf<TimePeriod>()

        //start end，采用逐步合并
        for (next in guardPeriod) {
            if (next.hasIntersection(newSelectedPeriod)) {
                newSelectedPeriod = next.merge(newSelectedPeriod)
            } else {
                newList.add(next)
            }
        }

        newList.add(newSelectedPeriod)
        guardPeriod.clear()
        guardPeriod.addAll(newList)

        guardPeriod.sortWith(comparator)
    }

    private val comparator = Comparator { ints: TimePeriod, ints1: TimePeriod ->
        if (ints.start.timeQuantity() > ints1.start.timeQuantity()) {
            return@Comparator 1
        } else if (ints.start.timeQuantity() < ints1.start.timeQuantity()) {
            return@Comparator -1
        }
        return@Comparator 0
    }

    fun changePeriod(origin: TimePeriod, changeTo: TimePeriod) {
        deletePeriod(origin)
        addGuardPeriod(changeTo)
    }

    fun deletePeriod(timePeriod: TimePeriod) {
        timePeriodList.remove(timePeriod)
        //删除最后一个带图标的数据
        if (timePeriodList.any {it.type == 1}) {
            timePeriodList.removeAt(timePeriodList.size - 1)
        }
        for (ints in timePeriodList) {
            Timber.d(timePeriod.toString())
        }
    }

}