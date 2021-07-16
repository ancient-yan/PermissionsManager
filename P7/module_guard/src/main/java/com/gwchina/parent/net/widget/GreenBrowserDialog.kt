package com.gwchina.parent.net.widget

import android.content.Context
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.net_dialog_browser.*

/**
 *@author hujie
 *      Email: hujie1991@126.com
 *      Date : 2019-08-22 14:37
 *      提示安装绿网游览器弹框
 */
class GreenBrowserDialog(
        context: Context, var onConfirmListener: (() -> Unit)? = null) : BaseDialog(context) {

    init {
        setCanceledOnTouchOutside(false)
        setCancelable(false)
        setContentView(R.layout.net_dialog_browser)
        setupViews()
    }

    private fun setupViews() {
        btnDialogBrowser.setOnClickListener {
            dismiss()
            if (onConfirmListener != null) {
                onConfirmListener?.invoke()
            }
        }
    }
}