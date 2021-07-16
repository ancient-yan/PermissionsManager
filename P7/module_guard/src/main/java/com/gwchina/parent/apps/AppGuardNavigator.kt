package com.gwchina.parent.apps

import android.content.Intent
import android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.clearBackStack
import com.android.base.app.fragment.inFragmentTransaction
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.data.AppGroup
import com.gwchina.parent.apps.presentation.approval.AppApprovalFragment
import com.gwchina.parent.apps.presentation.approval.AppApprovalRecordFragment
import com.gwchina.parent.apps.presentation.group.AddEditGroupFragment
import com.gwchina.parent.apps.presentation.guide.GuideDisabledAppFragment
import com.gwchina.parent.apps.presentation.guide.GuideFreeAppFragment
import com.gwchina.parent.apps.presentation.guide.LimitAppFlowFragment
import com.gwchina.parent.apps.presentation.rules.AppApprovalListFragment
import com.gwchina.parent.apps.presentation.rules.AppGuardRulesFragment
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-28 15:41
 */
@ActivityScope
class AppGuardNavigator @Inject constructor(
        private val appGuardActivity: AppGuardActivity,
        private val appRouter: AppRouter
) {

    fun openGuideFlowSetDisableAppPage() {
        appGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = GuideDisabledAppFragment())
        }
    }

    fun openGuideFlowSetFreeAppPage() {
        appGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = GuideFreeAppFragment())
        }
    }

    fun openGuideFlowSetLimitedAppPage() {
        appGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = LimitAppFlowFragment())
        }
    }

    fun openAddEditAppGroupPage(appGroup: AppGroup? = null) {
        appGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = AddEditGroupFragment.newInstance(appGroup))
        }
    }

    fun openAppApprovalRecordListPage() {
        appGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = AppApprovalRecordFragment())
        }
    }

    /**打开应用守护结果界面*/
    fun openAppRulesPage() {
        val fm = appGuardActivity.supportFragmentManager
        fm.popBackStack(GuideFreeAppFragment::class.qualifiedName, POP_BACK_STACK_INCLUSIVE)
        appGuardActivity.inFragmentTransaction {
            appGuardActivity.supportFragmentManager.clearBackStack()
            replaceWithDefaultContainer(fragment = AppGuardRulesFragment())
        }
    }

    /**重度模式下，需要先设置时间守护，才能进行应用守护设置*/
    fun openTimeGuardGuidePageForAppGuard() {
        appRouter.build(RouterPath.TimesGuard.PATH)
                .withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                .withString(RouterPath.TimesGuard.CHILD_USER_ID_KEY, appGuardActivity.childUserId)
                .withString(RouterPath.TimesGuard.DEVICE_ID_KEY, appGuardActivity.childDeviceId)
                .navigation()
    }

    /**打开权限设置界面*/
    fun openAppPermissionSettingPage(app: App, forAppApproval: Boolean = false, isForbid: Boolean = false) {
        appGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = AppApprovalFragment.newInstance(app, forAppApproval, isForbid))
        }
    }

    /**打开待批准列表*/
    fun openAppApprovalListPage() {
        appGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = AppApprovalListFragment())
        }
    }

    fun openIosSuperviseModePage() {
        appRouter.build(RouterPath.IOSSuperviseMode.PATH).navigation()
    }

}