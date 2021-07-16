package com.gwchina.sdk.base.widget.dialog

import android.view.View
import com.android.base.kotlin.clearComponentDrawable
import com.android.base.kotlin.gone
import com.android.base.kotlin.leftDrawable
import com.android.base.kotlin.visible
import com.app.base.R
import com.gwchina.sdk.base.widget.dialog.BaseDialogBuilder.Companion.NO_ID
import kotlinx.android.synthetic.main.dialog_confirm_layout.*

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-29 10:49
 */
internal class ConfirmDialog(builder: ConfirmDialogBuilder) : BaseDialog(builder.context, true, builder.style) {

    init {
        setContentView(R.layout.dialog_confirm_layout)
        applyBuilder(builder)
    }

    private fun applyBuilder(builder: ConfirmDialogBuilder) {
        //icon
        if (builder.iconId != NO_ID) {
            tvConfirmDialogTitle.leftDrawable(builder.iconId)
        } else {
            tvConfirmDialogTitle.clearComponentDrawable()
        }

        //title
        val title = builder.title
        if (title != null) {
            tvConfirmDialogTitle.visible()
            tvConfirmDialogTitle.text = title
            tvConfirmDialogTitle.textSize = builder.titleSize
            tvConfirmDialogTitle.setTextColor(builder.titleColor)
        }

        //message
        tvConfirmDialogMessage.text = builder.message
        tvConfirmDialogMessage.textSize = builder.messageSize
        tvConfirmDialogMessage.setTextColor(builder.messageColor)
        tvConfirmDialogMessage.gravity = builder.messageGravity
        //如果没有 title，则 message 为粗体
        if (tvConfirmDialogTitle.visibility != View.VISIBLE) {
            tvConfirmDialogMessage.paint.isFakeBoldText = true
            tvConfirmDialogMessage.invalidate()
        }

        //checkbox
        if (builder.checkBoxText.isNotEmpty()) {
            cbConfirmDialogCheckBox.visible()
            cbConfirmDialogCheckBox.text = builder.checkBoxText
            cbConfirmDialogCheckBox.isChecked = builder.checkBoxChecked
        } else {
            cbConfirmDialogCheckBox.gone()
        }

        //cancel
        val negativeText = builder.negativeText
        if (negativeText != null) {
            dblDialogBottom.negativeText(negativeText)
            dblDialogBottom.onNegativeClick(View.OnClickListener {
                dismissChecked(builder)
                builder.negativeListener?.invoke(this)
            })
        } else {
            dblDialogBottom.hideNegative()
        }

        //confirm
        dblDialogBottom.positiveText(builder.positiveText)
        dblDialogBottom.onPositiveClick(View.OnClickListener {
            dismissChecked(builder)
            builder.positiveListener?.invoke(this)
            builder.positiveListener2?.invoke(this, cbConfirmDialogCheckBox.isChecked)
        })

    }

    private fun dismissChecked(builder: ConfirmDialogBuilder) {
        if (builder.autoDismiss) {
            dismiss()
        }
    }

}
