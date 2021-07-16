package com.gwchina.parent.apps.common

import android.content.Context
import com.android.base.utils.android.ResourceUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.config.DEVICE_ID_KEY
import com.gwchina.sdk.base.data.api.APP_RULE_TYPE_DISABLE
import com.gwchina.sdk.base.data.api.APP_RULE_TYPE_FREE
import com.gwchina.sdk.base.data.api.APP_RULE_TYPE_LIMITED
import com.gwchina.sdk.base.data.api.isSevereMode
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.data.models.isAndroid
import com.gwchina.sdk.base.utils.formatSecondsToTimeText
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import javax.inject.Inject
import javax.inject.Named

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-22 16:28
 */
class AppGuardResource @Inject constructor(
        @Named(DEVICE_ID_KEY) private val childDeviceId: String,
        appDataSource: AppDataSource
) {

    val hasTimeGuard = appDataSource.user().findDevice(childDeviceId)?.guard_level.isSevereMode()

    val isAndroidDevice = appDataSource.user().findDevice(childDeviceId).isAndroid()

    val isAndroidAndHasTimeGuard = isAndroidDevice && hasTimeGuard

    val isUsableDurationEnable = isAndroidDevice

    /**app 守护类型转换为对应的文字描述*/
    fun getRuleTypeName(ruleType: Int): String {
        return when (ruleType) {
            APP_RULE_TYPE_LIMITED -> ResourceUtils.getString(com.app.base.R.string.limited_usable)
            APP_RULE_TYPE_FREE -> {
//                if (isAndroidDevice && hasTimeGuard) {
//                    ResourceUtils.getString(R.string.lock_screen_usable)
//                } else {
                    ResourceUtils.getString(R.string.free_usable)
//                }
            }
            APP_RULE_TYPE_DISABLE -> ResourceUtils.getString(com.app.base.R.string.disabled)
            else -> throw IllegalArgumentException("不支持的这种类型")
        }
    }

    fun showAppGroupDescDialog(context: Context) {
        showConfirmDialog(context) {
            titleId = R.string.what_is_limited_group
            messageId = if (isAndroidDevice) {
                R.string.what_is_limited_group_android_desc
            } else {
                R.string.what_is_limited_group_ios_desc
            }
            positiveId = R.string.i_got_it
            noNegative()
        }.setCanceledOnTouchOutside(false)
    }

    /**根据秒值生成具体描述文本，格式为 `今日可用{x小时x分钟/天}，已用{x小时x分钟/天}`*/
    fun generateAppUsedTimeTextFromSecond(usableSecond: Int, usedSecond: Int): String {
        return if (isAndroidDevice) {
            ResourceUtils.getString(R.string.app_usable_time_android_mask, generateDetailTimePerDayTextFromSecond(usableSecond), formatSecondsToTimeText(usedSecond))
        } else {
            ResourceUtils.getString(R.string.app_usable_time_ios_mask, generateDetailTimePerDayTextFromSecond(usableSecond))
        }
    }

}