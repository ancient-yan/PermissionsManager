package com.gwchina.sdk.base.utils

import com.android.base.utils.android.ResourceUtils
import com.app.base.R
import com.blankj.utilcode.constant.TimeConstants
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**一天的总秒数*/
const val TOTAL_SECONDS_OF_ONE_DAY = 24 * 60 * 60

/**根据出生年月日计算年龄*/
fun calculateAgeByBirthday(year: Int, month: Int, day: Int): Int {
    val calendar = Calendar.getInstance()
    val curYear = calendar.get(Calendar.YEAR)
    val curMonth = calendar.get(Calendar.MONDAY) + 1
    val curDay = calendar.get(Calendar.DAY_OF_MONTH)
    var age = curYear - year
    if (age == 0) {
        return 0
    }
    if (curMonth < month || curMonth == month && curDay < day) {
        age--
    }
    return age
}

/**组合日期，默认格式为 `yyyyMMdd` */
fun composeDate(year: Int, month: Int, day: Int, separator: String = ""): String {
    return arrayOf(year.toString(), month.to2BitText(), day.to2BitText()).joinToString(separator)
}

fun formatMilliseconds(milliseconds: Long, pattern: String = "yyyy-MM-dd"): String {
    val sdp = SimpleDateFormat(pattern, Locale.getDefault())
    val date = Date()
    date.time = milliseconds
    return sdp.format(date)
}

fun isSameDay(day1: Long, day2: Long): Boolean {
    val cal = Calendar.getInstance()
    cal.timeInMillis = day1
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MINUTE, 0)
    val wee1 = cal.timeInMillis

    cal.timeInMillis = day2
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MINUTE, 0)

    Timber.d("wee1 = $wee1, wee2 = ${cal.timeInMillis}")

    return wee1 == cal.timeInMillis
}

/**
 * 根据秒值生成具体描述文本，格式为`{x小时x分钟}` 或者 `{x秒}`， [adjustToFullMinutes] 为 true 时表示整体时间大于等于 1 分钟时，转换为分钟表示后少于 1 分钟的剩余秒数按照 1 分钟算，否则剩余秒数将被忽略。
 *
 * 时间显示分两类：
 *
 * - （1）时间从0秒开始累计，包括限时可用列表的已用XX（时长）、用机时长排行榜、各类app使用时长排行榜；
 * - （2）时间从多开始倒计，包括首页设备卡片的剩余可用时长、临时可用
 *
 * 第一类时间显示规则：
 *
 *  - 已用时间：m秒（1=<m<=59），显示：m秒
 *  - 已用时间：x小时n分m秒（1=<n<=59；1=<m<=59），显示：x小时n分
 *  - 已用时间：x小时n分m秒（n=0；1=<m<=59），显示：x小时
 *
 * 简单来讲就是，当已用时间小于一分钟时，显示具体秒数；已用时间大于一分钟时，不显示不足一分钟的秒数而仅显示已使用的足额的分钟数。
 *
 * 第二类时间显示规则：
 *
 *  - 剩余可用m秒（1=<m<=59），显示：m秒；
 *  - 剩余可用n分钟m秒（1=<n<=59；1=<m<=59），显示 n+1分钟；
 */
fun formatSecondsToTimeText(seconds: Int, adjustToFullMinutes: Boolean = false): String {
    if (seconds < 60) {
        return createTimeTextFromClockValues(0, 0, seconds)
    }

    val adjustedSeconds = if (adjustToFullMinutes) {
        adjustToFullMinutes(seconds)
    } else {
        seconds
    }

    val totalMinute = adjustedSeconds / 60
    val hour = totalMinute / 60
    val minute = totalMinute % 60
    return createTimeTextFromClockValues(hour, minute)
}

/**
 * 用于将秒转换为分钟时做修正，少于 1 分钟的秒值按照一分钟算，比如
 *
 * - 30s 会修正为 60s
 * - 61s 会修正为 120s
 * - 170s 会修正为 180s
 */
private fun adjustToFullMinutes(seconds: Int): Int {
    val mode = seconds % 60
    return if (mode != 0) {
        seconds + (60 - mode)
    } else {
        seconds
    }
}

