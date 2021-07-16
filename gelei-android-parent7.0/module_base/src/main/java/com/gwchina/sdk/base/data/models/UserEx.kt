@file:JvmName("UserEx")

package com.gwchina.sdk.base.data.models

import com.blankj.utilcode.constant.TimeConstants
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.utils.*

const val MAX_CHILD_COUNT = 3
const val MAX_DEVICE_COUNT_PER_CHILD = 3

fun User?.reachMaximumChildLimit(): Boolean {
    return this != null && childList?.size ?: 0 >= MAX_CHILD_COUNT
}

fun User?.reachMaximumChildAndDeviceLimit(): Boolean {
    if (this == null) {
        return false
    }
    if (reachMaximumChildLimit()) {
        return childList?.all {
            it.reachMaxDeviceCount()
        } ?: false
    }
    return false
}

/**获取当前孩子的设备id*/
val User?.currentChildDeviceId: String?
    get() = this?.currentDevice?.device_id

/**获取当前孩子的用户id*/
val User?.currentChildId: String?
    get() = this?.currentChild?.child_user_id


fun User?.logined(): Boolean {
    return this != null && this !== User.NOT_LOGIN
}

/**是不是高级会员*/
fun User?.isSVipMember(): Boolean {
    return isMemberByMemberType(MEMBER_SVIP)
}

/**是不是C端会员*/
fun User?.isCVipMember(): Boolean {
    return isMemberByMemberType(MEMBER_C_VIP)
}

/**是不是电信会员*/
fun User?.isCTVipMember(): Boolean {
    return isMemberByMemberType(MEMBER_CT_VIP)
}

/**根据会员类型判断是不是该会员*/
private fun User?.isMemberByMemberType(memberType: String): Boolean {
    if (!isMember) {
        return false
    }
    if (this!!.member_info!!.member_type == memberType) {
        return true
    }
    return false
}

/**是不是会员*/
val User?.isMember: Boolean
    get() {
        if (this == null) {
            return false
        }
        if (member_info == null || member_info.status != MEMBER_STATUS_VALID) {
            return false
        }
        return true
    }

/**解决会员权限请求失败的情况*/
val User?.getVipRule: VipRule?
    get() {
        if (this == null) {
            return null
        }
        if (vipRule == null) {
            //TODO 弹出提示框，重新请求，请求成功刷新user
        }
        return vipRule
    }

fun User?.deviceCount(): Int {
    if (this == null) {
        return 0
    }
    return childList?.fold(0) { aac, child -> aac + (child.device_list?.size ?: 0) } ?: 0
}

fun User?.iosDeviceCount(): Int {
    if (this == null) {
        return 0
    }
    return childList?.fold(0) { aac, child ->
        aac + (child.device_list?.filter { device -> device.isIOS() }?.size ?: 0)
    } ?: 0
}

fun User?.androidDeviceCount(): Int {
    if (this == null) {
        return 0
    }
    return childList?.fold(0) { aac, child ->
        aac + (child.device_list?.filter { device -> device.isAndroid() }?.size ?: 0)
    } ?: 0
}

/**获取正常守护状态的设备数量，排除了守护过期的设备*/
fun User?.guardDeviceCount(): Int {
    if (this == null) {
        return 0
    }
    return childList?.fold(0) { aac, child ->
        val fold = child.device_list?.fold(0) { count, device ->
            if (isMemberGuardExpired(device.status)) count else count + 1
        } ?: 0
        aac + fold
    } ?: 0
}

fun User?.childCount(): Int {
    if (this == null) {
        return 0
    }
    return childList?.size ?: 0
}

/**根据 [childId] 查找 [Child]*/
fun User.findChild(childId: String): Child? {
    return childList?.find {
        it.child_user_id == childId
    }
}

/**根据 [deviceId] 查找 [Device]*/
fun User.findDevice(deviceId: String): Device? {
    val list = childList
    if (list.isNullOrEmpty()) {
        return null
    }
    list.forEach {
        it.device_list?.forEachIndexed { _, device ->
            if (device.device_id == deviceId) {
                return device
            }
        }
    }
    return null
}

