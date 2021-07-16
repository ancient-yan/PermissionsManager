package com.gwchina.sdk.base.upgrade

import android.content.Context
import android.text.Html
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.app.base.R
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.dialog_upgrade.*


/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-02-05 13:46
 */
internal class UpgradeTipsDialog(context: Context) : BaseDialog(context, true) {

    init {
        setContentView(R.layout.dialog_upgrade)
        setCancelable(false)
    }

    override var maxDialogWidthPercent = 0.7F

    fun setContent(title: String, content: String, forcedUpdate: Boolean, operationListener: (Boolean) -> Unit) {
        tvDialogUpgradeTitle.text = title
        tvDialogUpgradeContent.text = Html.fromHtml(content)

        tvDialogUpgradeConfirm.setOnClickListener { operationListener(true) }
        tvDialogUpgradeCancel.setOnClickListener { operationListener(false) }

        if (forcedUpdate) {
            showForcedUpgradeLayout()
        } else {
            showOptionalUpgradeLayout()
        }

        setCancelable(false)
    }

    private fun showOptionalUpgradeLayout() {
        tvDialogUpgradeConfirm.visible()
        tvDialogUpgradeCancel.visible()
    }

    private fun showForcedUpgradeLayout() {
        tvDialogUpgradeConfirm.visible()
        tvDialogUpgradeCancel.gone()
    }

}

