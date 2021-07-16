package com.gwchina.parent.main

import android.app.Activity
import android.content.Intent
import com.android.base.app.activity.ActivityDelegate
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-29 18:47
 */
class ResultProcessor : ActivityDelegate<MainActivity> {

    private lateinit var mainActivity: MainActivity

    override fun onAttachedToActivity(activity: MainActivity) {
        mainActivity = activity
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        //注册后，需要提示绑定设备
        if (requestCode == RouterPath.Account.REQUEST_CODE && data != null) {
            if (RouterPath.Account.LOGIN_TYPE_NEW == data.getIntExtra(RouterPath.Account.LOGIN_TYPE_KEY, 0)) {
                val vipPresentTips = data.getStringExtra(RouterPath.Account.VIP_PRESENT_DAYS_KEY)
                mainActivity.window.decorView.post {
                    if (vipPresentTips.isNullOrEmpty()) {
                        showBindChildDeviceTips()
                    } else {
                        showPresentVIPTips(vipPresentTips)
                    }
                }
            }
        }
    }

    private fun showPresentVIPTips(vipPresentTips: String) {
        showConfirmDialog(mainActivity) {
            noNegative()
            message = vipPresentTips
            positiveId = R.string.okay
            titleId = R.string.register_success
            iconId = R.drawable.icon_status_normal
            positiveListener = {
                showBindChildDeviceTips()
            }
        }.setCancelable(false)
    }

    private fun showBindChildDeviceTips() {
        showConfirmDialog(mainActivity) {
            messageId = R.string.tip_to_bind_child_device
            positiveId = R.string.binding_devices
            negativeText = "暂不绑定"
            titleId = R.string.register_success
            iconId = R.drawable.icon_status_normal
            positiveListener = {
                StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_BUND)
                mainActivity.appRouter.build(RouterPath.Binding.PATH).navigation(mainActivity, RouterPath.Binding.REQUEST_CODE)
            }
        }.setCanceledOnTouchOutside(false)
    }

}