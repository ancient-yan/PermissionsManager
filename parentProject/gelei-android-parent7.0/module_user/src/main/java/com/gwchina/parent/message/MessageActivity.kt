package com.gwchina.parent.message

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.message.presentation.MessageFragment
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.router.RouterPath

/**
 * 个人信息
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-03-18 15:25
 */
@Route(path = RouterPath.Message.PATH)
class MessageActivity : InjectorAppBaseActivity() {

    override fun layout() = R.layout.app_base_activity

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        savedInstanceState.ifNull {
            inFragmentTransaction {
                addWithDefaultContainer(MessageFragment())
            }
        }
    }

}