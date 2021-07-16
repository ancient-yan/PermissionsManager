package com.gwchina.sdk.base.widget.dialog

import android.view.View
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.app.base.R
import com.gwchina.sdk.base.utils.Time
import com.gwchina.sdk.base.utils.to2BitText
import kotlinx.android.synthetic.main.dialog_select_duration.*
import java.util.*


/**
 * 选择可用时长
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : Date : 2019-01-12 16:06
 */
class SelectDurationDialog(
        private val durationDialogBuilder: SelectDurationDialogBuilder
) : BaseDialog(durationDialogBuilder.context, false, durationDialogBuilder.style) {

    private val hourList = ArrayList<Int>()
    private val minuteList = ArrayList<Int>()

    private var selectedHour: Int = durationDialogBuilder.initDuration?.hour ?: 0
    private var selectedMinute: Int = durationDialogBuilder.initDuration?.minute ?: 0

    private var maxHour: Int = durationDialogBuilder.limitedDuration?.hour ?: 0
    private var maxMinute: Int = durationDialogBuilder.limitedDuration?.minute ?: 0

    init {
        setContentView(R.layout.dialog_select_duration)
        initWheel()
        setupTips()
        setListeners()
    }

    private fun initWheel() {
        val hourStringList = mutableListOf<String>()
        //小时
        val range = durationDialogBuilder.durationRange
        for (i in range.start.hour..range.end.hour) {
            hourList.add(i)
            hourStringList.add(i.to2BitText())
        }
        //分钟
        val minuteStringList = mutableListOf<String>()
        for (i in range.start.minute..range.end.minute step 10) {
            minuteList.add(i)
            minuteStringList.add(i.to2BitText())
        }
        wheelDialogGuardHour.data = hourStringList
        wheelDialogGuardMinute.data = minuteStringList
        //初始化
        wheelDialogGuardHour.post {
            val indexOfHour = hourList.indexOf(selectedHour)
            if (indexOfHour != -1) {
                wheelDialogGuardHour.selectedItemPosition = indexOfHour
            } else {
                selectedHour = hourList[0]
            }
            val indexOfMinute = minuteList.indexOf(selectedMinute)
            if (indexOfMinute != -1) {
                wheelDialogGuardMinute.selectedItemPosition = indexOfMinute
            } else {
                selectedMinute = minuteList[0]
            }

            checkIfMust()
        }
    }

    private fun setupTips() {
        val tips = durationDialogBuilder.tips
        if (tips.isNullOrEmpty()) {
            tvDialogOverDurationTips.gone()
        } else {
            tvDialogOverDurationTips.text = tips
            tvDialogOverDurationTips.visible()
        }
    }

    private fun setListeners() {
        wheelDialogGuardHour.setOnItemSelectedListener { _, _, position ->
            selectedHour = hourList[position]
            checkIfMust()
        }
        wheelDialogGuardMinute.setOnItemSelectedListener { _, _, position ->
            selectedMinute = minuteList[position]
            checkIfMust()
        }

        dblDialogSelectDuration.onNegativeClick(View.OnClickListener {
            if (durationDialogBuilder.autoDismiss) {
                dismiss()
            }
            durationDialogBuilder.negativeListener?.invoke(this)
        })

        dblDialogSelectDuration.onPositiveClick {
            if (maxHour != 0 || maxMinute != 0) {
                if (maxHour < selectedHour) {
                    showOverLimitedIfNeed()
                    return@onPositiveClick
                }
                if (maxHour == selectedHour && maxMinute < selectedMinute) {
                    showOverLimitedIfNeed()
                    return@onPositiveClick
                }
            }
            durationDialogBuilder.positiveListener?.invoke(this, Time(selectedHour, selectedMinute))
            if (durationDialogBuilder.autoDismiss) {
                dismiss()
            }
        }
    }

    private fun checkIfMust() {
        dblDialogSelectDuration.positiveEnable = !(durationDialogBuilder.must && selectedHour == 0 && selectedMinute == 0)
    }

    private fun showOverLimitedIfNeed() {
        val onOverLimitedTipsFactory = durationDialogBuilder.onOverLimitedTipsFactory ?: return
        if (durationDialogBuilder.tips.isNullOrEmpty()) {
            val tips = onOverLimitedTipsFactory(Time(selectedHour, selectedMinute))
            if (tips.isEmpty()) {
                tvDialogOverDurationTips.visible()
                tvDialogOverDurationTips.text = tips
            }
        }
    }

}
