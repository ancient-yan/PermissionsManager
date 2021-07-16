package com.gwchina.sdk.base.widget.member

import android.content.Context
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.app.base.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.dialog_open_member.*

class OpenMemberDialogConfig internal constructor(private val context: Context) {
    var message: CharSequence = ""
    var messageId: Int = 0
        set(value) {
            message = context.getText(value)
        }

    var messageDesc: CharSequence = ""
    var messageDescId: Int = 0
        set(value) {
            messageDesc = context.getText(value)
        }

    var positiveText: CharSequence = ""
    var positiveTextId: Int = 0
        set(value) {
            positiveText = context.getText(value)
        }

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
class OpenMemberDialog(
        context: Context,
        private val config: OpenMemberDialogConfig
) : BaseDialog(context) {

    companion object {

        fun show(context: Context, config: (OpenMemberDialogConfig.() -> Unit)): OpenMemberDialog {
            val openMemberDialogConfig = OpenMemberDialogConfig(context)
            config(openMemberDialogConfig)
            val openMemberDialog = OpenMemberDialog(context, openMemberDialogConfig)
            openMemberDialog.show()
            return openMemberDialog
        }

        fun showSVip(context: Context, msgId: Int, mask: String): OpenMemberDialog {
            return show(context) {
                message = context.getString(msgId, mask)
                messageDesc = context.getString(R.string.open_member_to_get_more_function_tips_mask, mask)
                positiveText = context.getString(R.string.open_vip_to_experience_mask, mask)
            }
        }
    }

    init {
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_open_member)
        setupViews()
    }

    private fun setupViews() {
        tvDialogMemberFunctionsDesc.text = config.message

        if (config.messageDesc.isNotEmpty()) {
            tvDialogMemberFunctionsDescSub.visible()
            tvDialogMemberFunctionsDescSub.text = config.messageDesc
        } else {
            tvDialogMemberFunctionsDescSub.gone()
        }

        btnDialogMemberOpen.text = config.positiveText

        ivDialogMemberClose.setOnClickListener {
            dismiss()
        }

        btnDialogMemberOpen.setOnClickListener {
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