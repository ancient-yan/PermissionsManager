package com.gwchina.sdk.base.upgrade

import com.gwchina.sdk.base.utils.SequentialUITaskExecutor
import com.gwchina.sdk.base.utils.UITask


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-26 16:27
 */
class AppUpgradeTask : UITask(APP_UPGRADE_TASK_ID, SequentialUITaskExecutor.SCHEDULE_POLICY_TILL_SUCCESS) {

    companion object {
        private const val APP_UPGRADE_TASK_ID = "app_upgrade_task_id"
    }

    private val sequenceAppUpgradeChecker = SequenceAppUpgradeChecker()

    override fun run() {
        sequenceAppUpgradeChecker.checkAppUpgrade()
    }

    private inner class SequenceAppUpgradeChecker : AppUpgradeSilentChecker() {

        override fun onCheckEnd(success: Boolean) {
            finished(success)
        }
    }

}