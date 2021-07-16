package com.gwchina.parent.migration

import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.inFragmentTransaction
import com.gwchina.parent.migration.presentation.AddingChildFragment
import com.gwchina.parent.migration.presentation.BelongingDeviceFragment
import com.gwchina.parent.migration.presentation.MigrationChildInfoCollectFragment
import com.gwchina.parent.migration.presentation.MigrationGuideFragment
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.ChildDeviceInfo
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 15:38
 */
@ActivityScope
class MigrationNavigator @Inject constructor(
        private val migrationActivity: MigrationActivity,
        private val appRouter: AppRouter
) {

    fun openMigrationGuidePage() {
        migrationActivity.inFragmentTransaction {
            replaceWithDefaultContainer(MigrationGuideFragment())
        }
    }

    fun openChildInfoCollectingPage() {
        migrationActivity.inFragmentTransaction {
            replaceWithStack(fragment = MigrationChildInfoCollectFragment.newInstance(true))
        }
    }

    fun openAddingChildPage() {
        migrationActivity.inFragmentTransaction {
            replaceWithStack(fragment = AddingChildFragment())
        }
    }

    fun openBelongingDevicePage() {
        migrationActivity.inFragmentTransaction {
            replaceWithStack(fragment = BelongingDeviceFragment())
        }
    }

    fun openGuardLevelForGetLevelInfo(id: String, deviceType: String, childAge: Int) {
        val childDeviceInfo = ChildDeviceInfo(id, childAge, deviceType)
        appRouter.build(RouterPath.GuardLevel.PATH)
                .withInt(RouterPath.PAGE_KEY, RouterPath.GuardLevel.ACTION_CHOOSING_LEVEL)
                .withBoolean(RouterPath.GuardLevel.IS_FROM_MIGRATION,true)
                .withParcelable(RouterPath.GuardLevel.CHILD_DEVICE_INFO_KEY, childDeviceInfo)
                .navigation(migrationActivity, RouterPath.GuardLevel.REQUEST_CODE)
    }

    fun openSupportedDeviceListPage() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.SUPPORT_DEVICE_LIST)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    fun openMainPage() {
        appRouter.build(RouterPath.Main.PATH).navigation()
    }

    fun openIOSDescriptionFileUpdatingGuide() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.IOS_DESC_FILE_UPDATE)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

}