/**
 * 用于将秒转换为分钟时做修正，少于30 秒钟的秒值按照0分钟算，否则按1分钟计算，比如
 * 大于50秒钟的秒值按照1分钟计算
 */
private fun adjustToFullMinutes2(seconds: Int): Int {
    val mode = seconds % 60
    return if (mode < 30) {
        seconds
    } else {
        seconds + (60 - mode)
    }
}

/**
 * 根据小时、分钟、秒生成具体描述文本，格式为`{x小时x分钟}` 或者 `{x秒}`：
 *
 * - 0-59 只展示秒。
 * - 大于一分钟后不再展示秒。
 * - 全都是 0 则展示 0 分钟。
 */
fun createTimeTextFromClockValues(hours: Int, minutes: Int, seconds: Int = 0): String {
    //0-59 只展示秒
    if (hours == 0 && minutes == 0 && seconds != 0) {
        return "$seconds${ResourceUtils.getString(R.string.second)}"
    }
    //大于一分钟后不再展示秒，全都是 0 则展示 0 分钟。
    if (hours == 0 || (hours == 0 && minutes == 0)) {
        return "$minutes${ResourceUtils.getString(R.string.minute)}"
    }
    if (minutes == 0) {
        return "$hours${ResourceUtils.getString(R.string.hour)}"
    }
    return "$hours${ResourceUtils.getString(R.string.hour)}$minutes${ResourceUtils.getString(R.string.minute)}"
}

/**转换为至少 2 位的文本，比如 `1` 应该转换为 `01`*/
fun Int.to2BitText(): String {
    if (this < 10 && this > -10) {
        return "0$this"
    }
    return toString()
}

/**获取今天凌晨的时间（生成的时间戳精确到天），用于[isToday], [isYesterday], [isDayBeforeYesterday] 等方法的传值*/
fun getWeeOfToday(): Long {
    return getWeeOfTodayCalendar().timeInMillis
}

/**获取今天凌晨的时间（生成的时间戳精确到天），用于[isToday], [isYesterday], [isDayBeforeYesterday] 等方法的传值*/
fun getWeeOfTodayCalendar(): Calendar {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal
}

/**当前时间戳*/
fun timestampMillis() = System.currentTimeMillis()

/**判断是不是今天， [wee] 用作对比，应使用 [getWeeOfToday] 来创建，这个参数用于同时进行多个时间判断时减少创建 [Calendar] 对象*/
fun isToday(milliseconds: Long, wee: Long = getWeeOfToday()): Boolean {
    return milliseconds >= wee && milliseconds < wee + TimeConstants.DAY
}

/**判断是不是后天， [wee] 用作对比，应使用 [getWeeOfToday] 来创建，这个参数用于当前时间需要对比多个时间时减少创建 [Calendar] 对象*/
fun isTheDayAfterTomorrow(milliseconds: Long, wee: Long = getWeeOfToday()) = isToday(milliseconds, wee + 2 * TimeConstants.DAY)

/**判断是不是明天， [wee] 用作对比，应使用 [getWeeOfToday] 来创建，这个参数用于当前时间需要对比多个时间时减少创建 [Calendar] 对象*/
fun isTomorrow(milliseconds: Long, wee: Long = getWeeOfToday()) = isToday(milliseconds, wee + TimeConstants.DAY)

/**判断是不是昨天， [wee] 用作对比，应使用 [getWeeOfToday] 来创建，这个参数用于当前时间需要对比多个时间时减少创建 [Calendar] 对象*/
fun isYesterday(milliseconds: Long, wee: Long = getWeeOfToday()) = isToday(milliseconds + TimeConstants.DAY, wee)

/**判断是不是前天， [wee] 用作对比，应使用 [getWeeOfToday] 来创建，这个参数用于当前时间需要对比多个时间时减少创建 [Calendar] 对象*/
fun isDayBeforeYesterday(milliseconds: Long, wee: Long = getWeeOfToday()) = isToday(milliseconds + TimeConstants.DAY * 2, wee)

/**判断是不是一分钟以内，[cur] 是用于对比的时间，默认是当前时间戳*/
fun isWithinOneMinute(milliseconds: Long, cur: Long = timestampMillis()) = (cur - milliseconds) / 1000 < 60

