package com.gwchina.parent.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.launcher.LauncherActivity
import com.gwchina.sdk.debug.DebugActivity

class HomeDebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_debug)
    }

    fun openLauncher(view: View) {
        startActivity(Intent(this, LauncherActivity::class.java))
    }

    fun openDebug(view: View) {
        startActivity(Intent(this, DebugActivity::class.java))
    }

}
