package com.gwchina.parent.times.widget

import android.app.Dialog
import android.content.Context
import android.view.View
import android.widget.TextView
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.utils.TimePeriod
import com.gwchina.sdk.base.utils.formatTo24BSText
import com.gwchina.sdk.base.utils.to2BitText
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import com.gwchina.sdk.base.widget.dialog.BaseDialogBuilder
import kotlinx.android.synthetic.main.times_dialog_select_guard_period.*
import java.util.*

class SelectPeriodDialogBuilder(context: Context) : BaseDialogBuilder(context) {
    //默认时间点为“06:00至08:00”
    var initPeriod: TimePeriod = TimePeriod.fromTimes(6, 0, 8, 0)
    var onSelectGuardPeriodListener: ((TimePeriod) -> Unit)? = null
    var onDeletePeriodListener: ((TimePeriod) -> Unit)? = null
    var isEdit: Boolean = false
    //是否显示删除图标
    var showDeleteIcon = true
}

fun showSelectPeriodDialog(context: Context, builder: SelectPeriodDialogBuilder.() -> Unit): Dialog {
    val selectPeriodDialogBuilder = SelectPeriodDialogBuilder(context)
    builder.invoke(selectPeriodDialogBuilder)
    val choiceDialog = SelectGuardPeriodDialog(selectPeriodDialogBuilder)
    choiceDialog.show()
    return choiceDialog
}

/**
 * 选择可用时段
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-12-13 17:42
 */
class SelectGuardPeriodDialog(
        private val periodDialogBuilder: SelectPeriodDialogBuilder
) : BaseDialog(periodDialogBuilder.context) {

    private val hourList = ArrayList<Int>()
    private val minuteList = ArrayList<Int>()

    private var _startHour: Int = periodDialogBuilder.initPeriod.startHour()
    private var _startMinute: Int = periodDialogBuilder.initPeriod.startMinute()
    private var _endHour: Int = periodDialogBuilder.initPeriod.endHour()
    private var _endMinute: Int = periodDialogBuilder.initPeriod.endMinute()

    init {
        setContentView(R.layout.times_dialog_select_guard_period)
        applyBuilder()
    }

    private fun applyBuilder() {
        initWheel()
        setupDeletion()
        initSelectPeriodView()
        setupCancelConfirm()
    }

    private fun setupCancelConfirm() {
        dblSelectPeriod.onNegativeClick {
            dismiss()
        }
        setConfirmBtnToNextStep()
    }

    private fun initWheel() {
        //hour
        val hourStringList = mutableListOf<String>()
        for (i in 0..23) {
            hourList.add(i)
            hourStringList.add(i.to2BitText())
        }
        wheelGuardHour.data = hourStringList
        //minute
        val minuteStringList = mutableListOf<String>()
        for (i in 0..50 step 10) {
            minuteList.add(i)
            minuteStringList.add(i.to2BitText())
        }
        wheelGuardMinute.data = minuteStringList
    }

    private fun setupDeletion() {
        if (periodDialogBuilder.isEdit) {
            if (periodDialogBuilder.showDeleteIcon)
                ivGuardWidgetDelete.visible()
            ivGuardWidgetDelete.setOnClickListener {
                dismiss()
                periodDialogBuilder.onDeletePeriodListener?.invoke(periodDialogBuilder.initPeriod)
            }
        }
    }


    private fun setWheelHourValue(hour: Int) {
        val indexOfHour = hourList.indexOf(hour)
        if (indexOfHour != -1) {
            wheelGuardHour.selectedItemPosition = indexOfHour
        }
    }

    private fun setWheelMinuteValue(minute: Int) {
        val indexOfMinute = minuteList.indexOf(minute)
        if (indexOfMinute != -1) {
            wheelGuardMinute.selectedItemPosition = indexOfMinute
        }
    }

    private fun initSelectPeriodView() {
        tvGuardWidgetTimeStart.setPeriodSelectionStatus(true)
        tvGuardWidgetTimeEnd.setPeriodSelectionStatus(false)
        //初始化显示
        wheelGuardHour.post {
            setWheelHourValue(_startHour)
            setWheelMinuteValue(_startMinute)
        }
        tvGuardWidgetTimeStart.text = formatTo24BSText(_startHour, _startMinute)
        tvGuardWidgetTimeEnd.text = formatTo24BSText(_endHour, _endMinute)
        //监听器
        setPeriodListeners()
    }

    private fun setPeriodListeners() {
        wheelGuardHour.setOnItemSelectedListener { _, _, _ ->
            onPeriodWheelChanged()
        }
        wheelGuardMinute.setOnItemSelectedListener { _, _, _ ->
            onPeriodWheelChanged()
        }

        tvGuardWidgetTimeStart.setOnClickListener {
            tvGuardWidgetTimeStart.setPeriodSelectionStatus(true)
            tvGuardWidgetTimeEnd.setPeriodSelectionStatus(false)
            setWheelHourValue(_startHour)
            setWheelMinuteValue(_startMinute)

            setConfirmBtnToNextStep()
        }

        tvGuardWidgetTimeEnd.setOnClickListener {
            tvGuardWidgetTimeStart.setPeriodSelectionStatus(false)
            tvGuardWidgetTimeEnd.setPeriodSelectionStatus(true)
            setWheelHourValue(_endHour)
            setWheelMinuteValue(_endMinute)

            setConfirmBtnToConfirm()
        }
    }

    private fun setConfirmBtnToConfirm() {
        dblSelectPeriod.positiveText(context.getString(R.string.sure))
        dblSelectPeriod.onPositiveClick(View.OnClickListener {
            dismiss()
            if (!(_startHour == _endHour && _startMinute == _endMinute)) {
                periodDialogBuilder.onSelectGuardPeriodListener?.invoke(TimePeriod.fromTimes(_startHour, _startMinute, _endHour, _endMinute))
            }
        })
    }

    private fun setConfirmBtnToNextStep() {
        dblSelectPeriod.positiveText(context.getString(R.string.next_step))
        dblSelectPeriod.onPositiveClick(View.OnClickListener {
            tvGuardWidgetTimeEnd.performClick()
        })
    }

    private fun TextView.setPeriodSelectionStatus(selected: Boolean) {
        this.isSelected = selected
        if (selected) {
            setTextColor(context.colorFromId(R.color.white))
        } else {
            setTextColor(context.colorFromId(R.color.gray_level1))
        }
    }

    private fun onPeriodWheelChanged() {
        val hour = wheelGuardHour.currentItemPosition
        val minute = wheelGuardMinute.currentItemPosition
        if (tvGuardWidgetTimeStart.isSelected) {
            _startHour = hourList[hour]
            _startMinute = minuteList[minute]
            tvGuardWidgetTimeStart.text = formatTo24BSText(_startHour, _startMinute)
        } else if (tvGuardWidgetTimeEnd.isSelected) {
            _endHour = hourList[hour]
            _endMinute = minuteList[minute]
            tvGuardWidgetTimeEnd.text = formatTo24BSText(_endHour, _endMinute)
        }
    }

}