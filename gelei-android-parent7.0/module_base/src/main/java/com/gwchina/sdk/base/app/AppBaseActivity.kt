package com.gwchina.sdk.base.app

import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.v4.content.ContextCompat
import com.android.base.app.activity.BaseActivity
import com.android.base.rx.AutoDisposeLifecycleOwnerEx
import com.android.base.utils.android.compat.SystemBarCompat
import com.app.base.R
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.router.RouterManager
import com.gwchina.sdk.base.utils.setStatusBarLightMode

/**
 * @author Ztiany
 * Date : 2018-09-21 14:34
 */
abstract class AppBaseActivity : BaseActivity(), AutoDisposeLifecycleOwnerEx {

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        if (enableStatusBarLightMode()) {
            AppSettings.setSupportStatusBarLightMode(setStatusBarLightMode())
        }
        RouterManager.inject(this)
    }

    @CallSuper
    override fun setupView(savedInstanceState: Bundle?) {
        if (tintStatusBar()) {
            SystemBarCompat.setTranslucentStatusOn19(this)
            if (enableStatusBarLightMode() && AppSettings.supportStatusBarLightMode()) {
                SystemBarCompat.setStatusBarColor(this, ContextCompat.getColor(this, R.color.white))
            } else {
                SystemBarCompat.setStatusBarColor(this, ContextCompat.getColor(this, R.color.colorPrimaryDark))
            }
        }
    }

    protected open fun tintStatusBar() = true

    protected open fun enableStatusBarLightMode() = true

}