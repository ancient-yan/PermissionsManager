package com.gwchina.parent.times.widget

import android.content.Context
import android.graphics.Rect
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import com.android.base.kotlin.dip
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.android.base.kotlin.visibleOrGone
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.common.*
import com.gwchina.parent.times.presentation.make.TimeGuardPlanData
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.utils.Time
import com.gwchina.sdk.base.utils.TimePeriod
import com.gwchina.sdk.base.utils.formatToText
import kotlinx.android.synthetic.main.time_widget_plan_view_collapse.view.*
import kotlinx.android.synthetic.main.times_widget_plan_view.view.*

/**
 * 一个完整的制定时间计划的视图。
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-11 19:24
 */
class TimeGuardPlanView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var currentType = EXPAND

    companion object {
        const val EXPAND = 0
        const val COLLAPSE = 1
    }

    var notOptionalDays: List<Int>? = null
        set(value) {
            if (value.isNullOrEmpty() || selectedGuardDays.isEmpty()) {
                field = value
            } else {
                field = value.toMutableList().apply {
                    removeAll(selectedGuardDays)
                }
            }
        }

    var onUsableDurationClickListener: (() -> Unit)? = null
    var onUsablePeriodChangedListener: (() -> Unit)? = null
    var onUsableDurationChangedListener: ((Time) -> Unit)? = null
    var onGuardDaysChangedListener: ((List<Int>) -> Unit)? = null

    private val timeGuardPlan = TimeGuardDailyPlanVO(enabledTime = NOT_SET_ENABLE_TIME)

    private val selectedGuardDays = mutableListOf<Int>()

    fun setSelectedGuardDays(days: List<Int>) {
        setGuardDays(days)
    }

    fun selectedGuardDays(): List<Int> = selectedGuardDays

    var usableDurationEnable: Boolean
        get() = clTimesWidgetUsableDuration.visibility == View.VISIBLE
        set(value) {
            clTimesWidgetUsableDuration.visibleOrGone(value)
        }

    init {
        View.inflate(context, R.layout.times_widget_plan_view, this)
        setupViews()

        //just for tips
        setupForTips()

        //set adapter
        setTimeGuardPlanAdapter()

    }

    private fun setupViews() {
        tsvTimesWidget.onTimePeriodChangedListener = {
            onUsablePeriodChangedListener?.invoke()
        }
        clTimesWidgetUsableDuration.setOnClickListener {
            if (availableTimeEnable) {
                selectUsableDuration()
            } else {
                onUsableDurationClickListener?.invoke()
            }
        }
        clTimesWidgetGuardDays.setOnClickListener {
            selectGuardDays()
        }
    }

    private fun selectGuardDays() {
        SelectWeekDaysDialog(context) {
            selectedDayList = selectedGuardDays
            limitedDayList = notOptionalDays
            //listener
            onSelectedListener = {
                setGuardDays(it)
            }
        }.show()
    }

    private fun setGuardDays(guardDays: List<Int>) {
        if (selectedGuardDays == guardDays) {
            return
        }
        //ui
        tvTimesWidgetGuardDays.text = if (generateDaysOfWeekText(guardDays).isEmpty()) "未设置" else generateDaysOfWeekText(guardDays)
        //data
        selectedGuardDays.clear()
        selectedGuardDays.addAll(guardDays)
        //notify  recyclerview中会复用view所以重新创建一个list回调回去，用原list会造成复用数据集合混乱
        val result = mutableListOf<Int>().apply {
            addAll(selectedGuardDays)
        }
        onGuardDaysChangedListener?.invoke(result)
    }

    private fun selectUsableDuration() {
        showSelectingUsableDurationDialogForTimePlan(context, timeGuardPlan.enabledTime, tsvTimesWidget.getData()) {
            processOnGuardDurationSelected(Time.fromSeconds(it))
        }
    }

    /**
     * 设置已经选好的时段
     */
    fun setTimeGuardDailyPlan(plan: TimeGuardDailyPlanVO?) {
        tsvTimesWidget.setData(plan?.timePeriodList)

    }

    private fun processOnGuardDurationSelected(time: Time) {
        //data
        timeGuardPlan.enabledTime = time.toSeconds()
        //ui
        tvTimesWidgetUsableDuration.text = time.formatToText()
        tvTimesWidgetUsableDuration.isSelected = true
        //notify
        onUsableDurationChangedListener?.invoke(time)
    }

    var isSelectingGuardDaysEnable: Boolean
        get() = clTimesWidgetGuardDays.isEnabled
        set(value) {
            clTimesWidgetGuardDays.isEnabled = value
        }

    /**可用时长是否可用*/
    var availableTimeEnable: Boolean = true
        set(value) {
            if (value) {
                tvTimesWidgetUsableDuration.text = context.getString(R.string.click_to_set)
            } else {
                tvTimesWidgetUsableDuration.text = context.getString(R.string.open_vip_to_experience_mask, AppContext.appDataSource().user().vipRule?.time_defend_available_time_enabled_minimum_level)
            }
            field = value
        }

    /**根据[setGuardDays]设置的守护日，生成对应的守护计划*/
    fun toTimeGuardPlans(): List<TimeGuardDailyPlanVO> {
        timeGuardPlan.timePeriodList.clear()
        timeGuardPlan.timePeriodList.addAll(tsvTimesWidget.getData())

        val usableDuration = checkUsableDurationWhenSettingTimePlan(timeGuardPlan.enabledTime, timeGuardPlan.timePeriodList)

        return selectedGuardDays.map {
            timeGuardPlan.copy(dayOfWeek = it, enabledTime = usableDuration)
        }
    }

    fun isCompleted(): Boolean {
        if (selectedGuardDays.isEmpty()) {
            return false
        }
        if (!usableDurationEnable) {
            return !tsvTimesWidget.getData().isNullOrEmpty()
        }
        return !tsvTimesWidget.getData().isNullOrEmpty() || timeGuardPlan.enabledTime > 0
    }

    //------------------------------------------ show tips ------------------------------------------

    private fun setupForTips() {
        otTimesWidgetTips.setOnClickListener {
            onTimeTableOperated?.invoke()
        }

        tsvTimesWidget.onTimeTableOperated = {
            onTimeTableOperated?.invoke()
        }
        tsvTimesWidget.onTimeBlockOperated = {
            onTimeBlockOperated?.invoke()
        }
    }

    fun setTimeTableOperationTipsVisibility(visible: Boolean) {
        otTimesWidgetTips.visibleOrGone(visible)
    }

    fun setTimeBlockOperationTipsVisibility(visible: Boolean) {
        tsvTimesWidget.setTimeBlockOperationTipsVisibility(visible)
    }

    var onTimeTableOperated: (() -> Unit)? = null
    var onTimeBlockOperated: (() -> Unit)? = null

    private fun setLayoutState(timeGuardPlanData: TimeGuardPlanData) {
        setCollapseStateValue(timeGuardPlanData)
        if (currentType == COLLAPSE) {
            clCollapse.visible()
            clExpand.gone()
        } else {
            clCollapse.gone()
            clExpand.visible()
        }
    }

    fun changeCurrentState(timeGuardPlanData: TimeGuardPlanData) {
        currentType = timeGuardPlanData.state
        setLayoutState(timeGuardPlanData)
        setTimeGuardData(timeGuardPlanData)
    }

    /**
     * item展开时的数据
     */
    private fun setTimeGuardData(timeGuardPlanData: TimeGuardPlanData){
        //可用时长
        if (timeGuardPlanData.hasSetTimeDuration) {
            tvTimesWidgetUsableDuration.isSelected = true
            tvTimesWidgetUsableDuration.text = Time.fromSeconds(timeGuardPlanData.timeGuardDailyPlanVO.enabledTime).formatToText()
        } else {
            tvTimesWidgetUsableDuration.isSelected = false
            tvTimesWidgetUsableDuration.text = if (availableTimeEnable) "未设置" else context.getString(R.string.open_vip_to_experience_mask, AppContext.appDataSource().user().vipRule?.time_defend_available_time_enabled_minimum_level)
        }
        //守护日
        tvTimesWidgetGuardDays.text =  if (generateDaysOfWeekText(timeGuardPlanData.selectedGuardDays).isEmpty()) "未设置" else generateDaysOfWeekText(timeGuardPlanData.selectedGuardDays)
    }

    private fun setTimeGuardPlanAdapter() {
        rvWidget.addItemDecoration(object : RecyclerView.ItemDecoration() {
            private val offset = dip(5)
            private val topOffset = dip(15)
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                if (parent.getChildAdapterPosition(view) <= 2) {
                    outRect.set(offset, 0, offset, 0)
                } else {
                    outRect.set(offset, topOffset, offset, 0)
                }
            }
        })
        rvWidget.adapter = SegmentAdapter(context, true)
