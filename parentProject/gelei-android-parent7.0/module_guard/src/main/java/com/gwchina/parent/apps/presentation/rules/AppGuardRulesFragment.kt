package com.gwchina.parent.apps.presentation.rules

import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.constraint.ConstraintSet
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.android.base.adapter.pager.ViewPageFragmentAdapter
import com.android.base.adapter.pager.ViewPageInfo
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.RefreshStateLayout
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.interfaces.adapter.OnPageChangeListenerAdapter
import com.android.base.kotlin.gone
import com.android.base.kotlin.visibleOrGone
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardNavigator
import com.gwchina.parent.apps.common.AppEventCenter
import com.gwchina.parent.apps.common.AppGuardResource
import com.gwchina.parent.apps.common.RULE_TYPE_FREE
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.data.api.INSTRUCTION_SYNC_APP
import com.gwchina.sdk.base.sync.SyncManager
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.views.InstructionStateView
import kotlinx.android.synthetic.main.apps_fragment_app_rules.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-26 18:34
 */
class AppGuardRulesFragment : InjectorBaseStateFragment() {

    @Inject
    lateinit var appEventCenter: AppEventCenter
    @Inject
    lateinit var appGuardNavigator: AppGuardNavigator
    @Inject
    lateinit var appGuardResource: AppGuardResource

    private val viewModel by lazy {
        getViewModelFromActivity<AppRulesViewModel>(viewModelFactory)
    }

    private lateinit var pendingApprovalMenu: PendingApprovalItemProvider

    private lateinit var tabViews: Array<TextView>

    private var _previousTableStatus: TableStatus? = null

    private lateinit var deviceName: String

