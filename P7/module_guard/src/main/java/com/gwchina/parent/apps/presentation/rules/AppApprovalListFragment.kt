package com.gwchina.parent.apps.presentation.rules

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.dagger.Injectable
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.onMenuItemClick
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardNavigator
import com.gwchina.parent.apps.common.AppEventCenter
import com.gwchina.parent.apps.common.RULE_TYPE_DISABLE
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.widget.AppGuardMemberDialog
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import kotlinx.android.synthetic.main.apps_fragment_approval_list.*
import me.drakeet.multitype.register
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-01-02 15:53
 */
class AppApprovalListFragment : InjectorBaseStateFragment(), Injectable {

    @Inject lateinit var appEventCenter: AppEventCenter
    @Inject lateinit var appGuardNavigator: AppGuardNavigator

    private lateinit var showApprovalRecordsMenu: MenuItem

    private val appRulesViewModel by lazy {
        getViewModelFromActivity<AppRulesViewModel>(viewModelFactory)
    }

    private val appApprovalAdapter by lazy {
        MultiTypeAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    private fun processAppApprovalEvent(app: App) {
        appApprovalAdapter.items.find { item ->
            app.rule_id == (item as App).rule_id
        }?.let {
            appApprovalAdapter.remove(it)
            if (appApprovalAdapter.isEmpty) {
                showEmptyLayout()
            }
        }
    }

    override fun provideLayout() = R.layout.apps_fragment_approval_list

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        //empty config
        stateLayoutConfig
                .setStateAction(EMPTY, "")
                .setStateMessage(EMPTY, getString(R.string.no_pending_approval_tips))

        //approval record
        showApprovalRecordsMenu = gtlAppsRulesTitle.menu.add(R.string.approval_record)
                .onMenuItemClick {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_AUDITING_REPORT)
                    appGuardNavigator.openAppApprovalRecordListPage()
                }
        showApprovalRecordsMenu.isVisible = false
        showApprovalRecordsMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        //rv
        rvAppsApprovalList.layoutManager = LinearLayoutManager(context)
        rvAppsApprovalList.adapter = appApprovalAdapter
        val approvalItemViewBinder = AppApprovalItemViewBinder(this)
        appApprovalAdapter.register(approvalItemViewBinder)
        approvalItemViewBinder.onAllowListener = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_AUDITING_BTN_ALLOW)
            appGuardNavigator.openAppPermissionSettingPage(it, true, isForbid = appRulesViewModel.isRestricted())
        }
        approvalItemViewBinder.onProhibitListener = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_AUDITING_BTN_PROHIBIT)
            if (appRulesViewModel.isRestricted()) {
                AppGuardMemberDialog.showTips(requireContext(), getString(R.string.app_guard_limit_tips2, appRulesViewModel.maxCount), R.string.i_got_it)
            } else {
                doProhibitApp(it)
            }
        }
    }

    private fun doProhibitApp(app: App) {
        appRulesViewModel.updateAppRuleType(listOf(app), RULE_TYPE_DISABLE, true)
                .observe(this, Observer {
                    it?.onSuccess {
                        dismissLoadingDialog()
                        processAppApprovalEvent(app.copy(rule_type = RULE_TYPE_DISABLE))
                        showMessage(getString(R.string.x_add_to_y_list_mask, app.soft_name, getString(R.string.disabled)))
                        appEventCenter.notifyAppListNeedRefresh()
                    }?.onLoading {
                        showLoadingDialog(false)
                    }?.onError { err ->
                        dismissLoadingDialog()
                        errorHandler.handleError(err)
                    }
                })
    }

    private fun subscribeViewModel() {
        appEventCenter.appApprovedEvent()
                .observe(this, Observer {
                    it?.let(::processAppApprovalEvent)
                })

        appRulesViewModel.pendingApprovalList.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                showEmptyLayout()
            } else {
                appApprovalAdapter.replaceAll(it)
                showContentLayout()
            }
        })

        appRulesViewModel.hasApprovalRecords.observe(this, Observer {
            showApprovalRecordsMenu.isVisible = it ?: false
        })
    }

}