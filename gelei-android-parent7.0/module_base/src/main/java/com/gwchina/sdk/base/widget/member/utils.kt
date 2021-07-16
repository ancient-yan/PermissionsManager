package com.gwchina.sdk.base.widget.member

import com.gwchina.sdk.base.data.models.Member
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.utils.TOTAL_SECONDS_OF_ONE_DAY
import com.gwchina.sdk.base.utils.timestampMillis

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-28 16:29
 */
data class MemberExpiredTips(
        /**需要提示即将过期*/
        val shouldShowWillExpire: Boolean,
        /**还有多少天过期*/
        val remainingDay: Int
)

fun getMemberTimingRemainingDay(memberExpiredTime: Long): MemberExpiredTips {
    val oneDayMillis = 24 * 3600 * 1000
    val maxTipsTime = 7 * oneDayMillis
    val now = timestampMillis()
    val timeOffset = memberExpiredTime - now

    return if (timeOffset > maxTipsTime || timeOffset <= 0) {//未到提醒时间或者超过了过期时间不需要提醒
        MemberExpiredTips(shouldShowWillExpire = false, remainingDay = 0)
    } else {
        MemberExpiredTips(shouldShowWillExpire = true, remainingDay = (timeOffset / oneDayMillis).toInt() + 1)
    }
}

internal fun shouldShowMemberExpiredForceChooseDialog(user: User): Boolean {
    val memberExpiredTime = user.member_info?.end_time ?: return false

    val oneDayMillis = 24 * 3600 * 1000
    val now = timestampMillis()
    val timeOffset = memberExpiredTime - now

    return (timeOffset <= 0 && timeOffset > -oneDayMillis) /*过期第一天，强制弹框*/
}

/**会员大于七天才提示*/
internal fun shouldShowMemberExpiringDialog(memberInfo: Member) = (memberInfo.end_time - memberInfo.begin_time > 7 * TOTAL_SECONDS_OF_ONE_DAY * 1000)