package com.gwchina.parent.supervise

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.app.AppBaseActivity
import com.gwchina.sdk.base.router.RouterPath


/**
 * IOS 监督模式
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-22 11:36
 */
@Route(path = RouterPath.IOSSuperviseMode.PATH)
class IOSSuperviseModeActivity : AppBaseActivity() {

    override fun layout() = R.layout.app_base_activity

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        savedInstanceState.ifNull {
            inFragmentTransaction {
                addWithDefaultContainer(IOSSuperviseModeFragment())
            }
        }
    }

}