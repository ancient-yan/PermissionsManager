package com.gwchina.parent.guard

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.android.base.app.activity.BaseActivity
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardActivity
import com.gwchina.parent.family.FamilyPhoneActivity
import com.gwchina.parent.level.GuardLevelActivity
import com.gwchina.parent.net.NetGuardActivity
import com.gwchina.parent.times.TimeGuardActivity
import com.gwchina.sdk.debug.DebugActivity

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-12 10:34
 */
class GuardDebugActivity : BaseActivity() {

    override fun layout() = R.layout.guard_activity_debug

    override fun setupView(savedInstanceState: Bundle?) {
        //no op
    }

    fun openDebug(view: View) {
        startActivity(Intent(this, DebugActivity::class.java))
    }

    fun openTimeGuard(view: View) {
        startActivity(Intent(this, TimeGuardActivity::class.java))
    }

    fun openSoftwareGuard(view: View) {
        startActivity(Intent(this, AppGuardActivity::class.java))
    }

    fun openLevel(view: View) {
        startActivity(Intent(this, GuardLevelActivity::class.java))
    }

    fun openNetGuard(view: View) {
        startActivity(Intent(this, NetGuardActivity::class.java))
    }

    fun openFamily(view: View) {
        startActivity(Intent(this, FamilyPhoneActivity::class.java))
    }

}