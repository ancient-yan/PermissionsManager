package com.gwchina.parent.times.presentation.spare

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.alwaysShow
import com.android.base.kotlin.onMenuItemClick
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.TimeGuardNavigator
import com.gwchina.parent.times.common.TimeEventCenter
import com.gwchina.parent.times.common.TimeMapper
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.times_fragment_spare_plans.*
import me.drakeet.multitype.register
import javax.inject.Inject

/**
 * - 备用计划列表
 * - 选择备用计划
 * - todo：使用 DiffUtil 优化
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-23 10:45
 */
class SparePlanFragment : InjectorBaseListFragment<Any>() {

    companion object {

        private const val MAX_SPARE_PLAN_SIZE = 10

        private const val SHOW_START_SPARE_PLAN_TIPS = "show_start_spare_plan_tips"

        private const val CHOOSING_MODE_KEY = "choosing_mode"

        fun newInstance(choosingMode: Boolean) = SparePlanFragment().apply {
            arguments = Bundle().apply {
                putBoolean(CHOOSING_MODE_KEY, choosingMode)
            }
        }
    }

    private val adapter by lazy {
        MultiTypeAdapter(requireContext())
    }

    private var isChoosingMode: Boolean = false
    private var addPlanMenuItem: MenuItem? = null

    @Inject lateinit var timeGuardNavigator: TimeGuardNavigator
    @Inject lateinit var eventCenter: TimeEventCenter
    @Inject lateinit var timeMapper: TimeMapper

    private val viewModel by lazy {
        getViewModel<SparePlanViewModel>(viewModelFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isChoosingMode = arguments?.getBoolean(CHOOSING_MODE_KEY, false) ?: false
        viewModel.isChoosingMode = isChoosingMode
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.times_fragment_spare_plans

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        setupTitle()
        setupList()
    }

    private fun setupTitle() {
        if (isChoosingMode) {
            gtlTimesTitle.setTitle(getString(R.string.select_spare_plan))
        } else {
            addPlanMenuItem = gtlTimesTitle.menu.add(R.string.add_plan).alwaysShow()
        }
    }

    private fun setupList() {
        //list state
        stateLayoutConfig
                .disableOperationWhenRequesting(true)
                .setStateIcon(EMPTY, R.drawable.img_page_empty_content)
                .setStateAction(EMPTY, getString(R.string.add_plan))
                .setStateMessage(EMPTY, getText(R.string.empty_spare_plans_tips))
                .setMessageGravity(EMPTY, Gravity.CENTER)

        //rv
        with(adapter) {
            rvTimeSparePlans.adapter = this
            setDataManager(this)
            register(SparePlanItemHeaderViewBinder(isChoosingMode).apply { setupHeaderEventHandler(this) })
            register(SparePlanItemContentViewBinder())
            register(SparePlanItemTipsViewBinder())
            register(SparePlanItemBottomViewBinder().apply { setupBottomEventHandler(this) })
        }

    }

    private fun setupBottomEventHandler(sparePlanItemBottomViewBinder: SparePlanItemBottomViewBinder) {
        sparePlanItemBottomViewBinder.onEditPlan = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_EDITOR)
            adapter.items.asSequence().filterIsInstance<SparePlanHeader>().find { header ->
                it.batchId == header.batchId
            }?.let {
                timeGuardNavigator.openEditingSparePlanPage(timeMapper.toTimeGuardWeeklyPlanVO(it))
            }
        }

