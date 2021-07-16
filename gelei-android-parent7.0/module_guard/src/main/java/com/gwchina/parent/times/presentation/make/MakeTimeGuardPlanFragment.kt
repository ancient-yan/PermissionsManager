package com.gwchina.parent.times.presentation.make

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.dip
import com.android.base.kotlin.gone
import com.android.base.kotlin.textWatcher
import com.android.base.kotlin.visible
import com.android.base.widget.recyclerview.MarginDecoration
import com.google.android.flexbox.FlexboxLayoutManager
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.TimeGuardNavigator
import com.gwchina.parent.times.common.TimeEventCenter
import com.gwchina.parent.times.common.TimeGuardDailyPlanVO
import com.gwchina.parent.times.presentation.make.MakingPlanInfo.Companion.TYPE_NORMAL
import com.gwchina.parent.times.widget.TimeGuardPlanView
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.config.AppSettings.TIME_OPERATION_TIPS_SHOWED_FLAG
import com.gwchina.sdk.base.data.models.timePlanHasBeSet
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.disableEmojiEntering
import com.gwchina.sdk.base.utils.textStr
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.times_fragment_make_plans.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * - 制作时间计划
 * - 制定备用计划
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-09 13:58
 */
class MakeTimeGuardPlanFragment : InjectorBaseFragment() {

    companion object {

        private const val MAKING_INFO_KEY = "making_info_key"

        fun newInstance(makingPlanInfo: MakingPlanInfo) = MakeTimeGuardPlanFragment().apply {
            arguments = Bundle().apply {
                putParcelable(MAKING_INFO_KEY, makingPlanInfo)
            }
        }

    }

    @Inject
    lateinit var timeGuardNavigator: TimeGuardNavigator
    @Inject
    lateinit var eventCenter: TimeEventCenter

    private lateinit var makingPlanInfo: MakingPlanInfo

    private val viewModel by lazy {
        getViewModel<MakeTimeGuardPlanViewModel>(viewModelFactory)
    }

    /**Days already be planed are not optional*/
    private var daysAlreadyPlaned: List<Int> = emptyList()
    /**All selectable days of making step currently*/
    private val allSelectableDays = mutableListOf<Int>()
    /**Selected days of making step currently*/
    private val selectedGuardDays = mutableListOf<Int>()

    private val planViews = mutableListOf<TimeGuardPlanView>()

    private var isTypeNormal: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        makingPlanInfo = arguments?.getParcelable(MAKING_INFO_KEY) ?: throw NullPointerException()

