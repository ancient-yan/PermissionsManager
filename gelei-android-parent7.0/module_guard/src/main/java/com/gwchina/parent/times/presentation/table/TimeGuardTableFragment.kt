package com.gwchina.parent.times.presentation.table

import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.*
import com.android.base.utils.android.TintUtils
import com.android.base.utils.android.compat.SystemBarCompat
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.TimeGuardNavigator
import com.gwchina.parent.times.common.*
import com.gwchina.parent.times.widget.SelectWeekDaysDialog
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.api.INSTRUCTION_SYNC_TIME
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.sync.SyncManager
import com.gwchina.sdk.base.sync.SyncManager.Companion.DEVICE_ID
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.formatSecondsToTimeText
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.gwchina.sdk.base.widget.views.InstructionStateView
import com.zyyoona7.popup.EasyPopup
import com.zyyoona7.popup.XGravity
import com.zyyoona7.popup.YGravity
import kotlinx.android.synthetic.main.times_fragment_time_table.*
import kotlinx.android.synthetic.main.times_layout_no_plan.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-12 16:27
 */
class TimeGuardTableFragment : InjectorBaseStateFragment() {

    companion object {
        private const val DO_NOT_SHOW_SPARE_PLAN_TIPS = "do_not_show_spare_plan_tips"
        private const val USING_SPARE_TIPS_SHOWED = "using_spare_tips_showed"
    }

    private val viewModel by lazy {
        getViewModel<TimeGuardTableViewModel>(viewModelFactory)
    }

    private val weekAdapter by lazy {
        WeekAdapter(requireContext())
    }

    private var showSparePlanTipsWhenSaving = false
    private var selectedPlanOperator: TimeGuardDailyPlanVO? = null
    private lateinit var sparePlanMenuItem: MenuItem
    private var actionWhenSavedSuccessfully: (() -> Unit)? = null

    private var isChangeUsableDuration = false  //是否修改了可用时长
    private var isChangeTimesSegment = false    //是否修改了可用时段

    @Inject
    lateinit var timeGuardNavigator: TimeGuardNavigator
    @Inject
    lateinit var eventCenter: TimeEventCenter

    private var isConfirmTimeDuration = false //是否点击了可用时长确认按钮

    private lateinit var deviceName: String

    private lateinit var mIsvTimes: InstructionStateView
    private lateinit var mtvUsable: TextView
    private lateinit var mtvDisUsable: TextView
    //当前计划是否是备用计划
    private var isSpare: Boolean? = null

    private val disableMoreIcon by lazy {
        TintUtils.tint(drawableFromId(R.drawable.icon_menu_more)?.mutate(), colorFromId(R.color.gray_disable))
    }

    private val plansManager by lazy {
        PlansManager(viewModel.childDeviceIsAndroid)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_TIMEVIEW)
    }

    override fun provideLayout() = R.layout.times_fragment_time_table

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        setupTitle()
        setupWeekList()
        setupPlanViews()
        showUsingSparePlanTipsIfNeed()
        mIsvTimes = isvTimes
        mtvUsable = view.findViewById(R.id.tvUsable)
        mtvDisUsable = view.findViewById(R.id.tvDisUsable)

        val childDeviceId = viewModel.appDataSource.user().currentDevice!!.device_id
        deviceName = viewModel.appDataSource.user().currentDevice?.device_name ?: ""
        val childId = viewModel.appDataSource.user().currentChild?.child_user_id!!
        SyncManager.getInstance().getLocalBroadcastManager(requireContext()).registerReceiver(localReceiver, IntentFilter(SyncManager.getInstance().getIntentFilterAction(INSTRUCTION_SYNC_TIME, childDeviceId)))
        SyncManager.getInstance().querySyncState(requireContext(), INSTRUCTION_SYNC_TIME, childId, childDeviceId) { isSuccess ->
            if (isSuccess) {
                mIsvTimes.gone()
            } else {
                mIsvTimes.showInitFailedState(getString(R.string.instruction_time_name))
            }
        }

        isvTimes.onRetryClickListener = {
            SyncManager.getInstance().sendSync(childDeviceId, INSTRUCTION_SYNC_TIME, childId)
        }
    }

    //            SyncState.SYNCING -> 1
