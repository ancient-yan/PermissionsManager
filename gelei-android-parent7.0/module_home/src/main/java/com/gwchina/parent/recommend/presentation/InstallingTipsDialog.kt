package com.gwchina.parent.recommend.presentation

import android.support.v4.app.Fragment
import android.widget.CheckBox
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.utils.getWeeOfToday
import com.gwchina.sdk.base.widget.dialog.showCustomDialog

private const val INSTALLING_NO_TIPS_AGAIN_FLAG = "recommend_installing_no_tips_again_flag"
private const val INSTALLING_TIPS_DAY_FLAG = "recommend_installing_tips_day_flag"

internal fun Fragment.showInstallingTips(onConfirm: () -> Unit) {
    val weeOfToday = getWeeOfToday()
    val stableStorage = AppContext.storageManager().stableStorage()

    if (stableStorage.getBoolean(INSTALLING_NO_TIPS_AGAIN_FLAG, false) || weeOfToday.toString() == stableStorage.getString(INSTALLING_TIPS_DAY_FLAG, "")) {
        onConfirm()
        return
    }

    var noTipsAgain = false

    showCustomDialog {
        noNegative()

        layoutId = R.layout.reco_dialog_installing_tips

        onLayoutPrepared = {
            it.findViewById<CheckBox>(R.id.cbRecoNoTipsAgain).setOnCheckedChangeListener { _, isChecked -> noTipsAgain = isChecked }
        }

        positiveId = R.string.i_got_it

        positiveListener = {
            stableStorage.putString(INSTALLING_TIPS_DAY_FLAG, weeOfToday.toString())
            stableStorage.putBoolean(INSTALLING_NO_TIPS_AGAIN_FLAG, noTipsAgain)
            onConfirm()
        }
    }?.setCanceledOnTouchOutside(false)
}