        sparePlanItemBottomViewBinder.onStartPlan = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_START)
            if (adapter.items.asSequence().filterIsInstance<SparePlanHeader>().any { header -> header.using }) {
                showMessage("请先停用当前生效的备用计划喔")
            } else {
                viewModel.startPlan(it.batchId)
                if (!AppSettings.settingsStorage().getBoolean(SHOW_START_SPARE_PLAN_TIPS, false)) {
                    showConfirmDialog {
                        messageId = R.string.start_spare_plan_tips
                        checkBoxChecked = true
                        checkBoxId = R.string.do_not_tips_again
                        noNegative()
                        positiveId = R.string.i_got_it
                        positiveListener2 = { _, checked ->
                            AppSettings.settingsStorage().putBoolean(SHOW_START_SPARE_PLAN_TIPS, checked)
                        }
                    }?.setCanceledOnTouchOutside(false)
                }
            }

        }

        sparePlanItemBottomViewBinder.onStopUsingPlan = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_STOP)
            showConfirmDialog {
                messageId = R.string.stop_spare_plan_tips
                positiveText = "保存修改"
                negativeText = "保留生效前计划"
                positiveListener = { _ ->
                    viewModel.stopPlan(it.batchId, true)
                    StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_STOP_RENEW)
                }
                negativeListener = { _ ->
                    viewModel.stopPlan(it.batchId, false)
                    StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_STOP_RETAIN)
                }
            }
        }
    }

    private fun setupHeaderEventHandler(sparePlanItemHeaderViewBinder: SparePlanItemHeaderViewBinder) {
        sparePlanItemHeaderViewBinder.onDeletePlan = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_DELETE)
            showConfirmDialog {
                message = getString(R.string.confirm_to_delete_x_mask, it.batchName)
                positiveListener = { _ ->
                    viewModel.deletePlan(it.batchId)
                }
            }
        }

        sparePlanItemHeaderViewBinder.onPlanSelected = {
            btnTimeConfirm.isEnabled = true
        }

        if (isChoosingMode) {
            btnTimeConfirm.visible()
            btnTimeConfirm.setOnClickListener {
                sparePlanItemHeaderViewBinder.selectedPlanId?.let(viewModel::startPlan)
            }
        }

    }

    override fun onRefresh() = viewModel.loadSparePlans()

    override fun onRetry(state: Int) {
        if (state == EMPTY) {
            if (viewModel.user?.value?.vipRule?.time_backup_plan_enabled == FLAG_POSITIVE_ACTION) {
                toAddSparePlan()
            } else {
                timeGuardNavigator.openMemberCenter()
            }
        } else {
            super.onRetry(state)
        }
    }

    private fun toAddSparePlan() {
        timeGuardNavigator.openAddSparePlanPage(adapter.items.filterIsInstance<SparePlanHeader>().map { it.batchName })
        StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_ADD)
    }

    private fun subscribeViewModel() {
        eventCenter.onSparePlansChanged.observe(this, Observer {
            autoRefresh()
        })

        viewModel.sparePlans.observe(this, Observer {
            it?.onError { err ->
                addPlanMenuItem?.isEnabled = false
                processErrorWithStatus(err)
            }?.onSuccess { info ->
                processListResultWithStatus(info)
                checkCanAddingMore()
            }?.onLoading {
                addPlanMenuItem?.isEnabled = false
                showLoadingIfEmpty()
            }
        })

        viewModel.operationStatus.observe(this, Observer {
            it?.onError { err ->
                showContentLayout()
                errorHandler.handleError(err)
            }?.onSuccess { info ->
                showContentLayout()
                info?.let(::processOperatingSuccessfully)
            }?.onLoading {
                showRequesting()
            }
        })

        viewModel.user.observe(this, Observer {
            val sparePanEnable = it?.vipRule?.time_backup_plan_enabled == FLAG_POSITIVE_ACTION
            val btnText = if (sparePanEnable) getString(R.string.add_plan) else getString(R.string.open_vip_to_experience_mask, it?.vipRule?.time_backup_plan_enabled_minimum_level)
            stateLayoutConfig.setStateAction(EMPTY, btnText)
            addPlanMenuItem?.isVisible = sparePanEnable
            if (sparePanEnable) {
                //备用计划可用的时候才加载数据，否则直接显示提示开通会员页面
                viewModel.loadSparePlans()
            } else {
                showEmptyLayout()
            }
        })
    }

    private fun processOperatingSuccessfully(info: OperationInfo) {
        if (isChoosingMode && info.type == OperationInfo.START_PLAN) {
            timeGuardNavigator.openGuardTimePlanTablePage()
            return
        }

        if (info.type == OperationInfo.STOP_AND_UPDATE) {
            showMessage("计划已更新，计划已停用")
        } else if (info.type == OperationInfo.STOP_AND_NO_UPDATE) {
            showMessage("计划已恢复，计划已停用")
        }
        autoRefresh()
    }

    private fun checkCanAddingMore() {
        if (!isChoosingMode) {

            addPlanMenuItem?.isEnabled = true

            val plansCount = adapter.items.filterIsInstance<SparePlanHeader>().size

            if (plansCount < MAX_SPARE_PLAN_SIZE) {
                addPlanMenuItem
                        ?.setIcon(R.drawable.icon_add)
                        ?.onMenuItemClick { toAddSparePlan() }
            } else {
                addPlanMenuItem
                        ?.setIcon(R.drawable.icon_add_disable)
                        ?.onMenuItemClick { showMessage("最多添加十个备用计划喔") }
            }
        }
    }

}