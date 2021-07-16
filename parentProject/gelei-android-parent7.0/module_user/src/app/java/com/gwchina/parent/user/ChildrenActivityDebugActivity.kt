package com.gwchina.parent.user

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.gwchina.gelei.parent.user.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.router.RouterPath

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-01-20 22:46
 */
class ChildrenActivityDebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.children_activity_debug)
    }

    fun openChildInfo(view: View) {
        AppContext.appRouter().build(RouterPath.MemberCenter.PATH)?.navigation()
    }

}