package com.gwchina.sdk.base.utils.verify

import android.view.View


abstract class AbsValidator(val validateView: View) : IValidator {

    abstract val isMatch: Boolean

    abstract val validateData: Any

    override fun validate(): Boolean {
        return isMatch
    }

}
