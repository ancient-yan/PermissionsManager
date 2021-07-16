package com.gwchina.sdk.base.web.actions

import com.android.base.app.fragment.BaseFragment
import com.gwchina.sdk.base.upgrade.AppUpgradeObviousChecker

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-12 16:46
 */
class UpgradeAppAction(fragment: BaseFragment) {

    private val appUpgradeChecker = AppUpgradeObviousChecker(fragment.requireActivity(), fragment)

    fun run() {
        appUpgradeChecker.checkAppUpgrade()
    }

}