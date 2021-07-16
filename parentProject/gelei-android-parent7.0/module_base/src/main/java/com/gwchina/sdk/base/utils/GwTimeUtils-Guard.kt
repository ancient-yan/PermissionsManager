package com.gwchina.sdk.base.utils

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.math.max
import kotlin.math.min

/*
 * 本文件中包含所有时间守护的所有核心计算方法。
 *
 *      名词解释：timeQuantity，时间量，表示以 [DEFAULT_GUARD_TIME_UNIT] 为单位的时间量，DEFAULT_GUARD_TIME_UNIT 固定为 10 分钟。10 分钟 = 1 timeQuantity
 */

/**最小守护时间单位，10分钟*/
const val DEFAULT_GUARD_TIME_UNIT = 10
/**一天能划分为多少个时间单位*/
const val TOTAL_TIME_UNIT_COUNT = 24 * 60 / DEFAULT_GUARD_TIME_UNIT

/**
 *  - 表示一天的某个时刻：比如 03:30 = 3点30分
 *  - 表示时长：比如 03:30 = 3个小时30分钟
 */
@Parcelize
data class Time(val hour: Int = 0, val minute: Int = 0, val second: Int = 0) : Parcelable {

    companion object {
        /**以 [DEFAULT_GUARD_TIME_UNIT]  为单位的时间量转换为时间*/
        fun fromTimeQuantity(timeQuantity: Int): Time {
            //scale = 10
            // 10 * 10 = 100 分钟
            val totalTime = timeQuantity * DEFAULT_GUARD_TIME_UNIT
            //100 / 60 = 1
            val hour = totalTime / 60
            //100 % 60 = 40
            val minute = totalTime % 60
            //1 时 40 分
            return Time(hour, minute, 0)
        }

        /** 秒转换为 [Time]*/
        fun fromSeconds(seconds: Int): Time {
            val hour = seconds / 3600
            val minute = (seconds % 3600) / 60
            val second = seconds % 60
            return Time(hour, minute, second)
        }

        /** 分钟转换为 [Time]*/
        fun fromMinutes(minutes: Int): Time {
            val hour = minutes / 60
            val minute = minutes % 60
            return Time(hour, minute, 0)
        }
    }

    fun isZero() = hour == 0 && minute == 0 && second == 0
    fun isNotZero() = !isZero()

    fun toSeconds() = hour * 60 * 60 + minute * 60 + second
    fun toMillisecond() = toSeconds() * 1000

    /**忽略[second]*/
    fun toMinutes() = hour * 60 + minute

    /**时间转换为以 [DEFAULT_GUARD_TIME_UNIT] 为单位的时间量*/
    fun timeQuantity(): Int {
        //1 时 40 分
        //100 分钟
        val totalTime = hour * 60 + minute
        // 100 / 10 = 10
        return totalTime / DEFAULT_GUARD_TIME_UNIT
    }

}

/**由两个 [Time]  组成的时段，只支持精确到分钟*/
@Parcelize
data class TimePeriod(val start: Time, val end: Time, var type: Int = 0) : Parcelable {

    companion object {

        fun fromTimeQuantities(startTimeQuantity: Int, endTimeQuantity: Int): TimePeriod {
            return TimePeriod(Time.fromTimeQuantity(startTimeQuantity), Time.fromTimeQuantity(endTimeQuantity))
        }

        fun fromTimes(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int): TimePeriod {
            return TimePeriod(Time(startHour, startMinute), Time(endHour, endMinute))
        }

        fun fromMinutes(startMinute: Int, endMinute: Int): TimePeriod {
            return TimePeriod(Time.fromMinutes(startMinute), Time.fromMinutes(endMinute))
        }

    }

    fun startHour() = start.hour
    fun startMinute() = start.minute
    fun endHour() = end.hour
    fun endMinute() = end.minute

    fun isEmpty() = startHour() == 0 && endHour() == 0 && startMinute() == 0 && endMinute() == 0
    fun is24Hours() = startHour() == 0 && endHour() == 24 && startMinute() == 0 && endMinute() == 0

    fun merge(otherPeriod: TimePeriod): TimePeriod {
        return TimePeriod(
                Time.fromTimeQuantity(min(start.timeQuantity(), otherPeriod.start.timeQuantity())),
                Time.fromTimeQuantity(max(end.timeQuantity(), otherPeriod.end.timeQuantity())))
    }

    fun hasIntersection(otherPeriod: TimePeriod): Boolean {
        return start.timeQuantity() <= otherPeriod.end.timeQuantity() && end.timeQuantity() >= otherPeriod.start.timeQuantity()
    }

}

/**返回星期，星期一到星期天对应 `1-7` */
fun getDayOfWeek(): Int {
    var dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    dayOfWeek--
    if (dayOfWeek == 0) {
        dayOfWeek = 7
    }
    return dayOfWeek
}

/**生成具体描述文本，格式为`{x小时x分钟}`*/
fun Time.formatToText(): String = createTimeTextFromClockValues(hour, minute, second)

/**转换为24小时格式的文本：比如 `02:30`*/
fun Time.formatTo24BSText(): String {
    return "${hour.to2BitText()}:${minute.to2BitText()}"
}

/**24 小时格式的文本(比如 `02:30:20`)时间转换为[Time]*/
fun bs24TextToGuardTime(formatTimeText: String): Time {
    try {
        val array = formatTimeText.split(":")
        if (array.size == 2) {
            return Time(array[0].toInt(), array[1].toInt())
        } else if (array.size == 3) {
            return Time(array[0].toInt(), array[1].toInt(), array[2].toInt())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return Time(0, 0)
}

/**时间转换为24小时格式的文本：比如 `02:30`*/
fun formatTo24BSText(hour: Int, minute: Int): String {
    return "${hour.to2BitText()}:${minute.to2BitText()}"
}

fun adjustToTimeQuantityMultiple(seconds: Int): Int {
    return seconds / (DEFAULT_GUARD_TIME_UNIT * 60) * (DEFAULT_GUARD_TIME_UNIT * 60)
}