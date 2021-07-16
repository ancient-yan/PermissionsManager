package com.gwchina.sdk.base.utils.verify

import android.support.design.widget.TextInputLayout
import android.view.View
import android.widget.TextView
import com.gwchina.sdk.base.utils.textStr
import com.gwchina.sdk.base.widget.text.ValidateCodeInputLayout

internal fun getStringData(view: View): String {
    return when (view) {
        is TextView -> view.text.toString()
        is TextInputLayout -> view.textStr()
        is ValidateCodeInputLayout -> view.textInputLayout.textStr()
        else -> {
            throw IllegalArgumentException("ViewDataAdapter unSupport ")
        }
    }
}