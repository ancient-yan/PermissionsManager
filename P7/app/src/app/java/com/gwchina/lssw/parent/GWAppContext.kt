package com.gwchina.lssw.parent

import android.app.Activity
import android.content.Intent
import com.gwchina.parent.launcher.LauncherActivity
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.di.AppModule

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-10-12 21:53
 */
class GWAppContext : AppContext() {

    override fun injectAppContext() {
        DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()
                .inject(this)
    }

    override fun restartApp(activity: Activity) {
        val intent = Intent(this, LauncherActivity::class.java)
        intent.flags = intent.flags or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

}