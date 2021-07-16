package com.gwchina.sdk.base.utils.verify

import android.view.View

import com.android.base.utils.common.StringChecker
import com.app.base.R

class CellphoneNumberValidator private constructor(view: View) : TextValidator(view) {

    public override fun emptyTips(): Int {
        return R.string.cellphone_enter_tips
    }

    public override fun noMatchTips(): Int {
        return R.string.cellphone_no_match_tips
    }

    override fun validateTypeText(content: String): Boolean {
        return StringChecker.isChinaPhoneNumber(content)
    }

    companion object {

        fun validate(view: View): Boolean {
            return CellphoneNumberValidator(view).validate()
        }
    }

}