//            SyncState.SYNC_FAILED -> 2
//            SyncState.SYNC_SUCCESS -> 3
    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceId = intent?.getStringExtra(DEVICE_ID)
            val action = SyncManager.getInstance().intervalHandlerMap["$deviceId-$INSTRUCTION_SYNC_TIME"]?.childDeviceId
            if (action != null && ("$action-$INSTRUCTION_SYNC_TIME") == intent?.action) {
                when (intent.getIntExtra(SyncManager.SYNC_STATE, 0)) {
                    1 -> mIsvTimes.showSyncingState(getString(R.string.instruction_time_name))
                    2 -> mIsvTimes.showSyncFailedState(getString(R.string.instruction_time_name), deviceName)
                    3 -> mIsvTimes.gone()
                }
            }
        }
    }

    override fun onDestroy() {
        SyncManager.getInstance().getLocalBroadcastManager(requireContext()).unregisterReceiver(localReceiver)
        super.onDestroy()
    }

    private fun showUsingSparePlanTipsIfNeed() {
        if (AppSettings.settingsStorage().getBoolean(USING_SPARE_TIPS_SHOWED, false)) {
            return
        }

        (llTimesSparePlanTips.layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = SystemBarCompat.getActionBarHeight(requireActivity()) - dip(10)
        llTimesSparePlanTips.visible()
        llTimesSparePlanTips.setOnClickListener {
            dismissUsingSpareTips()
        }
    }

    private fun setupTitle() {
        tvTimesTotalTimeWeekly.text = formatSecondsToTimeText(0)

        gtlTimesTimeTable.setOnNavigationOnClickListener {
            activity?.onBackPressed()
        }

        sparePlanMenuItem = gtlTimesTimeTable.menu.findItem(R.id.menuTimesSparePlan)
                .onMenuItemClick {
                    dismissUsingSpareTips()
                    timeGuardNavigator.openSparePlanListPage()
                    StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN)
                }

        ivTimesMore.setOnClickListener { showOperationMenu(it) }

    }

    private fun dismissUsingSpareTips() {
        llTimesSparePlanTips.gone()
        AppSettings.settingsStorage().putBoolean(USING_SPARE_TIPS_SHOWED, true)
    }

    private fun showOperationMenu(anchor: View) {
        EasyPopup.create()
                .setContentView(context, R.layout.times_popup_more_menu)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setOnViewListener { contentView, popup ->
                    contentView.setOnClickListener {
                        popup.dismiss()
                        selectDaysToDelete()
                        StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_DETELEPLAN)
                    }
                }
                .apply()
                .showAtAnchorView(anchor, YGravity.BELOW, XGravity.LEFT, dip(20), dip(2))
    }

    private fun setupWeekList() {
        rvTimesWeek.addItemDecoration(MarginDecoration(dip(6)))
        rvTimesWeek.adapter = weekAdapter
        weekAdapter.onSelectedDayChanged = { showGuardPlan(it) }
    }

    private fun setupPlanViews() {
        clTimesUsableDuration.visibleOrGone(viewModel.childDeviceIsAndroid)

        tsvTimesSegmentView.onTimePeriodChangedListener = {
            selectedPlanOperator?.run {
                timePeriodList.clear()
                timePeriodList.addAll(it.getData())
            }
            onPlanDataChanged()
            isChangeTimesSegment = true
        }

        clTimesUsableDuration.setOnClickListener {
            if (viewModel.user.value?.vipRule?.time_defend_available_time_enabled == FLAG_POSITIVE_ACTION) {
                selectedPlanOperator?.let(::selectUsableDuration)
            } else {
                timeGuardNavigator.openMemberCenter()
            }
        }

        clTimesCopyPlans.setOnClickListener {
            selectedPlanOperator?.let(::selectDayToCopy)
        }

        btnTimesSave.setOnClickListener {
            doSaveChecked()
            if (isChangeTimesSegment) {
                StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_CHANGESHIDUAN)
            }
            if (isChangeUsableDuration) {
                StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_CHANGESHICHANG)
            }
        }
    }

    private fun showGuardPlan(selectedDay: Int) {
        plansManager.get(selectedDay).ifNonNull {
            showPlanOfSelectedDay(this)
            tvTimesUsablePeriod.visible()
            mtvUsable.visible()
            mtvDisUsable.visible()
            if (isSpare==true){
                tvTimesPlanLabel.visible()
            }
        } otherwise {
            showEmptyPlan(selectedDay)
            tvTimesUsablePeriod.gone()
            mtvUsable.gone()
            mtvDisUsable.gone()
            tvTimesPlanLabel.gone()
        }
    }

    private fun showPlanOfSelectedDay(plan: TimeGuardDailyPlanVO) {
        selectedPlanOperator = plan
        //展示布局
        clTimesContent.visible()
        llTimeNoPlanLayout?.gone()
        btnTimesSave.visibleOrGone(btnTimesSave.isEnabled)
        //可用时段
        tsvTimesSegmentView.setData(plan.timePeriodList)
        //可用时长
        tvTimesUsableDurationTitle.text = getString(R.string.usable_duration_of_day_mask, getSelectDayName(plan.dayOfWeek))
        setTimesUsableDuration(viewModel.user.value, plan)
        //复制计划
        tvTimesCopyPlans.text = getString(R.string.copy_x_plan_to_mask).format(getSelectDayName(plan.dayOfWeek))
    }

    private fun getSelectDayName(dayOfWeek: Int) = checkDayNameIfToday(weekAdapter.getItem(dayOfWeek - 1))

    private fun selectUsableDuration(plan: TimeGuardDailyPlanVO) {
        showSelectingUsableDurationDialogForTimePlan(requireContext(), plan.enabledTime, plan.timePeriodList) {
            isConfirmTimeDuration = true
            plan.enabledTime = it
            tvTimesUsableDuration.text = formatSecondsToTimeText(it)
            onPlanDataChanged()
            isChangeUsableDuration = true
        }
    }

    //修改时间计划 && 未设置可用时长的情况下联动可用时段
    private fun setTimeDuration() {
        if (!isConfirmTimeDuration && tvTimesUsableDuration.text != getString(R.string.click_to_set)) {
            selectedPlanOperator?.enabledTime = calculateUsablePeriodTotalSeconds(selectedPlanOperator?.timePeriodList)
            tvTimesUsableDuration.text = formatSecondsToTimeText(calculateUsablePeriodTotalSeconds(selectedPlanOperator?.timePeriodList))
        }
    }

    private fun selectDaysToDelete() {
        SelectWeekDaysDialog(requireContext()) {
            titleRes = R.string.select_guard_day_to_delete_title
            limitedDayList = plansManager.daysNotPlaned
            //listener
            onSelectedListener = {
                showMultiDeviceUsingSpareTime {
                    viewModel.deletePlans(it)
                }
            }
        }.show()
    }

    private fun selectDayToCopy(plan: TimeGuardDailyPlanVO) {
        SelectWeekDaysDialog(requireContext()) {
            title = getString(R.string.copy_x_day_plan_to_mask, getDayOfWeekName(plan.dayOfWeek))
            hiddenDayList = listOf(plan.dayOfWeek)
            tips = getString(R.string.guard_day_rule_copy_tips)
            //listener
            onSelectedListener = {
                StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_COPYPLAN)
                showMultiDeviceUsingSpareTime {
                    viewModel.copyPlans(plan.id, it)
                }
            }
        }.show()
    }

    private fun onPlanDataChanged() {
        btnTimesSave.isEnabled = true
        btnTimesSave.visible()
        tvTimesTotalTimeWeekly.text = formatSecondsToTimeText(plansManager.calculateTotalUsableDuration())
//        setTimeDuration()
    }

    private fun showEmptyPlan(selectedDay: Int) {
        selectedPlanOperator = null
        clTimesContent.gone()
        if (llTimeNoPlanLayout == null) {
            ignoreCrash { stubTimeNoPlanLayout.inflate() }
        }
        llTimeNoPlanLayout?.visible()
        tvTimesAddPlan?.setOnClickListener {
            timeGuardNavigator.openSetGuardTimePlanPage(plansManager.daysPlaned, listOf(selectedDay))
        }
    }

    override fun onRefresh() {
        viewModel.loadTimeGuardPlans()
    }

    private fun setMoreMenuEnable(enable: Boolean) {
        ivTimesMore.isEnabled = enable
        if (enable) {
            ivTimesMore.setImageResource(R.drawable.icon_menu_more)
        } else {
            ivTimesMore.setImageDrawable(disableMoreIcon)
        }
    }

    private fun setTimesUsableDuration(user: User?, planOperator: TimeGuardDailyPlanVO?) {
        if(user?.vipRule?.time_defend_available_time_enabled == FLAG_POSITIVE_ACTION) {
            planOperator.ifNonNull {
                tvTimesUsableDuration.text = formatSecondsToTimeText(enabledTime)
            }
        } else {
            tvTimesUsableDuration.text = getString(R.string.open_vip_to_experience_mask, user?.vipRule?.time_defend_available_time_enabled_minimum_level)
        }
    }

    private fun subscribeViewModel() {
        /*时间表有更新*/
        eventCenter.onPlansChanged.observe(this, Observer {
            plansManager.clearPlan()
            viewModel.loadTimeGuardPlans()
        })

        viewModel.user.observe(this, Observer {
            setTimesUsableDuration(it, selectedPlanOperator)
        })

        //更新状态
        viewModel.updateTimeGuardPlan.observe(this, Observer { resource ->
            resource ?: return@Observer
            resource.onSuccess {
                dismissLoadingDialog()
                showMessage(R.string.time_plan_already_updated)
                btnTimesSave.isEnabled = false
                btnTimesSave.gone()
                actionWhenSavedSuccessfully?.invoke()
                actionWhenSavedSuccessfully = null
            }.onError {
                dismissLoadingDialog()
                errorHandler.handleError(it)
                actionWhenSavedSuccessfully = null
            }.onLoading {
                showLoadingDialog(false)
            }
        })

        //时间规则数据
        viewModel.timeGuardRuses.observe(this, Observer { resource ->
            resource ?: return@Observer
            resource.onSuccess {

                it.ifNonNull {
                    isSpare = isSparePlan
                    plansManager.setPlans(dailyPlans)
                    if (isSparePlan) {
                        ivTimesMore.gone()
                        showSparePlanTipsWhenSaving = isMultiDeviceUsing
                        tvTimesPlanLabel.visible()
                        tvTimesPlanLabel.text = planName
                    } else {
                        showSparePlanTipsWhenSaving = false
                        tvTimesPlanLabel.gone()
                        ivTimesMore.visible()
                        setMoreMenuEnable(!dailyPlans.isNullOrEmpty())
                    }
                } otherwise {
                    setMoreMenuEnable(false)
                }

                tvTimesTotalTimeWeekly.text = formatSecondsToTimeText(plansManager.calculateTotalUsableDuration())
                showGuardPlan(weekAdapter.selectedDay)
                btnTimesSave.isEnabled = false
                btnTimesSave.gone()
                sparePlanMenuItem.isVisible = true
                showContentLayout()
                refreshCompleted()

            }.onError {
                sparePlanMenuItem.isVisible = false
                processErrorWithStatus(it)
            }.onLoading {
                sparePlanMenuItem.isVisible = false
                if (!plansManager.hasPlan()) {
                    showLoadingLayout()
                }
            }
        })

        //删除计划
        viewModel.deletePlans.observe(this, Observer {
            it?.onSuccess {
                dismissLoadingDialog()
                showMessage(R.string.plan_already_deleted)
            }?.onError { error ->
                dismissLoadingDialog()
                errorHandler.handleError(error)
            }?.onLoading {
                showLoadingDialog(false)
            }
        })

        //复制计划
        viewModel.copyPlans.observe(this, Observer {
            it?.onSuccess {
                dismissLoadingDialog()
                showMessage(R.string.time_plan_already_updated)
            }?.onError { error ->
                dismissLoadingDialog()
                errorHandler.handleError(error)
            }?.onLoading {
                showLoadingDialog(false)
            }
        })

        viewModel.deviceFlag.observe(this, Observer {
            tvTimesDeviceFlag.visibleOrGone(!it.isNullOrEmpty())
            tvTimesDeviceFlag.text = it
        })

    }

    override fun handleBackPress(): Boolean {
        return if (btnTimesSave.isEnabled) {
            askSaveModifiedBeforeExit()
            true
        } else {
            super.handleBackPress()
        }
    }

    private fun askSaveModifiedBeforeExit() {
        showConfirmDialog {
            messageId = R.string.save_modified_time_plans_tips
            negativeId = R.string.exit_directly
            positiveId = R.string.save_and_exit
            negativeListener = { exitFragment() }
            positiveListener = {
                doSaveChecked(true)
            }
        }
    }

    private fun doSaveChecked(exitWhenSaved: Boolean = false) {
        showMultiDeviceUsingSpareTime {
            val timeGuardRules = plansManager.plansCanBeSaved()
            setupActionWhenSaved(exitWhenSaved)
            removeTimePeriodWithType(timeGuardRules)
            viewModel.updateTimeGuardPlans(timeGuardRules)
        }
    }

    /**
     * 移除掉TimePeriod type为1的
     */
    private fun removeTimePeriodWithType(plan: List<TimeGuardDailyPlanVO>) {
        plan.forEach {
            it.timePeriodList.remove(
                    it.timePeriodList.find { timePeriod -> timePeriod.type == 1 }
            )
        }
    }

    private fun showMultiDeviceUsingSpareTime(onConfirm: () -> Unit) {
        if (showSparePlanTipsWhenSaving && !AppSettings.settingsStorage().getBoolean(DO_NOT_SHOW_SPARE_PLAN_TIPS, false)) {
            showConfirmDialog {
                noNegative()
                message = "修改仅针对当前手机，不影响其他启用同一个备用计划的手机和备用计划本身喔。"
                checkBoxId = R.string.do_not_tips_again
                checkBoxChecked = true
                positiveListener2 = { _, checked ->
                    AppSettings.settingsStorage().putBoolean(DO_NOT_SHOW_SPARE_PLAN_TIPS, checked)
                    onConfirm()
                }
            }?.setCancelable(false)
        } else {
            onConfirm()
        }
    }

    private fun setupActionWhenSaved(exitWhenSaved: Boolean) {
        actionWhenSavedSuccessfully = if (exitWhenSaved) {
            { exitFragment() }
        } else {
            null
        }
    }
}