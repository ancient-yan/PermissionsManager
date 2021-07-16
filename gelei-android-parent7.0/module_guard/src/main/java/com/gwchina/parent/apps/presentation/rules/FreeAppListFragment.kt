package com.gwchina.parent.apps.presentation.rules

import com.android.base.app.dagger.Injectable
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.common.AppGuardResource
import com.gwchina.parent.apps.common.RULE_TYPE_FREE
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.widget.showSelectAppsDialog
import com.gwchina.sdk.base.data.api.APP_RULE_TYPE_FREE
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import javax.inject.Inject

/**
 * 自由可用列表
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-27 15:07
 */
class FreeAppListFragment : BaseAppListFragment(), Injectable {

    @Inject lateinit var appGuardResource: AppGuardResource

    override fun hasElseAppCanBeSet() = appRulesViewModel.hasDisabledApp || appRulesViewModel.hasLimitedApp

    override fun selectLiveData(appRulesViewModel: AppRulesViewModel) = appRulesViewModel.freeList

    override fun onAddApp() {
        val appListCanBeFree = appRulesViewModel.appListCanBeFree()
        val typeName = appGuardResource.getRuleTypeName(APP_RULE_TYPE_FREE)

        if (appListCanBeFree.isEmpty()) {
            showMessage(getString(R.string.no_more_apps_can_be_set_tips_mask, typeName))
            return
        }
        showSelectAppsDialog(appListCanBeFree, getString(R.string.add_as_x_mask, typeName), onSelectedAppList = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_FREE_BTN_ADD)
            if (it.isNotEmpty()) {
                processOperationResult(appRulesViewModel.updateAppRuleType(it, RULE_TYPE_FREE))
            }
        })
    }

    override fun provideRuleTypeInfo(): RuleTypeInfo {
        val typeName = appGuardResource.getRuleTypeName(APP_RULE_TYPE_FREE)
        return RuleTypeInfo(
                typeName,
                getString(R.string.no_apps_tips_mask, typeName),
                R.drawable.app_img_no_free)
    }

    override fun onAppClicked(app: App) {
        appGuardNavigator.openAppPermissionSettingPage(app, isForbid = appRulesViewModel.isRestricted())
    }

}