package com.gwchina.sdk.base.widget.dialog

import android.view.View
import com.app.base.R
import com.gwchina.sdk.base.widget.dialog.BaseDialogBuilder.Companion.NO_ID
import kotlinx.android.synthetic.main.dialog_custom_layout.*

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-29 10:49
 */
internal class CustomDialog(builder: CustomDialogBuilder) : BaseDialog(builder.context, true, builder.style) {

    init {
        setContentView(R.layout.dialog_custom_layout)
        applyBuilder(builder)
    }

    private fun applyBuilder(builder: CustomDialogBuilder) {
        //content
        val view = builder.view
        if (view != null) {
            flDialogCustom.addView(view)
        } else if (builder.layoutId != NO_ID) {
            View.inflate(context, builder.layoutId, flDialogCustom)
        }
        builder.onLayoutPrepared?.invoke(flDialogCustom)

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
        })

    }

    private fun dismissChecked(builder: CustomDialogBuilder) {
        if (builder.autoDismiss) {
            dismiss()
        }
    }

}