    private lateinit var mIsvApp: InstructionStateView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.apps_fragment_app_rules

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        mIsvApp = isvApp
        //title
        gtlAppsRulesTitle.toolbar.inflateMenu(R.menu.apps_menu_pending_approval)
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_SOFTVIEW)
        //待审批
        pendingApprovalMenu = PendingApprovalItemProvider.findSelf(gtlAppsRulesTitle.menu).apply {
            setOnMenuClickListener {
                StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_AUDITING)
                appGuardNavigator.openAppApprovalListPage()
            }
        }

        //table Indicator
        tabViews = arrayOf(tvAppsTabLimited, tvAppsTabDisable, tvAppsTabFree, tvAppsTabRisk)
        tabViews.forEachIndexed { index, _view ->
            _view.setOnClickListener {
                vpAppsRulesContent?.currentItem = index
            }
        }

        vpAppsRulesContent.addOnPageChangeListener(object : OnPageChangeListenerAdapter {
            override fun onPageSelected(position: Int) {
                updateTab(position)
            }
        })

        setupSync()
    }

    private fun setupSync() {
        val childDeviceId = viewModel.appDataSource.user().currentDevice!!.device_id
        deviceName = viewModel.appDataSource.user().currentDevice?.device_name ?: ""
        val childId = viewModel.appDataSource.user().currentChild?.child_user_id!!
        SyncManager.getInstance().getLocalBroadcastManager(requireContext()).registerReceiver(localReceiver, IntentFilter(SyncManager.getInstance().getIntentFilterAction(INSTRUCTION_SYNC_APP, childDeviceId)))
        SyncManager.getInstance().querySyncState(requireContext(), INSTRUCTION_SYNC_APP, childId, childDeviceId) { isSuccess ->
            if (isSuccess) {
                isvApp.gone()
            } else {
                isvApp.showInitFailedState(getString(R.string.instruction_app_name))
            }
        }

        isvApp.onRetryClickListener = {
            SyncManager.getInstance().sendSync(childDeviceId, INSTRUCTION_SYNC_APP, childId)
        }
    }

    override fun onRefresh() {
        viewModel.refreshList()
    }

    private fun setupTable(tableStatus: TableStatus) {
        val previousTableStatus = _previousTableStatus
        if (previousTableStatus == null) {
            if (appGuardResource.isAndroidDevice) {
                showAndroidTable(tableStatus.showedRisk || tableStatus.hasRiskApp)
            } else {
                showIOSTable()
            }
            _previousTableStatus = tableStatus
        } else if (appGuardResource.isAndroidDevice && !(previousTableStatus.showedRisk || previousTableStatus.hasRiskApp) && (tableStatus.showedRisk || tableStatus.hasRiskApp)) {
            showAndroidTable(true)
            _previousTableStatus = tableStatus
        }
    }

    private fun showAndroidTable(showRisk: Boolean) {
        tvAppsTabRisk.visibleOrGone(showRisk)
        tvAppsTabFree.text = appGuardResource.getRuleTypeName(RULE_TYPE_FREE)

        val viewPageFragmentAdapter = ViewPageFragmentAdapter(childFragmentManager, context)

        with(mutableListOf<ViewPageInfo>()) {
            add(ViewPageInfo("", LimitedAppListFragment::class.java, null))
            add(ViewPageInfo("", DisabledAppListFragment::class.java, null))
            add(ViewPageInfo("", FreeAppListFragment::class.java, null))
            if (showRisk) {
                add(ViewPageInfo("", RiskAppListFragment::class.java, null))
            }
            viewPageFragmentAdapter.setDataSource(this)
        }

        vpAppsRulesContent.adapter = viewPageFragmentAdapter

        updateTab(vpAppsRulesContent.currentItem)
    }

    private fun showIOSTable() {
        tvAppsTabRisk.gone()
        tvAppsTabFree.text = getString(R.string.free_usable)

        val viewPageFragmentAdapter = ViewPageFragmentAdapter(childFragmentManager, context)

        viewPageFragmentAdapter.setDataSource(
                listOf(
                        ViewPageInfo("", LimitedAppListFragment::class.java, null),
                        ViewPageInfo("", DisabledAppListFragment::class.java, null),
                        ViewPageInfo("", FreeAppListFragment::class.java, null)
                )
        )

        vpAppsRulesContent.adapter = viewPageFragmentAdapter
        updateTab(vpAppsRulesContent.currentItem)
    }

    private fun updateTab(position: Int) {
        tabViews.forEachIndexed { index, textView ->
            if (index == position) {
                textView.paint.isFakeBoldText = true
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_level1))
            } else {
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_level2))
                textView.paint.isFakeBoldText = false
            }
            textView.invalidate()
        }

        val constraintSet = ConstraintSet()
        constraintSet.clone(clAppsRulesTab)
        constraintSet.connect(viewAppsTabIndicator.id, ConstraintSet.LEFT, tabViews[position].id, ConstraintSet.LEFT)
        constraintSet.connect(viewAppsTabIndicator.id, ConstraintSet.RIGHT, tabViews[position].id, ConstraintSet.RIGHT)
        constraintSet.applyTo(clAppsRulesTab)
    }

    private fun notifyChildPageRefresh() {
        childFragmentManager.fragments.forEach {
            if (it != null && it.isVisible && it is RefreshStateLayout/*子界面都是 RefreshStateLayout 实现者*/) {
                it.autoRefresh()
            }
        }
    }

    private fun subscribeViewModel() {
        appEventCenter.appListRefreshEvent()
                .observe(this, Observer {
                    view?.post { notifyChildPageRefresh() }
                })

        viewModel.tableStatus.observe(this, Observer {
            it?.let(::setupTable)
        })

        viewModel.hasPendingApprovalList
                .observe(this, Observer {
                    pendingApprovalMenu.showDot = it == true
                })

        viewModel.loadingAppRulesStatus
                .observe(this, Observer {
                    it?.onLoading {
                        if (!viewModel.hasLoadedData) {
                            showLoadingLayout()
                        }
                    }?.onError { err ->
                        if (!viewModel.hasLoadedData) {
                            processErrorWithStatus(err)
                        }
                    }?.onSuccess {
                        showContentLayout()
                    }
                })

    }

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceId = intent?.getStringExtra(SyncManager.DEVICE_ID)
            val action = SyncManager.getInstance().intervalHandlerMap["$deviceId-$INSTRUCTION_SYNC_APP"]?.childDeviceId
            if (action != null && ("$action-$INSTRUCTION_SYNC_APP") == intent?.action) {
                when (intent.getIntExtra(SyncManager.SYNC_STATE, 0)) {
                    1 -> mIsvApp.showSyncingState(getString(R.string.instruction_app_name))
                    2 -> mIsvApp.showSyncFailedState(getString(R.string.instruction_app_name), deviceName)
                    3 -> mIsvApp.gone()
                }
            }
        }
    }

    override fun onDestroy() {
        SyncManager.getInstance().getLocalBroadcastManager(requireContext()).unregisterReceiver(localReceiver)
        super.onDestroy()
    }

}