package com.gwchina.sdk.base.utils.verify

import android.text.TextUtils
import android.view.View

import com.app.base.R

  class ConfirmPasswordValidator private constructor(view: View, private val mNewPassword: String) : TextValidator(view) {

    override fun validateTypeText(content: String): Boolean {
        return !TextUtils.isEmpty(mNewPassword) && mNewPassword == content
    }

    public override fun emptyTips(): Int {
        return R.string.confirm_password_tips
    }

    public override fun noMatchTips(): Int {
        return R.string.confirm_password_error_tips
    }

    companion object {

        fun validate(newPassword: String, view: View): Boolean {
            return ConfirmPasswordValidator(view, newPassword).validate()
        }
    }

}
