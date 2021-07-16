package com.gwchina.parent.times.presentation.table

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.adapter.recycler.ViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.widget.ratio.RatioTextView
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.utils.getDayOfWeek

internal class WeekAdapter(context: Context, isSpareTimePlan: Boolean = false) : RecyclerAdapter<String, ViewHolder>(context) {

    var onSelectedDayChanged: ((day: Int) -> Unit)? = null

    var selectedDay = getDayOfWeek()
        private set

    private val _onClickListener = View.OnClickListener {
        val dayOfWeek = it.tag as Int
        if (dayOfWeek != selectedDay) {
            val oldSelected = selectedDay
            selectedDay = dayOfWeek
            notifyItemChanged(oldSelected - 1)
            notifyItemChanged(selectedDay - 1)
            onSelectedDayChanged?.invoke(selectedDay)
        }
    }

    init {
        val dataListText = mContext.resources.getStringArray(R.array.guard_day_of_week)

        val week = dataListText.toMutableList()

        val dayOfWeek = getDayOfWeek()

        if (!isSpareTimePlan)
            week[dayOfWeek - 1] = mContext.getString(R.string.today)

        setDataSource(week, false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = RatioTextView(mContext).apply {
            setRatio(1F)
            textSize = 12F
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundResource(R.drawable.times_sel_week_days_item)
        }
        return ViewHolder(textView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val textView = viewHolder.itemView as TextView
        textView.text = getItem(position)
        textView.isSelected = viewHolder.adapterPosition + 1 == selectedDay
        if (textView.isSelected) {
            textView.setTextColor(mContext.colorFromId(R.color.white))
        } else {
            textView.setTextColor(mContext.colorFromId(R.color.gray_level2))
        }
        textView.tag = (position + 1)//day of week
        textView.setOnClickListener(_onClickListener)
    }

}