//        clCollapse.setOnClickListener { findParentChangeLayoutState(it) }
    }

/*    private fun findParentChangeLayoutState(it: View) {
        var viewGroup = it.parent
        while (viewGroup !is LinearLayoutCompat) {
            viewGroup = viewGroup.parent
        }
        val childCount = viewGroup.childCount
        if (childCount < 2) return
        for (index in 0 until childCount) {
            val child = viewGroup.getChildAt(index) as TimeGuardPlanView
            child.changeCurrentState(COLLAPSE)
        }
        changeCurrentState(EXPAND)
    }*/

    private fun setCollapseStateValue(timeGuardPlanData: TimeGuardPlanData) {
        tvCanUsePerDay.text = if (timeGuardPlanData.hasSetTimeDuration) Time.fromSeconds(timeGuardPlanData.timeGuardDailyPlanVO.enabledTime).formatToText() else "未设置"
        tvGuardDayValue.text = if (generateDaysOfWeekText(timeGuardPlanData.selectedGuardDays).isEmpty()) "未设置" else generateDaysOfWeekText(timeGuardPlanData.selectedGuardDays)
        val result = timeGuardPlanData.timePeriodList
        if (result.isNullOrEmpty()) {
            result.add(TimePeriod(Time(), Time(), type = 1))
        } else {
            if (result.size > 1 && result.any { it.type == 1 }) {
                result.removeAt(result.size - 1)
            }
        }
        (rvWidget.adapter as SegmentAdapter).setDataSource(result, true)
    }

    /**
     * 内容是否修改——针对初次制定时间计划
     */
    fun contentHaveChanged(): Boolean {
        return tvTimesWidgetUsableDuration.isSelected || tvTimesWidgetGuardDays.text != context.getString(R.string.click_to_set) || !tsvTimesWidget.getData().isNullOrEmpty()
    }
}
