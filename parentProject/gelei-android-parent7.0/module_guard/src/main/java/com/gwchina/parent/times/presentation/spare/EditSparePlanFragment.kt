package com.gwchina.parent.times.presentation.spare

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.*
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.common.*
import com.gwchina.parent.times.presentation.table.WeekAdapter
import com.gwchina.parent.times.widget.SelectWeekDaysDialog
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.utils.disableEmojiEntering
import com.gwchina.sdk.base.utils.formatSecondsToTimeText
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.times_fragment_edit_spare_plan.*
import kotlinx.android.synthetic.main.times_fragment_time_table.btnTimesSave
import kotlinx.android.synthetic.main.times_fragment_time_table.clTimesCopyPlans
import kotlinx.android.synthetic.main.times_fragment_time_table.clTimesUsableDuration
import kotlinx.android.synthetic.main.times_fragment_time_table.rvTimesWeek
import kotlinx.android.synthetic.main.times_fragment_time_table.tsvTimesSegmentView
import kotlinx.android.synthetic.main.times_fragment_time_table.tvTimesCopyPlans
import kotlinx.android.synthetic.main.times_fragment_time_table.tvTimesUsableDuration
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-23 19:37
 */
class EditSparePlanFragment : InjectorBaseFragment() {

    companion object {

        private const val DATA_KEY = "DATA_KEY"

        fun newInstance(timeGuardWeeklyPlanVO: TimeGuardWeeklyPlanVO) = EditSparePlanFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DATA_KEY, timeGuardWeeklyPlanVO)
            }
        }

    }

    @Inject
    lateinit var eventCenter: TimeEventCenter

    private val viewModel by lazy {
        getViewModel<EditSparePlanViewModel>(viewModelFactory)
    }

    private lateinit var weeklyPlan: TimeGuardWeeklyPlanVO

    /**remember the original plan*/
    private lateinit var idListOriginalPlaned: List<Pair<String, Int>>

    private var selectedDailyPlan: TimeGuardDailyPlanVO? = null

    private var planChanged = false

    private val weekAdapter by lazy {
        WeekAdapter(requireContext(), true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        weeklyPlan = arguments?.getParcelable(DATA_KEY) ?: throw NullPointerException()

        idListOriginalPlaned = weeklyPlan.dailyPlans.map {
            Pair(it.id, it.dayOfWeek)
        }

        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.times_fragment_edit_spare_plan

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        setupTitle()
        setupWeekList()
        setupPlanViews()
        showGuardPlan(weekAdapter.selectedDay)

        cetTimesPlanName.editText.disableEmojiEntering()
        cetTimesPlanName.editText.setText(weeklyPlan.planName)
        cetTimesPlanName.editText.textWatcher {
            afterTextChanged {
                val newName = it.toString()
                btnTimesSave.isEnabled = newName.isNotEmpty()
                weeklyPlan.planName = newName
                planChanged = true
            }
        }
    }

    private fun setupTitle() {
        gtlTimesTitle.setOnNavigationOnClickListener {
            activity?.onBackPressed()
        }

        gtlTimesTitle.menu.add(R.string.delete)
                .setIcon(R.drawable.icon_delete_black)
                .alwaysShow()
                .onMenuItemClick {
                    selectDaysToDelete()
                }
    }

    private fun setupWeekList() {
        rvTimesWeek.addItemDecoration(MarginDecoration(dip(6)))
        rvTimesWeek.adapter = weekAdapter
        weekAdapter.onSelectedDayChanged = { showGuardPlan(it) }
    }

    private fun showGuardPlan(selectedDay: Int) = showPlanOfSelectedDay(selectedDay, getDailyPlan(selectedDay))

    private fun getDailyPlan(selectedDay: Int): TimeGuardDailyPlanVO? {
        return weeklyPlan.dailyPlans.find {
            it.dayOfWeek == selectedDay
        }
    }

    private fun showPlanOfSelectedDay(selectedDay: Int, plan: TimeGuardDailyPlanVO?) {
        selectedDailyPlan = plan
        //可用时段
        tsvTimesSegmentView.setData(plan?.timePeriodList)

        //可用时长title
        tvTimesUsableDurationTitle.text = getString(R.string.usable_duration_of_day_mask, getSelectDayName(selectedDay))

        //可用时长和复制计划
        if (plan == null) {
            tvTimesUsableDuration.setText(R.string.not_set)
            clTimesCopyPlans.gone()
        } else {
            tvTimesUsableDuration.text = if (plan.enabledTime == -1) {
                getString(R.string.not_set)
            } else {
                formatSecondsToTimeText(plan.enabledTime)
            }
            clTimesCopyPlans.visible()
            tvTimesCopyPlans.text = getString(R.string.copy_x_plan_to_mask).format(getSelectDayName(plan.dayOfWeek))
        }
    }

    private fun getSelectDayName(dayOfWeek: Int) = checkDayNameIfToday(weekAdapter.getItem(dayOfWeek - 1))

    private fun setupPlanViews() {
//        clTimesUsableDuration.visibleOrGone(viewModel.childDeviceIsAndroid)

        tsvTimesSegmentView.onTimePeriodChangedListener = {
            val dailyPlan = selectedDailyPlan
            if (dailyPlan == null) {
                addNewPlanForSelectedDay().timePeriodList.addAll(it.getData())
            } else {
                dailyPlan.timePeriodList.clear()
                dailyPlan.timePeriodList.addAll(it.getData())
            }

            planChanged = true
        }

        clTimesUsableDuration.setOnClickListener {
            selectUsableDuration()
        }

        clTimesCopyPlans.setOnClickListener {
            selectedDailyPlan?.let(::selectDayToCopy)
        }

        btnTimesSave.setOnClickListener {
            if (planChanged) {
                doUpdate()
            } else {
                exitFragment()
            }
        }
    }

    private fun selectUsableDuration() {
        val enabledTime = selectedDailyPlan?.enabledTime ?: NOT_SET_ENABLE_TIME
        showSelectingUsableDurationDialogForTimePlan(requireContext(), enabledTime, selectedDailyPlan?.timePeriodList) {
            val dailyPlan = selectedDailyPlan
            if (dailyPlan == null) {
                addNewPlanForSelectedDay().enabledTime = it
            } else {
                dailyPlan.enabledTime = it
            }
            tvTimesUsableDuration.text = formatSecondsToTimeText(it)

            planChanged = true
        }
    }

    private fun addNewPlanForSelectedDay(selectedDay: Int = weekAdapter.selectedDay, setAsSelectedPlan: Boolean = true) =
            with(TimeGuardDailyPlanVO(dayOfWeek = selectedDay)) {
                id = idListOriginalPlaned.find { idWithDay ->
                    idWithDay.second/*day*/ == selectedDay
                }?.first ?: ""
                if (setAsSelectedPlan) {
                    selectedDailyPlan = this
                }
                enabledTime = NOT_SET_ENABLE_TIME/*未设置*/
                weeklyPlan.dailyPlans.add(this)
                this
            }

    private fun selectDayToCopy(dailyPlanVO: TimeGuardDailyPlanVO) {
        SelectWeekDaysDialog(requireContext()) {
            hiddenDayList = listOf(weekAdapter.selectedDay)
            title = getString(R.string.copy_x_day_plan_to_mask, getDayOfWeekName(dailyPlanVO.dayOfWeek))
            tips = getString(R.string.guard_day_rule_copy_tips)
            //listener
            onSelectedListener = {
                doCopy(it, dailyPlanVO)
            }
        }.show()
    }

    private fun doCopy(days: List<Int>, dailyPlanVO: TimeGuardDailyPlanVO) {
        val copier = { toBeCopied: TimeGuardDailyPlanVO ->
            toBeCopied.enabledTime = dailyPlanVO.enabledTime
            toBeCopied.timePeriodList.clear()
            toBeCopied.timePeriodList.addAll(dailyPlanVO.timePeriodList)
        }

        val dailyPlans = weeklyPlan.dailyPlans

        days.forEach { day ->
            dailyPlans.find { plan ->
                plan.dayOfWeek == day
            }.ifNonNull {
                copier(this)
            } otherwise {
                copier(addNewPlanForSelectedDay(day, false))
            }
        }

        if (days.contains(weekAdapter.selectedDay)) {
            showGuardPlan(weekAdapter.selectedDay)
        }

        planChanged = true
    }

    private fun selectDaysToDelete() {
        val map = weeklyPlan.dailyPlans.map { it.dayOfWeek }
        val apply = (1..7).toMutableList().apply { removeAll(map) }
        SelectWeekDaysDialog(requireContext()) {
            titleRes = R.string.select_guard_day_to_delete_title
            limitedDayList = apply
            //listener
            onSelectedListener = ::doDelete
        }.show()
    }

    private var mTimeGuardDailyPlanVO: TimeGuardDailyPlanVO? = null
    private fun doDelete(days: List<Int>) {
        //记录一个要删除的数据
        if (days.isNotEmpty()) {
            run out@{
                for (day in days) {
                    weeklyPlan.dailyPlans.forEach { timeGuardDailyPlanVO ->
                        if (day == timeGuardDailyPlanVO.dayOfWeek) {
                            mTimeGuardDailyPlanVO = timeGuardDailyPlanVO
                            return@out
                        }
                    }
                }
            }
        }
        days.forEach { day ->
            weeklyPlan.dailyPlans.remove(weeklyPlan.dailyPlans.find { plan ->
                plan.dayOfWeek == day
            })
        }
        if (days.contains(weekAdapter.selectedDay)) {
            showGuardPlan(weekAdapter.selectedDay)
        }

        planChanged = true
    }

    private fun doUpdate() {
        val toBeDeleted = idListOriginalPlaned.toMutableList().apply {
            val toBeUpdated = weeklyPlan.dailyPlans.map { it.dayOfWeek }
            removeWhich { toBeUpdated.contains(it.second) }
        }.map { it.first }

        val dailyPlans = weeklyPlan.dailyPlans.map {
            it.copy(enabledTime = checkUsableDurationWhenSettingTimePlan(it.enabledTime, it.timePeriodList))
        }.toMutableList()
        removeTimePeriodWithType(dailyPlans)
        ensureBatchTimeNotNull(dailyPlans)
        viewModel.updateSparePlan(weeklyPlan.copy(dailyPlans = dailyPlans), toBeDeleted)
    }

    /**
     * 解决28727的bug batch_time miss,确保batch_time数组不为空 构造一个删除的数据
     */
    private fun ensureBatchTimeNotNull(dailyPlans: MutableList<TimeGuardDailyPlanVO>) {
        if (dailyPlans.isNotEmpty()) return
        mTimeGuardDailyPlanVO?.apply {
            dailyPlans.add(mTimeGuardDailyPlanVO!!)
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

    private fun subscribeViewModel() {
        viewModel.updateSparePlan.observe(this, Observer {
            it?.onError { error ->
                dismissLoadingDialog()
                errorHandler.handleError(error)
            }?.onSuccess {
                eventCenter.notifySparePlansChanged()
                showMessage(R.string.save_successfully)
                exitFragment()
            }?.onLoading {
                showLoadingDialog(false)
            }
        })
    }

    override fun handleBackPress(): Boolean {
        if (planChanged) {
            askExit()
            return true
        }
        return false
    }

    private fun askExit() {
        showConfirmDialog {
            messageId = R.string.save_edits_tips
            positiveListener = { exitFragment() }
        }
    }

}