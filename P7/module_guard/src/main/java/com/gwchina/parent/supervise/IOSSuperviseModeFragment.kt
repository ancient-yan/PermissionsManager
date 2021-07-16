package com.gwchina.parent.supervise

import android.os.Bundle
import android.view.View
import com.android.base.app.fragment.BaseFragment
import com.android.base.kotlin.ignoreCrash
import com.blankj.utilcode.util.IntentUtils
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.utils.enableSpanClickable
import com.gwchina.sdk.base.utils.newGwStyleClickSpan
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.supervise_fragment.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-12 10:37
 */
class IOSSuperviseModeFragment : BaseFragment() {

    override fun provideLayout() = R.layout.supervise_fragment

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        tvWatchOperationalVideo.setOnClickListener {
            openSuperviseModeGuidePage()
        }

        tvSuperviseCallCustomerService.enableSpanClickable()
        tvSuperviseCallCustomerService.text = SpanUtils()
                .append(getString(R.string.setting_has_problem))
                .append(getString(R.string.call_customer_service))
                .setClickSpan(newGwStyleClickSpan(requireContext()) {
                    askToCallService()
                })
                .create()

    }

    private fun openSuperviseModeGuidePage() {
        AppContext.appRouter().build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.IOS_SUPERVISE_MODE)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    private fun askToCallService() {
        showConfirmDialog {
            titleId = R.string.consumer_hotline_title
            messageId = R.string.consumer_hotline
            positiveId = R.string.call
            positiveListener = {
                ignoreCrash {
                    startActivity(IntentUtils.getDialIntent(getString(R.string.consumer_hotline), true))
                }
            }
        }
    }

}