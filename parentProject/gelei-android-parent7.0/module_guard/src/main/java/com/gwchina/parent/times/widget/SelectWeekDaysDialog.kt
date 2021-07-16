package com.gwchina.parent.times.widget

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.adapter.recycler.ViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.android.base.utils.android.ResourceUtils.getString
import com.android.base.utils.android.ResourceUtils.getStringArray
import com.android.base.widget.recyclerview.MarginDecoration
import com.coorchice.library.SuperTextView
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.times_dialog_select_guard_day.*

/**
 * 选择日期
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-15 16:20
 */
internal class SelectWeekDaysDialog(
        context: Context,
        dialogBuilder: DialogBuilder.() -> Unit
) : BaseDialog(context) {

    class DialogBuilder(private val context: Context) {
        /**限制选择的天*/
        var limitedDayList: List<Int>? = null
        /**禁止选择的天（隐藏选项）*/
        var hiddenDayList: List<Int>? = null
        /**已经选的天*/
        var selectedDayList: List<Int>? = null
        /**选择后通过[onSelectedListener]回调通知调用方，选择的天已经按照自然顺序排好序*/
        var onSelectedListener: ((days: List<Int>) -> Unit)? = null
        /**展示的tips*/
        var tips: String? = null
        /**展示的title*/
        var titleRes: Int = 0
            set(value) {
                title = context.getString(value)
            }

        var title: String? = context.getString(R.string.select_guard_day_title)
    }

    private val _selectedDays = mutableListOf<Int>()
    private val _selectedLimitedDays = mutableListOf<Int>()

    private val dialogBuilder: DialogBuilder

    init {
        setContentView(R.layout.times_dialog_select_guard_day)
        this.dialogBuilder = DialogBuilder(context)
        dialogBuilder(this.dialogBuilder)
        val selectedDays = this.dialogBuilder.selectedDayList
        if (selectedDays != null) {
            _selectedDays.addAll(selectedDays)
        }
        setupViews()
    }

    private fun setupViews() {
        initList()
        setupListeners()
    }

    private fun setupListeners() {
        dblDialogBottom.onNegativeClick {
            dismiss()
        }

        dblDialogBottom.positiveEnable = _selectedDays.isNotEmpty()
        dblDialogBottom.onPositiveClick {
            if (_selectedLimitedDays.isEmpty()) {
                dismiss()
                if (_selectedDays.isNotEmpty()) {
                    _selectedDays.sort()
                }
                dialogBuilder.onSelectedListener?.invoke(_selectedDays)
            }
        }

        if (!dialogBuilder.tips.isNullOrEmpty()) {
            tvGuardDayTips.text = dialogBuilder.tips
            tvGuardDayTips.visible()
        } else {
            tvGuardDayTips.gone()
        }

        if (!dialogBuilder.title.isNullOrEmpty()) {
            tvTimesDialogGuardDaysTitle.text = dialogBuilder.title
            tvTimesDialogGuardDaysTitle.visible()
        } else {
            tvTimesDialogGuardDaysTitle.gone()
        }
    }

    private fun initList() {
        rvGuardCustomDays.layoutManager = GridLayoutManager(context, 4)
        rvGuardCustomDays.addItemDecoration(MarginDecoration(dip(5)))

        val adapter = Adapter(context).apply {
            onDayStatusChanged = { day, selected ->
                processOnDaySelected(day, selected)
            }
        }

        val dayOfWeekText = getStringArray(R.array.guard_day_of_week).map { getString(R.string.week_mask, it) }.toList()

        val dataSource = dayOfWeekText.mapIndexed { index, dayText ->
            Day(index + 1, dayText, _selectedDays.contains(index + 1), dialogBuilder.limitedDayList?.contains(index + 1) == true)
        }.run {
            val hiddenDayList = dialogBuilder.hiddenDayList
            if (hiddenDayList.isNullOrEmpty()) {
                this
            } else {
                filter { !hiddenDayList.contains(it.day) }
            }
        }

        adapter.setDataSource(dataSource, false)

        rvGuardCustomDays.adapter = adapter
    }

    private fun processOnDaySelected(day: Int, selected: Boolean) {
        if (selected) {
            _selectedDays.add(day)
        } else {
            _selectedDays.remove(day)
        }

        dblDialogBottom.positiveEnable = _selectedDays.isNotEmpty()

    }

}

private data class Day(val day: Int, val dayText: String, var selected: Boolean, val limited: Boolean)

private class Adapter(context: Context) : RecyclerAdapter<Day, ViewHolder>(context) {

    var onDayStatusChanged: ((day: Int, selected: Boolean) -> Unit)? = null
    var mLimitedDayClicked: ((day: Day) -> Unit)? = null

    private val mOnClickListener = View.OnClickListener {
        val item = it.tag as Day
        if (!item.limited) {
            item.selected = !item.selected
            onDayStatusChanged?.invoke(item.day, item.selected)
            notifyItemChanged(getItemPosition(item))
        } else {
            mLimitedDayClicked?.invoke(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = SuperTextView(mContext)
        itemView.textSize = 14F
        itemView.corner = dip(2F)
        itemView.strokeWidth = 1F
        itemView.gravity = Gravity.CENTER
        itemView.setPadding(0, dip(5), 0, dip(5))
        itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val textView = viewHolder.itemView as SuperTextView
        val item = getItem(position)
        textView.text = item.dayText
        textView.tag = item
        setTextAttrs(item, textView)
    }

    private fun setTextAttrs(item: Day, textView: SuperTextView) {
        when {
            item.limited -> {
                textView.setTextColor(mContext.colorFromId(R.color.gray_level3))
                textView.strokeColor = mContext.colorFromId(R.color.gray_level4)
                textView.solid = mContext.colorFromId(R.color.white)
                textView.setOnClickListener(null)
            }
            item.selected -> {
                textView.setTextColor(mContext.colorFromId(R.color.white))
                textView.strokeColor = mContext.colorFromId(R.color.green_main)
                textView.solid = mContext.colorFromId(R.color.green_main)
                textView.setOnClickListener(mOnClickListener)
            }
            else -> {
                textView.setTextColor(mContext.colorFromId(R.color.gray_level2))
                textView.strokeColor = mContext.colorFromId(R.color.gray_level4)
                textView.solid = mContext.colorFromId(R.color.white)
                textView.setOnClickListener(mOnClickListener)
            }
        }
    }

}