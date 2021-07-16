package com.gwchina.parent.apps

import android.os.Bundle
import android.support.v4.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.presentation.approval.AppApprovalFragment
import com.gwchina.parent.apps.presentation.guide.AppsGuardGuidFragment
import com.gwchina.parent.apps.presentation.rules.AppGuardRulesFragment
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.data.models.appPlanHasBeSet
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.router.AppInfo
import com.gwchina.sdk.base.router.RouterPath

/**
 * 应用守护
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-28 15:34
 */
@Route(path = RouterPath.AppsGuard.PATH)
class AppGuardActivity : InjectorAppBaseActivity() {

    @JvmField @Autowired(name = RouterPath.AppsGuard.APP_INFO_KEY)
    var appInfoToApproval: AppInfo? = null

    @JvmField @Autowired(name = RouterPath.AppsGuard.DEVICE_ID_KEY)
    var childDeviceId: String? = null

    @JvmField @Autowired(name = RouterPath.AppsGuard.CHILD_USER_ID_KEY)
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
        val appInfo = this.appInfoToApproval

        val target: Fragment = when {
            //去审批
            appInfo != null -> AppApprovalFragment.newInstance(buildApp(appInfo), true)
            //有计划，进入计划界面
            AppContext.appDataSource().user().findDevice(deviceId).appPlanHasBeSet() -> AppGuardRulesFragment()
            ///没有计划，进入引导界面
            else -> AppsGuardGuidFragment()
        }

        inFragmentTransaction {
            addWithDefaultContainer(target)
        }
    }

    private fun buildApp(appInfo: AppInfo) = App(
            rule_type = appInfo.rule_type,
            rule_id = appInfo.rule_id,
            soft_icon = appInfo.app_icon ?: "",
            soft_name = appInfo.app_name ?: "",
            type_name = appInfo.type_name ?: "",
            used_time_perday = appInfo.used_time_per_day
    )

}