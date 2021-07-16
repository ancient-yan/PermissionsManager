package com.gwchina.parent.account

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.gwchina.lssw.parent.account.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.router.RouterPath

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 14:10
 */
class AccountDebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_actiivty_debug)
    }

    fun openAccount(view: View) {
        AppContext.appRouter().build(RouterPath.Account.PATH).navigation()
    }


}