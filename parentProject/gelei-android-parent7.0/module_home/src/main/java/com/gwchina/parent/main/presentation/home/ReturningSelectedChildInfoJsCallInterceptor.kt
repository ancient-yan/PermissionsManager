package com.gwchina.parent.main.presentation.home

import android.os.Bundle
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.web.BaseCustomJsCallInterceptor
import com.gwchina.sdk.base.web.BaseWebFragment
import com.gwchina.sdk.base.web.ResultReceiver
import com.gwchina.sdk.base.web.actions.JSCALL_GET_CHILD_INFORMATION
import com.gwchina.sdk.base.web.actions.jscallReturnQueryChildInfo

class ReturningSelectedChildInfoJsCallInterceptor : BaseCustomJsCallInterceptor {

    private var childId: String? = ""

    override fun onInit(host: BaseWebFragment, bundle: Bundle?) {
        childId = bundle?.getString(CHILD_USER_ID_KEY, "")
    }

    override fun intercept(method: String, args: Array<String>?, resultReceiver: ResultReceiver?): Boolean {

        if (JSCALL_GET_CHILD_INFORMATION == method) {
            val user = AppContext.appDataSource().user()
            val child = user.childList?.find { it.child_user_id == childId }
            jscallReturnQueryChildInfo(child, resultReceiver, child?.device_list?.firstOrNull()?.device_id)
            return true
        }

        return false
    }

}