        subscribeToViewModel()
    }

    override fun provideLayout() = R.layout.times_fragment_make_plans

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        setupOptionalNames()
        addFirstPlanView()
        showOperationTipsIfNeed()
        setupListeners()
    }

    private fun addFirstPlanView() {
        daysAlreadyPlaned = makingPlanInfo.notOptionalDays ?: emptyList()
        allSelectableDays.addAll((1..7).toList())
        allSelectableDays.removeAll(daysAlreadyPlaned)

        addPlanView()

        with(makingPlanInfo.selectedDays) {
            if (!this.isNullOrEmpty()) {
                selectedGuardDays.addAll(this)
                planViews[0].setSelectedGuardDays(this)
                if (allSelectableDays.size == 1) {
                    planViews[0].isSelectingGuardDaysEnable = false
                }
            }
        }

    }

    /*备用计划名称*/
    private fun setupOptionalNames() {
        if (makingPlanInfo.makingType != MakingPlanInfo.TYPE_SPARE) {
            cetTimesPlanName.gone()
            rvTimesPlanNameSuggestions.gone()
            return
        }

        isTypeNormal = false

        val optionalNameAdapter = OptionalNameAdapter(requireContext())

        rvTimesPlanNameSuggestions.layoutManager = FlexboxLayoutManager(requireContext())
        rvTimesPlanNameSuggestions.addItemDecoration(MarginDecoration(0, 0, dip(10), dip(10)))
        rvTimesPlanNameSuggestions.adapter = optionalNameAdapter

        val usedNames = makingPlanInfo.usedName

        val names = resources.getStringArray(R.array.spare_plan_name_optional).run {
            if (!usedNames.isNullOrEmpty()) {
                filter { !usedNames.contains(it) }
            } else {
                this.toList()
            }
        }.map {
            Name(it, false)
        }

        cetTimesPlanName.editText.disableEmojiEntering()
        optionalNameAdapter.onNameSelected = {
            cetTimesPlanName.editText.setText(it.value)
        }

        cetTimesPlanName.editText.textWatcher {
            afterTextChanged {
                if (it.toString().isEmpty()) {
                    optionalNameAdapter.enableAllItems()
                    rvTimesPlanNameSuggestions.visible()
                    tvTimesPlanNameDuplicateTips.gone()
                }
                checkPlansStatus()
            }
        }

        if (names.isEmpty()) {
            rvTimesPlanNameSuggestions.gone()
        } else {
            optionalNameAdapter.replaceAll(names)
        }

    }

    private fun setupListeners() {
        gwlTimesMakePlan.setOnNavigationOnClickListener {
            activity?.onBackPressed()
        }

        ivTimeAddPlan.setOnClickListener {
            addPlanView()
            setChildViewExpandByPosition(llTimesMakePlanContents.childCount - 1)
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_START_BTN_ADD_NEW_PLAN)
        }

        btnTimeComplete.setOnClickListener {
            setTimeGuardPlans()
            if (makingPlanInfo.makingType == TYPE_NORMAL) {
                StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_START_BTN_FINISH)
            } else {
                StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_FINISH)
            }
        }
    }

    private fun addPlanView() {

        with(TimeGuardPlanView(requireContext())) {

            usableDurationEnable = viewModel.childDeviceIsAndroid || makingPlanInfo.makingType == MakingPlanInfo.TYPE_SPARE
            notOptionalDays = buildNotOptionalDays()

            onGuardDaysChangedListener = {
                processGuardDaysChanged()
                if (makingPlanInfo.makingType == TYPE_NORMAL) {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_START_BTN_SETDATE)
                } else {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_EDITOR_CHANGESHIDUAN)
                }
            }

            onUsablePeriodChangedListener = {
                checkPlansStatus()
            }

            onUsableDurationChangedListener = {
                checkPlansStatus()
                if (makingPlanInfo.makingType == TYPE_NORMAL) {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_START_BTN_SETSHICHANG)
                } else {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_EDITOR_CHANGESHICHANG)
                }
            }

            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            planViews.add(this)
            llTimesMakePlanContents.addView(this)
        }

        checkPlansStatus()

        svTimesMakePlan.post {
            svTimesMakePlan?.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    /**
     * 7.0.1新需求：
     * 新添加了新的时间计划卡片后，之前展开的时间计划卡片收起：
    （1）新添加的卡片位置自动上拉完整显示在完成按钮之上；
    （2）收起的卡片点击后可展开，恢复至可编辑状态；
    （3）同时，页面中仅可有一张展开的卡片；
    （4）未设置时段的显示时段为0：00-0：00，每日可用和守护日显示为未设置；
     */
    private fun setChildViewExpandByPosition(position: Int) {
        val childCount = llTimesMakePlanContents.childCount
        if (childCount < 2) return
        for (index in 0 until childCount) {
            val child = llTimesMakePlanContents.getChildAt(index) as TimeGuardPlanView
            if (position == index) {
//                child.changeCurrentState(TimeGuardPlanView.EXPAND)
            } else {
//                child.changeCurrentState(TimeGuardPlanView.COLLAPSE)
            }
        }
    }

    private fun processGuardDaysChanged() {
        selectedGuardDays.clear()
        planViews.forEach {
            selectedGuardDays.addAll(it.selectedGuardDays())
        }
        selectedGuardDays.sort()

        val newLimitedDays = buildNotOptionalDays()

        planViews.forEach {
            it.notOptionalDays = newLimitedDays
        }

        checkPlansStatus()
    }

    private fun buildNotOptionalDays(): MutableList<Int> = mutableListOf<Int>().apply {
        addAll(selectedGuardDays)
        addAll(daysAlreadyPlaned)
    }

    private fun checkPlansStatus() {

        val isAllPlansCompleted = planViews.all { it.isCompleted() }
        val isAnyPlansCompleted = planViews.any { it.isCompleted() }

        ivTimeAddPlan.isEnabled = isAllPlansCompleted && selectedGuardDays != allSelectableDays

        btnTimeComplete.isEnabled = if (makingPlanInfo.makingType == TYPE_NORMAL) {
            isAnyPlansCompleted
        } else {
            isAnyPlansCompleted && cetTimesPlanName.editText.textStr().isNotEmpty()
        }
    }

    private fun showOperationTipsIfNeed() {
        if (viewModel.deviceCount != 1) {
            return
        }
        if (AppSettings.settingsStorage().getBoolean(TIME_OPERATION_TIPS_SHOWED_FLAG, false)) {
            return
        }

        with(planViews[0]) {

            setTimeTableOperationTipsVisibility(true)
            setTimeBlockOperationTipsVisibility(true)

            onTimeTableOperated = {
                setTimeTableOperationTipsVisibility(false)
                AppSettings.settingsStorage().putBoolean(TIME_OPERATION_TIPS_SHOWED_FLAG, true)
                onTimeTableOperated = null
            }

            onTimeBlockOperated = {
                setTimeBlockOperationTipsVisibility(false)
                AppSettings.settingsStorage().putBoolean(TIME_OPERATION_TIPS_SHOWED_FLAG, true)
                onTimeBlockOperated = null
            }
        }
    }

    override fun handleBackPress(): Boolean {
        if (firstMakePlanAskExit()){
            return true
        }
        if (btnTimeComplete.isEnabled) {
            askExit()
            return true
        }
        return false
    }

    /**
     * 首次制定时间计划时，有内容设置提示弹窗
     */
    private fun firstMakePlanAskExit(): Boolean {
        if (isTypeNormal) {
            val childView = llTimesMakePlanContents.getChildAt(0) as TimeGuardPlanView
            if (childView.contentHaveChanged()) {
                askExit()
                return true
            }
        }
        return false
    }

    private fun askExit() {
        showConfirmDialog {
            messageId = R.string.save_edits_tips
            positiveListener = { exitFragment() }
        }
    }

    private fun setTimeGuardPlans() {
        if (makingPlanInfo.makingType == MakingPlanInfo.TYPE_SPARE && makingPlanInfo.usedName?.contains(cetTimesPlanName.editText.textStr()) == true) {
            tvTimesPlanNameDuplicateTips.visible()
            rvTimesPlanNameSuggestions.gone()
            return
        }

        planViews.filter {
            it.isCompleted()
        }.map {
            it.toTimeGuardPlans()
        }.fold(mutableListOf<TimeGuardDailyPlanVO>()) { acc, list ->
            acc.addAll(list)
            acc
        }.let {
            if (makingPlanInfo.makingType == TYPE_NORMAL) {
                viewModel.addTimeGuardPlans(it)
            } else {
                viewModel.addSparePlans(cetTimesPlanName.editText.textStr(), it)
            }
        }
    }

    private fun subscribeToViewModel() {
        viewModel.addPlan
                .observe(this, Observer { resource ->
                    resource?.onLoading {
                        showLoadingDialog(false)
                    }?.onError {
                        dismissLoadingDialog()
                        errorHandler.handleError(it)
                    }?.onSuccess {
                        dismissLoadingDialog()
                        processAddPlanSuccessfully()
                    }
                })
    }

    private fun processAddPlanSuccessfully() {
        if (makingPlanInfo.makingType == TYPE_NORMAL) {
            showMessage(R.string.time_table_update_successfully_tips)
            eventCenter.notifyPlansChanged()
            timeGuardNavigator.openGuardTimePlanTablePage()
            saveFirstSetTimeGuardDate()
        } else {
            eventCenter.notifySparePlansChanged()
            timeGuardNavigator.openSparePlanListPage()
        }
    }

    private var haveSetTimeGuardKey = "haveSetTimeGuard"

    @SuppressLint("SimpleDateFormat")
    @Synchronized
    private fun saveFirstSetTimeGuardDate() {
        if (AppContext.appDataSource().user().currentDevice.timePlanHasBeSet()) {
            return
        }
        if (AppSettings.settingsStorage().getString(haveSetTimeGuardKey).isNullOrEmpty()) {
            //保存的是明天0点0分0秒的时间
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, 1)
            val time = cal.time
            AppSettings.settingsStorage().putString(haveSetTimeGuardKey, SimpleDateFormat("yyyy-MM-dd 00:00:00").format(time))
        }
    }

}