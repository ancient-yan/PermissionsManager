package com.gwchina.parent.times

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.presentation.guide.TimeGuardGuideFragment
import com.gwchina.parent.times.presentation.table.TimeGuardTableFragment
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.data.models.timePlanHasBeSet
import com.gwchina.sdk.base.router.RouterPath

/**
 * 时间守护
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-12-12 15:50
 */
@Route(path = RouterPath.TimesGuard.PATH)
class TimeGuardActivity : InjectorAppBaseActivity() {

    @JvmField @Autowired(name = RouterPath.TimesGuard.DEVICE_ID_KEY)
    var childDeviceId: String? = null

    @JvmField @Autowired(name = RouterPath.TimesGuard.CHILD_USER_ID_KEY)
    var childUserId: String? = null

    override fun layout() = R.layout.app_base_activity

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        if (childDeviceId.isNullOrEmpty() || childUserId.isNullOrEmpty()) {
            val user = AppContext.appDataSource().user()
            childUserId = user.currentChild?.child_user_id ?: ""
            childDeviceId = user.currentDevice?.device_id ?: ""
        }
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        savedInstanceState.ifNull {
            childDeviceId?.let(::showFragment)
        }
    }

    private fun showFragment(deviceId: String) {
        inFragmentTransaction {
            addWithDefaultContainer(
                    if (AppContext.appDataSource().user().findDevice(deviceId).timePlanHasBeSet()) {
                        TimeGuardTableFragment()
                    } else {
                        TimeGuardGuideFragment()
                    }
            )
        }
    }
}