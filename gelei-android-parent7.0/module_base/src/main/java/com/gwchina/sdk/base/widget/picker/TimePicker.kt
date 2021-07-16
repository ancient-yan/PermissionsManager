package com.gwchina.sdk.base.widget.picker


import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.view.View
import android.view.WindowManager
import cn.qqtheme.framework.picker.DatePicker
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.android.base.utils.android.SoftKeyboardUtils
import com.app.base.R
import java.util.*

fun minChildDate(): Calendar = Calendar.getInstance().apply {
    set(Calendar.YEAR, 1998)
    set(Calendar.MONTH, 0)
    set(Calendar.DAY_OF_MONTH, 1)
}

fun maxChildDate(): Calendar = Calendar.getInstance()


fun minPatriarchDate(): Calendar = Calendar.getInstance().apply {
    set(Calendar.YEAR, 1900)
    set(Calendar.MONTH, 0)
    set(Calendar.DAY_OF_MONTH, 1)
}

/**[initDate]、[minDate]、[maxDate] 的月份从 0 到 11 */
class SelectDateConfig(val context: Context) {
    var initDate: Calendar? = null
    var minDate: Calendar? = null
    var maxDate: Calendar? = null
    var onDateSelectedListener: ((year: Int, monthOfYear: Int, dayOfMonth: Int) -> Unit)? = null
}

fun Fragment.selectDate(config: SelectDateConfig.() -> Unit) {
    context?.let {
        selectDate(it, config)
    }
}

@Suppress("UNUSED")
fun Activity.selectDate(config: SelectDateConfig.() -> Unit) {
    selectDate(this, config)
}

fun selectDate(context: Context, config: SelectDateConfig.() -> Unit) {

    val selectDateConfig = SelectDateConfig(context).also {
        config(it)
    }

    val nonnullInitDate = selectDateConfig.initDate ?: Calendar.getInstance()

    val minDate = selectDateConfig.minDate
    val maxDate = selectDateConfig.maxDate

    val picker = DatePicker(context as Activity)

    picker.setCanceledOnTouchOutside(true)
    picker.setUseWeight(true)
    picker.setTopPadding(dip(10))
    picker.setContentPadding(dip(10), dip(10))
    picker.setLabelTextColor(context.colorFromId(R.color.black))
    picker.setTextColor(context.colorFromId(R.color.black), context.colorFromId(R.color.gray_level2))
    picker.setCancelTextColor(context.colorFromId(R.color.green_main))
    picker.setSubmitTextColor(context.colorFromId(R.color.green_main))
    picker.setDividerColor(context.colorFromId(R.color.green_main))
    picker.setTopLineColor(context.colorFromId(R.color.green_main))

    if (minDate != null) {
        picker.setRangeStart(minDate.get(Calendar.YEAR), minDate.get(Calendar.MONTH) + 1, minDate.get(Calendar.DAY_OF_MONTH))
    }

    if (maxDate != null) {
        picker.setRangeEnd(maxDate.get(Calendar.YEAR), maxDate.get(Calendar.MONTH) + 1, maxDate.get(Calendar.DAY_OF_MONTH))
    }

    picker.setSelectedItem(nonnullInitDate.get(Calendar.YEAR), nonnullInitDate.get(Calendar.MONTH) + 1, nonnullInitDate.get(Calendar.DAY_OF_MONTH))

    picker.setOnDatePickListener(DatePicker.OnYearMonthDayPickListener { year, month, day ->
        selectDateConfig.onDateSelectedListener?.invoke(year.toInt(), month.toInt(), day.toInt())
    })

    SoftKeyboardUtils.hideSoftInput(context)

    picker.setAnimationStyle(R.style.Style_Anim_Bottom_In)

    //https://stackoverflow.com/questions/23520892/unable-to-hide-navigation-bar-during-alertdialog-logindialog
    picker.window?.run {
        setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
        decorView.systemUiVisibility = uiOptions
        clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    picker.show()
}