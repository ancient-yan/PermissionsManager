package com.gwchina.parent.times.presentation.make

import android.support.constraint.ConstraintLayout
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.common.calculateUsablePeriodTotalSeconds
import com.gwchina.parent.times.widget.TimeGuardPlanView
import com.gwchina.parent.times.widget.TimeSegmentView
import com.gwchina.sdk.base.config.AppSettings
import me.drakeet.multitype.ItemViewBinder
import timber.log.Timber

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-03 12:19
 */
class TimeGuardBinder(val host: MakeTimeGuardPlanFragment2) : ItemViewBinder<TimeGuardPlanData, KtViewHolder>() {

    var onGuardDataChangedListener: (() -> Unit)? = null
    var onUsableDurationClickListener: (() -> Unit)? = null
    var isSVip: Boolean = true

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        params.marginStart = host.resources.getDimension(R.dimen.common_edge).toInt()
        params.marginEnd = host.resources.getDimension(R.dimen.common_edge).toInt()
        val timeGuardPlanView = TimeGuardPlanView(context = host.requireContext()).apply {
            this.layoutParams = params
        }
        return KtViewHolder(timeGuardPlanView)
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, item: TimeGuardPlanData) {
        val timeGuardPlanView = viewHolder.containerView as TimeGuardPlanView
        setLayout(timeGuardPlanView)
        if (host.makingPlanInfo.makingType == MakingPlanInfo.TYPE_SPARE) {
            if (viewHolder.adapterPosition == 1) {
                showOperationTipsIfNeed(timeGuardPlanView)
            }
        } else if (viewHolder.adapterPosition == 0) {
            showOperationTipsIfNeed(timeGuardPlanView)
        }

        viewHolder.itemView.setOnClickListener {
            Timber.e("position==${viewHolder.adapterPosition}")
            if (item.state == TimeGuardPlanView.EXPAND) return@setOnClickListener
            notifyStateChange(viewHolder.adapterPosition)
            adapter.notifyDataSetChanged()
            host.recyclerView.scrollToPosition(viewHolder.adapterPosition)
        }
        timeGuardPlanView.setSelectedGuardDays(item.selectedGuardDays)
        timeGuardPlanView.setTimeGuardDailyPlan(item.timeGuardDailyPlanVO)
        timeGuardPlanView.availableTimeEnable = isSVip
        val tsvTimesWidget = timeGuardPlanView.findViewById<TimeSegmentView>(R.id.tsvTimesWidget)
        timeGuardPlanView.notOptionalDays = selectedDays()
        timeGuardPlanView.onUsableDurationClickListener = onUsableDurationClickListener
        timeGuardPlanView.onGuardDaysChangedListener = {
            val position = viewHolder.adapterPosition
            (adapter.items[position] as TimeGuardPlanData).selectedGuardDays = it
            onGuardDataChangedListener?.invoke()
        }
        tsvTimesWidget.onTimePeriodChangedListener = {
            val position = viewHolder.adapterPosition
            (adapter.items[position] as TimeGuardPlanData).timeGuardDailyPlanVO.timePeriodList.clear()
            (adapter.items[position] as TimeGuardPlanData).timeGuardDailyPlanVO.timePeriodList.addAll(it.getData())
            /**
             * 2019.12.25改动：没有设置可用时长，可用时长跟随时段变化
             */
            if (!item.hasSetTimeDuration)
            {
                (adapter.items[position] as TimeGuardPlanData).timeGuardDailyPlanVO.enabledTime = calculateUsablePeriodTotalSeconds(it.getData())
            }
            onGuardDataChangedListener?.invoke()
        }
        timeGuardPlanView.onUsableDurationChangedListener = {
            val position = viewHolder.adapterPosition
            (adapter.items[position] as TimeGuardPlanData).timeGuardDailyPlanVO.enabledTime = it.toSeconds()
            onGuardDataChangedListener?.invoke()
            item.hasSetTimeDuration = true
        }
        timeGuardPlanView.changeCurrentState(item)
    }

    var isTypeSpare: Boolean = false

    private fun setLayout(timeGuardPlanView: TimeGuardPlanView) {
        timeGuardPlanView.usableDurationEnable = host.viewModel.childDeviceIsAndroid || isTypeSpare
    }

    private fun notifyStateChange(position: Int) {
        val data = adapter.items
        (data.indices).forEach {
            if (data[it] is TimeGuardPlanData) {
                if (it == position) {
                    (data[it] as TimeGuardPlanData).state = TimeGuardPlanView.EXPAND
                } else {
                    (data[it] as TimeGuardPlanData).state = TimeGuardPlanView.COLLAPSE
                }
            }
        }
    }

    private val mSelectedList = mutableListOf<Int>()

    var notOptionalDays: List<Int>? = null

    fun selectedDays(): List<Int> {
        val data: List<TimeGuardPlanData> = adapter.items.filterIsInstance<TimeGuardPlanData>()
        mSelectedList.clear()
        (data.indices).forEach {
            data[it].selectedGuardDays.forEach {
                mSelectedList.add(it)
            }
        }
        notOptionalDays?.let {
            mSelectedList.addAll(notOptionalDays!!)
        }
        return mSelectedList
    }

    /**
     * 显示提示框
     */
    private fun showOperationTipsIfNeed(timeGuardPlanView: TimeGuardPlanView) {
        if (host.viewModel.deviceCount != 1) {
            return
        }
        if (AppSettings.settingsStorage().getBoolean(AppSettings.TIME_OPERATION_TIPS_SHOWED_FLAG, false)) {
            return
        }

        with(timeGuardPlanView) {

            setTimeTableOperationTipsVisibility(true)
            setTimeBlockOperationTipsVisibility(true)

            onTimeTableOperated = {
                setTimeTableOperationTipsVisibility(false)
                AppSettings.settingsStorage().putBoolean(AppSettings.TIME_OPERATION_TIPS_SHOWED_FLAG, true)
                onTimeTableOperated = null
            }

            onTimeBlockOperated = {
                setTimeBlockOperationTipsVisibility(false)
                AppSettings.settingsStorage().putBoolean(AppSettings.TIME_OPERATION_TIPS_SHOWED_FLAG, true)
                onTimeBlockOperated = null
            }
        }
    }
}