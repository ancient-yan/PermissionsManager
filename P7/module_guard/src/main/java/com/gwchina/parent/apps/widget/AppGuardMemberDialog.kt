package com.gwchina.parent.apps.widget

import android.content.Context
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.dialog_app_guard_member.*

class AppGuardMemberDialogConfig internal constructor(private val context: Context) {
    var message: CharSequence = ""
    var messageId: Int = 0
        set(value) {
            message = context.getText(value)
        }

    var openMemberDesc: CharSequence = context.getString(R.string.open_svip_app_guard_tips)

    var positiveText: CharSequence = ""

    /**如果不设置，则默认跳转到会员界面*/
    var onConfirmListener: (() -> Unit)? = null
}

/**
 * 要求开通会员弹框
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-17 15:56
 */
class AppGuardMemberDialog(
        context: Context,
        private val config: AppGuardMemberDialogConfig
) : BaseDialog(context) {

    companion object {

        private fun show(context: Context, config: (AppGuardMemberDialogConfig.() -> Unit)): AppGuardMemberDialog {
            val openMemberDialogConfig = AppGuardMemberDialogConfig(context)
            config(openMemberDialogConfig)
            val openMemberDialog = AppGuardMemberDialog(context, openMemberDialogConfig)
            openMemberDialog.show()
            return openMemberDialog
        }

        fun showTips(context: Context, msg: String, btnId: Int): AppGuardMemberDialog {
            return show(context) {
                message = msg
                positiveText = context.getText(btnId)
            }
        }
    }

    init {
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_app_guard_member)
        setupViews()
    }

    private fun setupViews() {
        tvDialogMemberFunctionsDesc.text = config.message

        btnKnow.setOnClickListener {
            dismiss()
        }
        btnKnow.text = config.positiveText

        tvOpenSVip.text = config.openMemberDesc
        tvOpenSVip.setOnClickListener {
            dismiss()
            if (config.onConfirmListener != null) {
                config.onConfirmListener?.invoke()
            } else {
                AppContext.appRouter().build(RouterPath.MemberCenter.PATH)
                        .withInt(RouterPath.PAGE_KEY, RouterPath.MemberCenter.CENTER)
                        .navigation()
            }
        }
    }
}