/**判断是不是一小时以内，[cur] 是用于对比的时间，默认是当前时间戳*/
fun isWithinOneHour(milliseconds: Long, cur: Long = timestampMillis()) = (cur - milliseconds) / 1000 / 60 < 60

/**判断是不是[hours]小时以内，[cur] 是用于对比的时间，默认是当前时间戳*/
fun isWithinHours(milliseconds: Long, cur: Long = timestampMillis(), hours: Int) = (cur - milliseconds) / 1000 / 60 / 60 < hours

/**返回时间上的分钟差，[cur] 是用于对比的时间，默认是当前时间戳*/
fun minutesDifference(milliseconds: Long, cur: Long? = null) = ((cur
        ?: timestampMillis()) - milliseconds) / 1000 / 60

/**返回时间上的小时差，[cur] 是用于对比的时间，默认是当前时间戳*/
fun hoursDifference(milliseconds: Long, cur: Long? = null) = ((cur
        ?: timestampMillis()) - milliseconds) / 1000 / 60 / 60

/**
 * 格式化毫秒值，具体规则描述：
 *
 *   - 刚刚：1分钟以内；
 *   - xx分钟前：1分钟-1个小时以内；
 *   - xx小时前：1小时-24小时以内；
 *   - 昨天09:35：24个小时-48个小时（可设置为显示出具体时间，也可不）；
 *   - 前天09:35：48个小时-72个小时（可设置为显示出具体时间，也可不）；
 *   - 09-23 09:35：当年的72小时后，当年的时间不显示年；
 *   - 2014-08-21 12:30：不在当年。
 */
fun formatMillisecondsToDetailDesc(milliseconds: Long): String {
    //对比的对象
    val thisCalendar = Calendar.getInstance()
    thisCalendar.timeInMillis = milliseconds

    //当前时间，精确到毫秒值
    val nowCalendar = Calendar.getInstance()
    val now = nowCalendar.timeInMillis

    //当前时间：用于对比今天、昨天、前天
    val wee = getWeeOfToday()

    return when {
        isWithinOneMinute(milliseconds, now) -> {
            ResourceUtils.getString(R.string.just)
        }
        isWithinOneHour(milliseconds, now) -> {
            ResourceUtils.getString(R.string.x_minutes_ago_mask, minutesDifference(milliseconds, now).toInt())
        }
        isToday(milliseconds, wee) -> {
            ResourceUtils.getString(R.string.x_hours_ago_mask, hoursDifference(milliseconds, now).toInt())
        }
        isYesterday(milliseconds, wee) -> {
            "${ResourceUtils.getString(R.string.yesterday)}${thisCalendar.get(Calendar.HOUR_OF_DAY).to2BitText()}:${thisCalendar.get(Calendar.MINUTE).to2BitText()}"
        }
        isDayBeforeYesterday(milliseconds, wee) -> {
            "${ResourceUtils.getString(R.string.the_day_before_yesterday)}${thisCalendar.get(Calendar.HOUR_OF_DAY).to2BitText()}:${thisCalendar.get(Calendar.MINUTE).to2BitText()}"
        }
        nowCalendar.get(Calendar.YEAR) == thisCalendar[Calendar.YEAR] -> {
            formatMilliseconds(milliseconds, "MM-dd HH:mm")
        }
        else -> {
            formatMilliseconds(milliseconds, "yyyy-MM-dd HH:mm")
        }
    }
}

/**
 * 格式化毫秒值，具体规则描述：
 *
 *   - 后天
 *   - 明天
 *   - 今天
 *   - 昨天
 *   - 前天
 *   - 09-23（当年的72小时后，当年的时间不显示年）
 *   - 2014-08-21（不在当年）
 */
