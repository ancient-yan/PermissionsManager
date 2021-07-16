package com.gwchina.parent.account

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.account.R
import com.gwchina.parent.account.presentation.smslogin.SmsLoginFragment
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.router.RouterPath

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 14:20
 */
@Route(path = RouterPath.Account.PATH)
class AccountActivity : InjectorAppBaseActivity() {

    override fun layout() = R.layout.app_base_activity

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        savedInstanceState.ifNull {
            inFragmentTransaction {
                addWithDefaultContainer(SmsLoginFragment())
            }
        }
    }

}