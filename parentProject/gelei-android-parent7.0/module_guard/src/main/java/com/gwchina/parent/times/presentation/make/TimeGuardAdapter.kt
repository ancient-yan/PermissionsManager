package com.gwchina.parent.times.presentation.make

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.widget.TimeGuardPlanView
import com.gwchina.parent.times.widget.TimeSegmentView
import com.gwchina.sdk.base.config.AppSettings
import timber.log.Timber

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-28 14:54
 */
class TimeGuardAdapter(val context: Context, val viewModel: MakeTimeGuardPlanViewModel, val recyclerView: RecyclerView) : SimpleRecyclerAdapter<TimeGuardPlanData>(context) {

    var onGuardDataChangedListener: (() -> Unit)? = null

    override fun provideLayout(parent: ViewGroup, viewType: Int): Any {
        val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        params.marginStart = context.resources.getDimension(R.dimen.common_edge).toInt()
        params.marginEnd = context.resources.getDimension(R.dimen.common_edge).toInt()
        return TimeGuardPlanView(context = mContext).apply {
            this.layoutParams = params
        }
    }


    override fun bind(viewHolder: KtViewHolder, item: TimeGuardPlanData) {
        val timeGuardPlanView = viewHolder.containerView as TimeGuardPlanView
        setLayout(timeGuardPlanView)
        if (viewHolder.adapterPosition == 0) {
            showOperationTipsIfNeed(timeGuardPlanView)
        }
        viewHolder.itemView.setOnClickListener {
            if (getItem(viewHolder.adapterPosition).state == TimeGuardPlanView.EXPAND) return@setOnClickListener
            notifyStateChange(viewHolder.adapterPosition)
            notifyDataSetChanged()
            recyclerView.scrollToPosition(viewHolder.adapterPosition)
        }
        timeGuardPlanView.setSelectedGuardDays(item.selectedGuardDays)
        timeGuardPlanView.setTimeGuardDailyPlan(item.timeGuardDailyPlanVO)
        val tsvTimesWidget = timeGuardPlanView.findViewById<TimeSegmentView>(R.id.tsvTimesWidget)
        timeGuardPlanView.notOptionalDays = selectedDays()
        timeGuardPlanView.onGuardDaysChangedListener = {
            val position = viewHolder.adapterPosition
            getItem(position).selectedGuardDays = it
            onGuardDataChangedListener?.invoke()
        }
        tsvTimesWidget.onTimePeriodChangedListener = {
            val position = viewHolder.adapterPosition
            getItem(position).timeGuardDailyPlanVO.timePeriodList.clear()
            getItem(position).timeGuardDailyPlanVO.timePeriodList.addAll(it.getData())
            onGuardDataChangedListener?.invoke()
        }
        timeGuardPlanView.onUsableDurationChangedListener = {
            val position = viewHolder.adapterPosition
            getItem(position).timeGuardDailyPlanVO.enabledTime = it.toSeconds()
            getItem(position).hasSetTimeDuration = true
            onGuardDataChangedListener?.invoke()
        }
        timeGuardPlanView.changeCurrentState(item)
    }

    var isTypeSpare: Boolean = false

    var notOptionalDays: List<Int>? = null

    private fun setLayout(timeGuardPlanView: TimeGuardPlanView) {
        timeGuardPlanView.usableDurationEnable = viewModel.childDeviceIsAndroid || isTypeSpare
    }

    private fun notifyStateChange(position: Int) {
        (0 until dataSize).forEach {
            if (it == position) {
                getItem(it).state = TimeGuardPlanView.EXPAND
            } else {
                getItem(it).state = TimeGuardPlanView.COLLAPSE
            }
        }
    }

    private val mSelectedList = mutableListOf<Int>()

     fun selectedDays(): List<Int> {
        mSelectedList.clear()
        (0 until dataSize).forEach {
            getItem(it).selectedGuardDays.forEach {
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
        if (viewModel.deviceCount != 1) {
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