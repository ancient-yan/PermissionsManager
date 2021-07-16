package com.gwchina.parent.binding

import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.inFragmentTransaction
import com.gwchina.parent.binding.presentation.ChildInfoCollectFragment
import com.gwchina.parent.binding.presentation.ScanChildGuideFragment
import com.gwchina.parent.binding.presentation.ScannerFragment
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-23 11:04
 */
@ActivityScope
class BindingNavigator @Inject constructor(
        private val bindingActivity: BindingActivity,
        private val appRouter: AppRouter
) {

    fun openChildInfoCollectPage() {
        bindingActivity.inFragmentTransaction {
            replaceWithStack(fragment = ChildInfoCollectFragment())
        }
    }

    fun openScanChildGuidePage() {
        bindingActivity.inFragmentTransaction {
            replaceWithStack(fragment = ScanChildGuideFragment())
        }
    }

    fun openScannerPage() {
        bindingActivity.inFragmentTransaction {
            replaceWithStack(fragment = ScannerFragment(), transition = false)
        }
    }

    fun openSupportedDevices() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.SUPPORT_DEVICE_LIST)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

}