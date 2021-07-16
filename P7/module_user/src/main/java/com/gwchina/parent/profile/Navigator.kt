package com.gwchina.parent.profile

import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.findFragmentByTag
import com.android.base.app.fragment.inFragmentTransaction
import com.gwchina.parent.profile.data.DeliveryAddress
import com.gwchina.parent.profile.presentation.patriarch.PatriarchAddressFragment
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-26 20:46
 */
@ActivityScope
@Suppress("UNUSED")
class Navigator @Inject constructor(private val activity: ProfileActivity, private val appRouter: AppRouter) {

    fun openAddressPage(mDeliveryAddress: DeliveryAddress?) {
        if (activity.supportFragmentManager.findFragmentByTag(PatriarchAddressFragment::class) != null) {
            return
        }
        activity.inFragmentTransaction {
            addWithStack(fragment = PatriarchAddressFragment.createFragment(mDeliveryAddress))
        }
    }

    fun openAddDevicePage(childUserId: String) {
        appRouter.build(RouterPath.Binding.PATH)
                .apply {
                    withBoolean(RouterPath.Binding.SELECT_CHILD_KEY, false)
                    withString(RouterPath.Binding.CHILD_USER_ID_KEY, childUserId)
                }
                .navigation(activity, RouterPath.Binding.REQUEST_CODE)
    }

    fun openChildDevicePermissionInfo() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.CHILD_DEVICE_PERMISSION_INFO)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }
}