fun formatMillisecondsToDayDesc(milliseconds: Long, monthPattern: String = "MM-dd", yearPattern: String = "yyyy-MM-dd"): String {
    //对比的对象
    val thisCalendar = Calendar.getInstance()
    thisCalendar.timeInMillis = milliseconds

    //当前时间，精确到毫秒值
    val nowCalendar = Calendar.getInstance()

    //当前时间：用于对比今天、昨天、前天
    val wee = getWeeOfToday()

    return when {
        isTomorrow(milliseconds, wee) -> {
            ResourceUtils.getString(R.string.tomorrow)
        }
        isTheDayAfterTomorrow(milliseconds, wee) -> {
            ResourceUtils.getString(R.string.the_day_after_tomorrow)
        }
        isToday(milliseconds, wee) -> {
            ResourceUtils.getString(R.string.today)
        }
        isYesterday(milliseconds, wee) -> {
            ResourceUtils.getString(R.string.yesterday)
        }
        isDayBeforeYesterday(milliseconds, wee) -> {
            ResourceUtils.getString(R.string.the_day_before_yesterday)
        }
        nowCalendar.get(Calendar.YEAR) == thisCalendar[Calendar.YEAR] -> {
            formatMilliseconds(milliseconds, monthPattern)
        }
        else -> {
            formatMilliseconds(milliseconds, yearPattern)
        }
    }
}

/**
 * 格式化毫秒值毫秒值为更新时间，具体规则描述：
 *
 * - 刚刚更新：1分钟以内；
 * - xx分钟前更新：1分钟-1个小时以内；
 * - xx小时前更新：1小时-24小时以内；
 * - 最后更新 昨天09:35：24个小时-48个小时（可设置为显示出具体时间，也可不）；
 * - 最后更新 前天09:35：48个小时-72个小时（可设置为显示出具体时间，也可不）；最后更新 09-23 09:35：当年的72小时后，当年的时间不显示年；
 * - 最后更新 2014-08-21 12:30：不在当年
 */
fun formatMillisecondsToUpdateTime(milliseconds: Long): String {
    //对比的对象
    val thisCalendar = Calendar.getInstance()
    thisCalendar.timeInMillis = milliseconds

    //当前时间，精确到毫秒值
    val nowCalendar = Calendar.getInstance()
    val now = nowCalendar.timeInMillis

    //当前时间：用于对比今天、昨天、前天
    val wee = getWeeOfToday()

    return when {
        isWithinOneMinute(milliseconds, now) -> {
            ResourceUtils.getString(R.string.update_mask, ResourceUtils.getString(R.string.just))
        }
        isWithinOneHour(milliseconds, now) -> {
            ResourceUtils.getString(R.string.update_mask, ResourceUtils.getString(R.string.x_minutes_ago_mask, minutesDifference(milliseconds, now).toInt()))
        }
        isToday(milliseconds, wee) -> {
            ResourceUtils.getString(R.string.update_mask, ResourceUtils.getString(R.string.x_hours_ago_mask, hoursDifference(milliseconds, now).toInt()))
        }
        isYesterday(milliseconds, wee) -> {
            ResourceUtils.getString(R.string.update_mask,
                    "${ResourceUtils.getString(R.string.yesterday)}${thisCalendar.get(Calendar.HOUR_OF_DAY).to2BitText()}:${thisCalendar.get(Calendar.MINUTE).to2BitText()}")
        }
        isDayBeforeYesterday(milliseconds, wee) -> {
            ResourceUtils.getString(R.string.update_mask,
                    "${ResourceUtils.getString(R.string.the_day_before_yesterday)}${thisCalendar.get(Calendar.HOUR_OF_DAY).to2BitText()}:${thisCalendar.get(Calendar.MINUTE).to2BitText()}")
        }
        nowCalendar.get(Calendar.YEAR) == thisCalendar[Calendar.YEAR] -> {
            ResourceUtils.getString(R.string.update_mask, formatMilliseconds(milliseconds, "MM-dd HH:mm"))
        }
        else -> {
            ResourceUtils.getString(R.string.update_mask, formatMilliseconds(milliseconds, "yyyy-MM-dd HH:mm"))
        }
    }
}

/**相对于今天，当前时间的秒值：`(hour * 60 * 60) + (minute * 60) + second`*/
fun secondsOfToday(): Int {
    val instance = Calendar.getInstance()
    val hour = instance.get(Calendar.HOUR_OF_DAY)
    val minute = instance.get(Calendar.MINUTE)
    val second = instance.get(Calendar.SECOND)
    return (hour * 60 * 60) + (minute * 60) + second
}
