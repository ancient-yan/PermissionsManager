package com.gwchina.parent.main.presentation.home.card

import com.android.base.utils.android.ResourceUtils
import com.blankj.utilcode.constant.TimeConstants
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.DEVICE_STATUS_ONLINE
import com.gwchina.sdk.base.data.api.isNo
import com.gwchina.sdk.base.data.api.isYes
import com.gwchina.sdk.base.data.models.*
import com.gwchina.sdk.base.utils.*
import java.util.*
import kotlin.math.abs
import kotlin.math.max

/**可用：设备处于可用时间段内且有可用时长，前置条件，设备已经设置了时间守护*/
fun Device.isInUsablePeriod(): Boolean {
    //今天是否设置了时间守护，没有则全天可用
    val ruleTimeList = rule_time_list
    if (ruleTimeList.isNullOrEmpty()) {
        return true
    }

    //今天设置了时间守护，是否有可用时长
    if (used_time >= enabled_time) {
        return false
    }

    /*是否在可用时段内*/
    if (isYes(rule_time_flag)) {
        return true
    }

    return false
}

/**临时可用：临时可用状态已生效，孩子设备处于临时可用时间。*/
fun Device.isTemporarilyUsable(): Boolean {
    val tempUsableTime = temp_usable_time ?: return false
    val currentTimestamp = System.currentTimeMillis()
    //允许误差10秒钟
    return (currentTimestamp >= tempUsableTime.begin_time || abs(currentTimestamp - tempUsableTime.begin_time) < 10000) && currentTimestamp < tempUsableTime.end_time
}

/**禁用：设备处于非可用时间段或可用时长用尽。*/
fun Device.isDisable(): Boolean {
    //可用时长用尽
    if (used_time >= enabled_time) {
        return true
    }
    //不在时段内
    if (isNo(rule_time_flag)) {
        return true
    }
    return false
}

/**设备离线：设备处于离线状态（孩子端离线，如关机、无网络）。*/
fun Device.isOffline(): Boolean {
    return on_line_flag != DEVICE_STATUS_ONLINE
}

//孩子端开始权限上报版本(7.1.2)
const val CHILD_DEVICE_VERSION = "7.1.2"

/**
 * 设备权限丢失
 */
fun isPermissionLose(privilegeData: PrivilegeData?): Boolean {
    if (privilegeData == null) return false
    if (privilegeData.childVersion.isNullOrEmpty() || privilegeData.childVersion!! < CHILD_DEVICE_VERSION || isYes(AppContext.appDataSource().user().currentDevice?.custom_device_flag)) {
        return false
    }
    val permissions = privilegeData.privilegeList
    return !permissions.isNullOrEmpty() && permissions.any { it.state == 0 && it.is_must == 1 }
}

/**权限是否未设置*/
fun isDeviceNoPermission(privilegeData: PrivilegeData?): Boolean {
    if (privilegeData == null) return false
    if (privilegeData.childVersion.isNullOrEmpty() || privilegeData.childVersion!! < CHILD_DEVICE_VERSION || isYes(AppContext.appDataSource().user().currentDevice?.custom_device_flag)) {
        return false
    }
    val permissions = privilegeData.privilegeList
    return permissions.isNullOrEmpty()
}

private const val MONTH_PATTERN = "MM月dd日"
private const val YEAR_PATTERN = "yyyy年MM月dd日"

fun getNextUnlockTime(device: Device): String {
    val ruleTimeList = device.rule_time_list
    if (ruleTimeList.isNullOrEmpty() || (ruleTimeList.size == 7 && ruleTimeList.all { it.enabled_time == 0 })) {
        return if (device.isAndroid()) {
            "已锁屏"
        } else {
            "不可用"
        }
    }

    val nextUnlockTime = getNextUnlockTime(ruleTimeList, device)
    return if (device.isAndroid()) {
        ResourceUtils.getString(R.string.auto_unlock_desc_android_mark, nextUnlockTime)
    } else {
        ResourceUtils.getString(R.string.auto_unlock_desc_ios_mark, nextUnlockTime)
    }
}

private fun getNextUnlockTime(ruleTimeList: List<TimePeriodRule>, device: Device): String {
    //今天
    val timestampMillis = timestampMillis()
    val currentTime = formatMilliseconds(timestampMillis, "HH:mm:ss")
    val today = getDayOfWeek()
    val instance = Calendar.getInstance()
    val todayPeriodRule = ruleTimeList.find { it.what_day == today }

    if (device.used_time >= device.enabled_time) {
        instance.add(Calendar.DAY_OF_YEAR, 1)
    } else {
        val todayNextTimePart = todayPeriodRule?.rule_time_fragment?.find { it.begin_time >= currentTime }
        if (todayNextTimePart == null) {
            instance.add(Calendar.DAY_OF_YEAR, 1)
        } else {
            return todayNextTimePart.begin_time.substring(0, todayNextTimePart.begin_time.length - 3)
        }
    }

    //其他天数
    for (index in 1..7) {

        val day = getRealDay(today, index)

        val timePeriodRule = ruleTimeList.find { it.what_day == day }
        val ruleTimeFragment = timePeriodRule?.rule_time_fragment
        if (timePeriodRule != null && timePeriodRule.enabled_time == 0) {//当天有规则且可用时长为0。
            instance.add(Calendar.DAY_OF_YEAR, 1)
            continue
        }
        if (timePeriodRule == null || ruleTimeFragment.isNullOrEmpty()) {
            return "${formatMillisecondsToDayDesc(instance.timeInMillis, MONTH_PATTERN, YEAR_PATTERN)} 00:00"
        }
        val timePart = ruleTimeFragment.first()
        return "${formatMillisecondsToDayDesc(instance.timeInMillis, MONTH_PATTERN, YEAR_PATTERN)} ${timePart.begin_time.substring(0, timePart.begin_time.length - 3)}"
    }

    return "-"
}

