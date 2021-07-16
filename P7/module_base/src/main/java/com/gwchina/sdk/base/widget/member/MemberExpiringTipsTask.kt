package com.gwchina.sdk.base.widget.member

import android.app.Dialog
import android.content.Context
import com.android.base.app.BaseKit
import com.app.base.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.utils.SequentialUITaskExecutor
import com.gwchina.sdk.base.utils.UITask
import com.gwchina.sdk.base.utils.formatMilliseconds

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-28 11:01
 */
class MemberExpiringTipsTask : UITask(
        MEMBER_EXPIRING_TIPS_TASK_TASK_ID,
        SequentialUITaskExecutor.SCHEDULE_POLICY_REPEATED
) {

    companion object {

        private const val MEMBER_EXPIRING_TIPS_TASK_TASK_ID = "member_expiring_tips_task_task_id"
        private const val MEMBER_TIPS_SHOWING_TIME_FLAG = "member_expiring_time_tips_showed_flag_"

        private fun storage() = AppContext.storageManager().stableStorage()

//        private val spCache = SpCache(BaseUtils.getAppContext().packageName, false)

        fun isMemberExpiringTimeConfirmed(): Boolean {
            val endTime = AppContext.appDataSource().user().member_info?.end_time ?: return false
            return storage().getBoolean(buildFlag(endTime), false)
        }

        fun setMemberExpiringTimeConfirmed() {
            val endTime = AppContext.appDataSource().user().member_info?.end_time ?: return
            storage().putBoolean(buildFlag(endTime), true)
        }

        private fun buildFlag(endTime: Long) = "${MEMBER_TIPS_SHOWING_TIME_FLAG}_$endTime"

        fun showMemberExpiringTimeTipsDirectly(context: Context) {
            val endTime = AppContext.appDataSource().user().member_info?.end_time ?: return
            OpenMemberDialog.show(context) {
                message = context.getString(R.string.member_will_expired_tips_mask, formatMilliseconds(endTime))
                messageDescId = R.string.member_will_expired_tips_desc
                positiveTextId = R.string.renewal_fee_member
            }
        }

        @Volatile
        private var instance: MemberExpiringTipsTask? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: MemberExpiringTipsTask().also { instance = it }
                }
    }

    var dialog: Dialog? = null

    override fun run() {
        val memberInfo = AppContext.appDataSource().user().member_info
        if (memberInfo == null) {
            finished(true)
            return
        }

        val endTime = memberInfo.end_time

        if (shouldShowMemberExpiringDialog(memberInfo) && getMemberTimingRemainingDay(endTime).shouldShowWillExpire) {
            if (!storage().getBoolean("${MEMBER_TIPS_SHOWING_TIME_FLAG}_$endTime", false)) {
                showMemberExpiringTips(endTime)
            } else {
                finished(true)
            }
        } else {
            finished(true)
        }
    }

    private fun showMemberExpiringTips(endTime: Long) {
        val currentActivity = BaseKit.get().topActivity

        if (currentActivity == null) {
            finished(false)
            return
        }

        dialog = OpenMemberDialog.show(currentActivity) {
            message = currentActivity.getString(R.string.member_will_expired_tips_mask, formatMilliseconds(endTime))
            messageDescId = R.string.member_will_expired_tips_desc
            positiveTextId = R.string.renewal_fee_member
        }.apply {
            setOnDismissListener {
                storage().putBoolean(buildFlag(endTime), true)
                finished(true)
            }
        }
    }

}