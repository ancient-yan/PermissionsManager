package com.gwchina.parent.net

import android.os.Bundle
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.inFragmentTransaction
import com.gwchina.parent.net.presentation.home.fragment.NetGuardFragment
import com.gwchina.parent.net.presentation.netManagement.fragment.DeleteUrlListFragment
import com.gwchina.parent.net.presentation.netManagement.fragment.NetWorkManagementFragment
import com.gwchina.parent.net.presentation.record.fragment.NetGuardRecordFragment
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject


@ActivityScope
class NetGuardNavigator @Inject constructor(
        private val activityTime: NetGuardActivity,
        private val appRouter: AppRouter
) {

    fun openNetGuardRecordPage() {
        activityTime.inFragmentTransaction {
            replaceWithStack(fragment = NetGuardRecordFragment())
        }
    }


    fun openNetWorkManagementPage() {
        activityTime.inFragmentTransaction {
            replaceWithStack(fragment = NetWorkManagementFragment())
        }
    }

    fun openNetGuardPage() {
        activityTime.inFragmentTransaction {
            replaceWithDefaultContainer(fragment = NetGuardFragment())
        }
    }

    fun openIosSuperviseModePage() {
        appRouter.build(RouterPath.IOSSuperviseMode.PATH).navigation()
    }

    fun openDelUrlPage(type: String) {
        val deleteUrlListFragment = DeleteUrlListFragment()
        val bundle = Bundle()
        bundle.putString("type", type)
        deleteUrlListFragment.arguments = bundle
        activityTime.inFragmentTransaction {
            replaceWithStack(fragment = deleteUrlListFragment)
        }
    }

    fun openInterceptUrl(url: String) {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, url)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, true)
                .navigation()
    }
}
