package com.gwchina.sdk.base.utils.verify

import android.view.View
import com.app.base.R

class PasswordValidator private constructor(view: View) : TextValidator(view) {

    override fun validateTypeText(content: String): Boolean {
        return content.matchGwPassword()
    }

    public override fun emptyTips(): Int {
        return R.string.password_enter_tips
    }

    public override fun noMatchTips(): Int {
        return R.string.password_no_match_tips
    }

    companion object {
        fun validate(view: View): Boolean {
            return PasswordValidator(view).validate()
        }
    }

}
