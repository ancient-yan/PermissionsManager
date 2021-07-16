package com.gwchina.sdk.base.utils.verify

import android.view.View

import com.app.base.R

class CodeValidator private constructor(view: View) : TextValidator(view) {

    override fun validateTypeText(content: String): Boolean {
        return content.isNotEmpty()
    }

    public override fun emptyTips(): Int {
        return R.string.sms_code_enter_tips
    }

    public override fun noMatchTips(): Int {
        return R.string.sms_code_no_match_tips
    }

    companion object {

        fun validate(codeIil: View): Boolean {
            return CodeValidator(codeIil).validate()
        }
    }

}
