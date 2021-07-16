package com.gwchina.parent.main

import com.android.base.app.BaseKit
import com.android.base.kotlin.onDismiss
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.utils.SequentialUITaskExecutor
import com.gwchina.sdk.base.utils.UITask
import com.gwchina.sdk.base.widget.dialog.PrivacyAgreementDialog

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-10 13:58
 */
class UserPrivacyAgreementTask : UITask(
        USER_PRIVACY_AGREEMENT_TASK_ID,
        SequentialUITaskExecutor.SCHEDULE_POLICY_ONCE
) {

    companion object {
        private const val USER_PRIVACY_AGREEMENT_TASK_ID = "USER_PRIVACY_AGREEMENT_TASK_ID"
        private const val AGREE_PRIVACY = "agree_privacy"
    }

    override fun run() {
        val topActivity = BaseKit.get().topActivity
        if (topActivity == null) {
            finished()
            return
        }

        if (AppSettings.settingsStorage().getBoolean(AGREE_PRIVACY, false)) {
            finished()
            return
        }
        PrivacyAgreementDialog(topActivity) {
            AppSettings.settingsStorage().putBoolean(AGREE_PRIVACY, true)
        }.onDismiss {
            finished()
        }.show()

    }

}