package com.gwchina.parent.account

import com.gwchina.sdk.base.di.AppModule
import com.gwchina.sdk.debug.DebugAppContext

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 14:08
 */
class AccountAppContext : DebugAppContext() {

    override fun injectAppContext() {
        DaggerAccountAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
                .inject(this)
    }

}