fun getNextLockTimeDesc(device: Device): String {
    val ruleTimeList = device.rule_time_list
    if (ruleTimeList.isNullOrEmpty() ||
            ruleTimeList.all {
                val ruleTimeFragment = it.rule_time_fragment
                ruleTimeFragment.isNullOrEmpty() || ruleTimeFragment.first().isFullDay()
            }
    ) {
        return if (device.isAndroid()) {
            "已解锁"
        } else {
            "可用"
        }
    }

    val nextLockTime = getNextLockTime(ruleTimeList)

    return if (device.isAndroid()) {
        ResourceUtils.getString(R.string.auto_lock_desc_android_mark, nextLockTime)
    } else {
        ResourceUtils.getString(R.string.auto_lock_desc_ios_mark, nextLockTime)
    }
}

private fun getNextLockTime(ruleTimeList: List<TimePeriodRule>): String {
    //今天
    val today = getDayOfWeek()
    val instance = Calendar.getInstance()
    val todayPeriodRule = ruleTimeList.find { it.what_day == today }
    if (todayPeriodRule == null || todayPeriodRule.rule_time_fragment.isNullOrEmpty()) {
        instance.add(Calendar.DAY_OF_YEAR, 1)
    } else {

        val timestampMillis = timestampMillis()
        val currentTime = formatMilliseconds(timestampMillis, "HH:mm:ss")
        val todayInPart = todayPeriodRule.rule_time_fragment?.find { it.begin_time <= currentTime && it.end_time >= currentTime }
                ?: return ""/*找不到对应的时段那就是cache了*/

        if (todayInPart.end_time == "24:00:00") {
            instance.add(Calendar.DAY_OF_YEAR, 1)
        } else {
            val endPeriodTime = bs24TextToGuardTime(todayInPart.end_time)
            return "${endPeriodTime.hour.to2BitText()}:${endPeriodTime.minute.to2BitText()}"
        }

    }

    //其他天数
    for (index in 1..7) {
        val day = getRealDay(today, index)
        val timePeriodRule = ruleTimeList.find { it.what_day == day }
        val ruleTimeFragment = timePeriodRule?.rule_time_fragment

        if (timePeriodRule != null && timePeriodRule.enabled_time <= 0) {
            return "${formatMillisecondsToDayDesc(instance.timeInMillis, MONTH_PATTERN, YEAR_PATTERN)} 00:00"
        }

        if (ruleTimeFragment.isNullOrEmpty()) {//当天没有规则
            instance.add(Calendar.DAY_OF_YEAR, 1)
            continue
        }

        val timePart = ruleTimeFragment.first()
        when {
            timePart.isFullDay() -> instance.add(Calendar.DAY_OF_YEAR, 1)
            timePart.begin_time == "00:00:00" -> {
                return "${formatMillisecondsToDayDesc(instance.timeInMillis, MONTH_PATTERN, YEAR_PATTERN)} ${timePart.end_time.substring(0, timePart.end_time.length - 3)}"
            }
            else -> {
                return "${formatMillisecondsToDayDesc(instance.timeInMillis, MONTH_PATTERN, YEAR_PATTERN)} 00:00"
            }
        }
    }
    return "-"
}

private fun getRealDay(today: Int, index: Int) = ((today + index) % 7).let {
    if (it == 0) {
        7
    } else {
        it
    }
}


private fun TimePart.isFullDay(): Boolean {
    return begin_time == "00:00:00" && end_time == "24:00:00"
}

/**返回剩余临时可用时间*/
fun Device.getTemporarilyRemainingSeconds(): Int {
    val timePeriodRule = temp_usable_time ?: return 0
    return max(0, ((timePeriodRule.end_time - timestampMillis()) / 1000).toInt())
}

class NextUsablePeriodTime(
        val isNextDay: Boolean,
        val timeSeconds: Int
)

fun getNextUsablePeriodTime(device: Device): NextUsablePeriodTime {
    val ruleTimeList = device.rule_time_list
    val timestampMillis = timestampMillis()

    if (ruleTimeList.isNullOrEmpty()) {
        return NextUsablePeriodTime(true, ((getWeeOfToday() + TimeConstants.DAY - timestampMillis) / 1000).toInt())
    }

    val today = getDayOfWeek()
    val currentTime = formatMilliseconds(timestampMillis, "HH:mm:ss")
    val todayPeriodRule = ruleTimeList.find { it.what_day == today }
    val todayNextTimePart = todayPeriodRule?.rule_time_fragment?.find {
        it.begin_time >= currentTime
    }

    return if (todayNextTimePart == null) {
        NextUsablePeriodTime(true, ((getWeeOfToday() + TOTAL_SECONDS_OF_ONE_DAY * 1000 - timestampMillis) / 1000).toInt())
    } else {
        NextUsablePeriodTime(
                false,
                bs24TextToGuardTime(todayNextTimePart.begin_time).toSeconds() - bs24TextToGuardTime(currentTime).toSeconds()
        )
    }

}