package com.gwchina.sdk.base.widget.dialog

import android.content.Context
import com.app.base.R
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.utils.enableSpanClickable
import com.gwchina.sdk.base.utils.newGwStyleClickSpan
import kotlinx.android.synthetic.main.dialog_privacy_agreement.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-10 13:16
 */
class PrivacyAgreementDialog(context: Context, private val onAgree: (() -> Unit)? = null) : BaseDialog(context) {

    init {
        setContentView(R.layout.dialog_privacy_agreement)
        setupViews()
        setCancelable(false)
    }

    private fun setupViews() {
        tvDialogPrivacyAgree.setOnClickListener {
            onAgree?.invoke()
            dismiss()
        }
        tvDialogPrivacyNotAgree.setOnClickListener {
            AppUtils.exitApp()
        }

        setPrivacyAgreementText()
    }

    private fun setPrivacyAgreementText() {
        tvDialogPrivacy.enableSpanClickable()
        tvDialogPrivacy.text = SpanUtils()
                .append(context.getString(R.string.user_agreement_with_guillemet))
                .setClickSpan(newGwStyleClickSpan(context) {
                    AppContext.appRouter().build(RouterPath.Browser.PATH)
                            .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.AGREEMENT)
                            .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                            .navigation()
                })
                .append("   ")
                .append(context.getString(R.string.privacy_policy_with_guillemet))
                .setClickSpan(newGwStyleClickSpan(context) {
                    AppContext.appRouter().build(RouterPath.Browser.PATH)
                            .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.PRIVACY)
                            .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                            .navigation()
                })
                .create()
    }

}