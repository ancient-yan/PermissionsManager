package com.gwchina.sdk.base.utils.verify

import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.EditText
import com.android.base.utils.common.StringChecker

/**是否符合手机号的长度*/
fun CharSequence?.matchCellphone(): Boolean {
    return if (this == null) {
        false
    } else {
        return StringChecker.isChinaPhoneNumber(this.toString())
    }
}


/**是否符合Gw的密码规范*/
fun CharSequence?.matchGwPassword(): Boolean {
    return if (this == null) {
        false
    } else {
        eqGwPassword(this.toString())
    }
}

/**是否符合手机号的长度*/
fun CharSequence?.matchCellphoneLength(): Boolean {
    return if (this == null) {
        false
    } else {
        return length == 11
    }
}


/**是否符合Gw的密码规范*/
fun CharSequence?.matchGwPasswordLength(): Boolean {
    return if (this == null) {
        false
    } else {
        eqGwPasswordLength(this.toString())
    }
}


/**
 * 8-16位英文数字组合
 */
private fun  eqGwPassword(string: String): Boolean {
    return StringChecker.isLengthIn(string, 8, 16)
            && StringChecker.containsDigtalLetterOnly(string)
            && StringChecker.containsDigital(string)
            && StringChecker.containsLetter(string)
}

/**8-16位*/
private fun eqGwPasswordLength(string: String): Boolean {
    return StringChecker.isLengthIn(string, 8, 16)
}

/**获取到焦点后就清空错误*/
fun EditText.clearErrorWhenHasFocus() {
    this.setOnFocusChangeListener { _: View, hasFocus: Boolean ->
        if (hasFocus) {
            this.error = null
        }
    }
}

/**获取到焦点后就清空错误*/
fun TextInputLayout.clearErrorWhenHasFocus() {
    this.editText?.setOnFocusChangeListener { _: View, hasFocus: Boolean ->
        if (hasFocus) {
            this.error = null
        }
    }
}

/**是否是合法的孩子呢称*/
fun CharSequence?.isLegalChildNickname(): Boolean {
    return this != null && this.matches(Regex("[ _`~!@#\$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\\n|\\r|\\t"))
}