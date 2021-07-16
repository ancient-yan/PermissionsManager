package com.gwchina.sdk.base.utils.verify

import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.android.base.utils.android.ResourceUtils.getString
import com.gwchina.sdk.base.widget.dialog.TipsManager
import com.gwchina.sdk.base.widget.text.ValidateCodeInputLayout

/**
 * 验证手机号的输入
 */
fun validateCellphone(view: View): Boolean {
    return CellphoneNumberValidator.validate(view)
}

/**
 * 验证密码输入
 */
fun validatePassword(view: View): Boolean {
    return PasswordValidator.validate(view)
}

/**
 * 验证确认密码
 */
fun validateConfirmPassword(confirmPassword: String, view: View): Boolean {
    return ConfirmPasswordValidator.validate(confirmPassword, view)
}

/**
 * 验证验证码
 */
fun validateSmsCode(view: View): Boolean {
    return CodeValidator.validate(view)
}

/**
 * 验证孩子昵称是否合法
 */
fun validateChildNickname(view: View): Boolean {
    return ChildNicknameValidator.validate(view)
}

///////////////////////////////////////////////////////////////////////////
// utils
///////////////////////////////////////////////////////////////////////////
internal fun handleNoMatch(view: View, strId: Int) {
    val message = getString(strId)

    when (view) {
        is EditText -> {
            view.error = message
            view.requestFocus()
        }

        is TextView -> {//keep it,maybe change
            //SoftKeyboardUtils.showErrorImmediately(message, view)
            view.error = message
            view.requestFocus()
        }

        is TextInputLayout -> {
            view.isErrorEnabled = true
            view.error = message
        }

        is ValidateCodeInputLayout -> {
            view.textInputLayout.isErrorEnabled = true
            view.textInputLayout.error = message
        }
        else -> TipsManager.showMessage(message)
    }
}

internal fun handleMatch(view: View) {
    when (view) {
        is TextView -> {
            view.error = null
        }

        is TextInputLayout -> {
            view.isErrorEnabled = false
            view.error = null
        }

        is ValidateCodeInputLayout -> {
            view.textInputLayout.isErrorEnabled = false
            view.textInputLayout.error = null
        }
    }
}