/**根据 [deviceId] 查找所属 [Child]*/
fun User.findChildByDeviceId(deviceId: String): Child? {
    val list = childList
    if (list.isNullOrEmpty()) {
        return null
    }
    list.forEach {
        it.device_list?.forEach { device ->
            if (device.device_id == deviceId) {
                return it
            }
        }
    }
    return null
}

/**根据 [deviceId] 查找 [Device]*/
fun Child.findDevice(deviceId: String): Device? {
    val list = device_list
    if (list.isNullOrEmpty()) {
        return null
    }
    list.forEachIndexed { _, device ->
        if (device.device_id == deviceId) {
            return device
        }
    }
    return null
}


/**
 * 从用户中提取出所有孩子以及其设备, 返回值 List 中的元素为 [Child] 或 [Device]，排序方式为：
 *
 * - Child
 *      - device1
 *      - device2
 * - Child
 *      - device1
 * - Child
 *      - device1
 */
fun User.extractChildAndDevice(filterIfNoDevice: Boolean = false): List<Any> {
    val list = mutableListOf<Any>()
    childList?.forEach {
        val size = it.device_list?.size ?: 0
        if (!filterIfNoDevice || size > 0) {
            list.add(it)
        }
        it.device_list?.forEachIndexed { _, device ->
            list.add(device)
        }
    }
    return list
}

/**
 * 获取当前时间到设备的下一个锁屏时间
 */
fun getNextLookScreenTime(rule_time_list: List<TimePeriodRule>?): NextUsablePeriodTime {
    val timestampMillis = timestampMillis()

    if (rule_time_list.isNullOrEmpty()) {
        return NextUsablePeriodTime(true, ((getWeeOfToday() + TimeConstants.DAY - timestampMillis) / 1000).toInt())
    }

    val today = getDayOfWeek()
    val currentTime = formatMilliseconds(timestampMillis, "HH:mm:ss")
    val todayPeriodRule = rule_time_list.find { it.what_day == today }
    val todayNextTimePart = todayPeriodRule?.rule_time_fragment?.find {
        it.end_time >= currentTime && it.end_time != "24:00:00"
    }

    return if (todayNextTimePart == null) {
        NextUsablePeriodTime(true, ((getWeeOfToday() + TimeConstants.DAY - timestampMillis) / 1000).toInt())
    } else {
        NextUsablePeriodTime(
                false,
                bs24TextToGuardTime(todayNextTimePart.end_time).toSeconds() - bs24TextToGuardTime(currentTime).toSeconds()
        )
    }
}

/**
 * 获取当前时间到设备下一个可用时段的起始时间
 */
fun getNextUsablePeriodTime(rule_time_list: List<TimePeriodRule>?): NextUsablePeriodTime {
    val timestampMillis = timestampMillis()
    if (rule_time_list.isNullOrEmpty()) {
        return NextUsablePeriodTime(true, ((getWeeOfToday() + TimeConstants.DAY - timestampMillis) / 1000).toInt())
    }

    val today = getDayOfWeek()
    val currentTime = formatMilliseconds(timestampMillis, "HH:mm:ss")
    val todayPeriodRule = rule_time_list.find { it.what_day == today }
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

class NextUsablePeriodTime(
        val isNextDay: Boolean,
        val timeSeconds: Int
)

fun Device?.isAndroid() = com.gwchina.sdk.base.data.api.isAndroid(this?.device_type)
fun Device?.isIOS() = com.gwchina.sdk.base.data.api.isIOS(this?.device_type)

fun Device?.planedTimePlan() = !isYes(this?.first_setting_time_flag)
fun Device?.planedAppPlan() = !isYes(this?.first_setting_soft_flag)
fun Device?.timePlanHasBeSet() = isYes(this?.setting_time_flag)
fun Device?.appPlanHasBeSet() = isYes(this?.setting_soft_flag)
fun Device?.netPlanHasBeSet() = isYes(this?.setting_url_pattern_flag)
fun Device?.phonePlanHasBeSet() = isYes(this?.setting_phone_flag)