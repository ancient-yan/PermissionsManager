package com.gwchina.parent.guard

import com.gwchina.sdk.base.di.AppModule
import com.gwchina.sdk.debug.DebugAppContext

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-12 10:35
 */
class GuardAppContext : DebugAppContext() {

    override fun injectAppContext() {
        DaggerGuardAppComponent
                .builder()
                .appModule(AppModule(this))
                .build()
                .inject(this)
    }

}