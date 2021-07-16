package com.gwchina.sdk.base.widget.member

import android.app.Dialog
import android.support.v4.app.FragmentActivity
import com.android.base.app.BaseKit
import com.android.base.app.fragment.BaseDialogFragment
import com.app.base.R
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.EXPIRE_RETAIN_FLAG_NOT_SET
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.data.models.deviceCount
import com.gwchina.sdk.base.utils.SequentialUITaskExecutor
import com.gwchina.sdk.base.utils.UITask

/**
 * 会员过期，强制选择孩子和设备
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-10 11:07
 */
class MemberExpiredForceChooseTask : UITask(
        MEMBER_EXPIRED_FORCE_CHOOSE_TASK_ID,
        SequentialUITaskExecutor.SCHEDULE_POLICY_REPEATED
) {

    companion object {

        private const val MEMBER_EXPIRED_FORCE_CHOOSE_TASK_ID = "member_expired_force_choose_task_id"
        private const val MEMBER_EXPIRED_FORCE_CHOOSE_TIME_FLAG = "member_expired_tips_showed_time_flag_"

        private fun storage() = AppContext.storageManager().stableStorage()

        private fun buildFlag(endTime: Long?) = "${MEMBER_EXPIRED_FORCE_CHOOSE_TIME_FLAG}_$endTime"

        internal fun finish() {
            SequentialUITaskExecutor.finished(MEMBER_EXPIRED_FORCE_CHOOSE_TASK_ID, true)
        }

        fun markFlag() {
            val user = AppContext.appDataSource().user()
            storage().putBoolean(buildFlag(user.member_info?.end_time), true)
        }

        @Volatile
        private var instance: MemberExpiredForceChooseTask? = null

        fun getInstance() =
                instance ?: synchronized(this) {
                    instance ?: MemberExpiredForceChooseTask().also { instance = it }
                }

    }

    var dialog: Dialog? = null
    var dialogFragment: BaseDialogFragment? = null

    override fun run() {
        val user = AppContext.appDataSource().user()
        if (shouldShowMemberExpiredForceChooseDialog(user)) {
            if (user.deviceCount() > 1) {
                if (EXPIRE_RETAIN_FLAG_NOT_SET == user.member_info?.expire_setting_retain_flag) {
                    showExpiredForcedChoosingDialog()
                } else {
                    finished(true)
                }
            } else {
                showExpiredTipsDialog(user)
            }
        } else {
            finished(true)
        }
    }

    private fun showExpiredTipsDialog(user: User) {
        if (storage().getBoolean(buildFlag(user.member_info?.end_time), false)) {
            finished(true)
            return
        }

        val currentActivity = BaseKit.get().topActivity

        if (currentActivity == null) {
            finished(false)
            return
        }

        dialog = OpenMemberDialog.show(currentActivity) {
            messageId = R.string.member_expired_at_yesterday
            messageDesc = SpanUtils()
                    .append(currentActivity.getString(R.string.member_will_expired_tips_desc))
                    .appendLine()
                    .appendLine()
                    .append(currentActivity.getString(R.string.member_expired_functions_desc))
                    .create()
            positiveTextId = R.string.renewal_fee_member
        }.apply {
            setOnDismissListener {
                storage().putBoolean(buildFlag(user.member_info?.end_time), true)
                finished(true)
            }
        }
    }

    private fun showExpiredForcedChoosingDialog() {
        val currentActivity = BaseKit.get().topActivity
        if (currentActivity == null && currentActivity !is FragmentActivity) {
            finished(false)
            return
        }
        try {
            dialogFragment = MemberExpiredDialog.showMemberExpired((currentActivity as FragmentActivity).supportFragmentManager)
        } catch (e: Exception) {
            finished(false)
        }
    }

}