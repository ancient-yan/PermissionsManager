package com.gwchina.parent.migration.presentation

import android.support.v4.app.Fragment
import com.blankj.utilcode.util.IntentUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog


internal fun setMigrationStarted() {
    AppSettings.settingsStorage().putBoolean(AppSettings.NEED_MIGRATION_FLAG, true)
}

internal fun setMigrationEnded() {
    AppSettings.settingsStorage().putBoolean(AppSettings.NEED_MIGRATION_FLAG, false)
}

internal fun Fragment.showCallServiceDialog() {
    showConfirmDialog {
        message = "是否拨打客服电话：${getString(R.string.consumer_hotline)}"
        positiveText = "拨打"
        positiveListener = {
            val dialIntent = IntentUtils.getDialIntent(getString(R.string.consumer_hotline))
            if (dialIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(dialIntent)
            }
        }
    }
}