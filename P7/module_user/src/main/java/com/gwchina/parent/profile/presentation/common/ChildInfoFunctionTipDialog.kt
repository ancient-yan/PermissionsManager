package com.gwchina.parent.profile.presentation.common

import android.content.Context
import com.gwchina.lssw.parent.user.R
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.dialog_child_info_function_tip.*

/**
 *@author hujie
 *      Email: hujie1991@126.com
 *      Date : 2019-12-17 16:25
 */
class ChildInfoFunctionTipDialog(context: Context, private val onKnow: (() -> Unit)? = null) : BaseDialog(context) {

    init {
        setContentView(R.layout.dialog_child_info_function_tip)
        setupViews()
        setCancelable(false)
    }

    override var maxDialogWidthPercent = 0.9F

    private fun setupViews() {
        setListener()
    }

    private fun setListener() {
        tvKnow.setOnClickListener {
            onKnow?.invoke()
            dismiss()
        }
    }
}