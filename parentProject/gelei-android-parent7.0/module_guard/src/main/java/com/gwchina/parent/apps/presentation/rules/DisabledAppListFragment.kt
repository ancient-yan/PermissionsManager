package com.gwchina.parent.apps.presentation.rules

import android.arch.lifecycle.Observer
import com.android.base.app.dagger.Injectable
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.common.AppGuardResource
import com.gwchina.parent.apps.common.RULE_TYPE_DISABLE
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.widget.AppGuardMemberDialog
import com.gwchina.parent.apps.widget.showSelectAppsDialog
import com.gwchina.sdk.base.data.api.APP_RULE_TYPE_DISABLE
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import javax.inject.Inject

/**
 * 禁用列表
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-27 15:06
 */
class DisabledAppListFragment : BaseAppListFragment(), Injectable {

    @Inject lateinit var appGuardResource: AppGuardResource

    override fun hasElseAppCanBeSet(): Boolean {
        return if (appRulesViewModel.isRestricted()) {
            false
        } else {
            appRulesViewModel.hasFreeAppWithoutSystemApp || appRulesViewModel.hasLimitedApp
        }
    }

    override fun selectLiveData(appRulesViewModel: AppRulesViewModel) = appRulesViewModel.disabledList

    override fun provideRuleTypeInfo(): RuleTypeInfo {
        val typeName = appGuardResource.getRuleTypeName(APP_RULE_TYPE_DISABLE)
        return RuleTypeInfo(typeName, getString(R.string.no_apps_tips_mask, typeName), R.drawable.app_img_no_disabled)
    }

    override fun subscribeViewModel() {
        super.subscribeViewModel()
        appRulesViewModel.limitedList.observe(this, Observer {
            appListAdapter.canAddMore = hasElseAppCanBeSet()
            appListAdapter.notifyDataSetChanged()
        })
    }

    override fun onAddApp() {
        val appListCanBeDisable = appRulesViewModel.appListCanBeDisable()
        if (appListCanBeDisable.isEmpty()) {
            showMessage(getString(R.string.no_more_apps_can_be_set_tips_mask, getString(R.string.disabled)))
            return
        }
        if (!hasElseAppCanBeSet()) {
            AppGuardMemberDialog.showTips(requireContext(), getString(R.string.app_guard_limit_tips2, appRulesViewModel.maxCount), R.string.i_got_it)
            return
        }
        showSelectAppsDialog(appListCanBeDisable, getString(R.string.add_as_x_mask, getString(R.string.disabled)),
                onSelectedAppList = {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_PROHIBIT_BTN_ADD)
                    if (it.isNotEmpty()) {
                        processOperationResult(appRulesViewModel.updateAppRuleType(it, RULE_TYPE_DISABLE))
                    }
                },
                onSelectionListener = { app: App, selectMap: HashMap<String, App> ->
                    val allow = appRulesViewModel.isCanAddApp(app, selectMap)
                    if (allow) {
                        showMessage(getString(R.string.app_guard_limit_tips4, appRulesViewModel.maxCount))
                    }
                    allow
                }
        )
    }

}