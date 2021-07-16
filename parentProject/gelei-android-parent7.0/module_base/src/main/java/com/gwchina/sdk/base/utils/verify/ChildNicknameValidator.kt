package com.gwchina.sdk.base.utils.verify

import android.view.View
import com.app.base.R

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-29 17:52
 */
class ChildNicknameValidator private constructor(view: View) : TextValidator(view) {

    public override fun emptyTips(): Int {
        return R.string.nick_name_empty_tips
    }

    public override fun noMatchTips(): Int {
        return R.string.child_nick_name_tips
    }

    override fun validateTypeText(content: String): Boolean {
        return content.isLegalChildNickname()
    }

    companion object {
        fun validate(view: View): Boolean {
            return ChildNicknameValidator(view).validate()
        }
    }

}