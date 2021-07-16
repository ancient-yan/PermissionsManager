package com.gwchina.parent.main.presentation.device

import android.content.Context
import android.text.Html
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.textWatcher
import com.android.base.utils.android.SoftKeyboardUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.utils.hidePhoneNumber
import com.gwchina.sdk.base.utils.textStr
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.bound_device_dialog_unbind.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-27 14:24
 */
class UnbindDialog(context: Context, onConfirmUnbindListener: (String) -> Unit) : BaseDialog(context) {

    init {
        setContentView(R.layout.bound_device_dialog_unbind)

        setCancelable(false)

        // click：监听短信验证码输入
        cetDialogEnterSmsCode.editText.textWatcher {
            onTextChanged { charSequence, _, _, _ ->
                dblUnbindLayout.positiveEnable = charSequence?.length ?: 0 > 5
                if (dblUnbindLayout.positiveEnable) {
                    dblUnbindLayout.positiveColor(context.colorFromId(R.color.color_text_green))
                } else {
                    dblUnbindLayout.positiveColor(context.colorFromId(R.color.gray_level3))
                }
                tvUnbindTip.text = ""
            }
        }

        cetDialogEnterSmsCode.editText.setText("")

        // click：取消绑定
        dblUnbindLayout.onNegativeClick {
            dismiss()
        }

        // click：确认绑定
        dblUnbindLayout.positiveText(context.getString(R.string.confirm_unbind))
        dblUnbindLayout.onPositiveClick {
            dblUnbindLayout.positiveEnable = false
            SoftKeyboardUtils.hideSoftInput(cetDialogEnterSmsCode.editText)
            it.post {
                onConfirmUnbindListener(cetDialogEnterSmsCode.editText.textStr())
            }
        }
    }

    fun setSentPhoneNumber(mPhoneNumber: String) {
        val content = context.getString(R.string.dialog_verity_code, hidePhoneNumber(mPhoneNumber))
        tvDialogTips.text = Html.fromHtml(content)
    }

    override fun show() {
        super.show()
        cetDialogEnterSmsCode?.postDelayed({
            SoftKeyboardUtils.showSoftInput(cetDialogEnterSmsCode.editText)
        }, 400)
    }

    override fun dismiss() {
        cetDialogEnterSmsCode.editText.setText("")
        SoftKeyboardUtils.hideSoftInput(cetDialogEnterSmsCode.editText)
        super.dismiss()
    }

    fun unbindError(mes: String?) {
        tvUnbindTip.text = mes
        dblUnbindLayout.positiveEnable = false
        dblUnbindLayout.positiveColor(context.colorFromId(R.color.gray_level3))
    }

}