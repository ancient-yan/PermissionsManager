package com.gwchina.parent.times.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.android.base.kotlin.visibleOrGone
import com.coorchice.library.SuperTextView
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.common.TimePeriodOperator
import com.gwchina.sdk.base.utils.Time
import com.gwchina.sdk.base.utils.TimePeriod
import com.gwchina.sdk.base.utils.to2BitText
import com.gwchina.sdk.base.widget.dialog.TipsManager
import kotlinx.android.synthetic.main.times_widget_time_segment.view.*

/**
 * 时间片段视图：
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-11 16:50
 */
class TimeSegmentView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val segmentTableView: TimeSegmentTableView
    private val rvTimeSegmentLabels: RecyclerView

    private val segmentAdapter by lazy {
        SegmentAdapter(context)
    }

    private val timePeriodOperator = TimePeriodOperator()

    var onTimePeriodChangedListener: ((TimeSegmentView) -> Unit)? = null

    init {
        View.inflate(context, R.layout.times_widget_time_segment, this)
        segmentTableView = tsbvTimesWidget
        rvTimeSegmentLabels = rvTimesWidget
        setupViews()

        //just for tips
        setupForTips()
    }

    private fun setupViews() {
        //初始化列表
        rvTimeSegmentLabels.addItemDecoration(object : RecyclerView.ItemDecoration() {
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
        val mutableList = mutableListOf<TimePeriod>()
        mutableList.add(TimePeriod(Time(), Time(), type = 1))
        segmentAdapter.setDataSource(mutableList, true)
        rvTimeSegmentLabels.adapter = segmentAdapter
        segmentAdapter.onItemClickListener = {
            editTimePeriod(it)

            //for tips
            onTimeBlockOperated?.invoke()
        }

        //TimeSegmentTableView 相关设置
        segmentTableView.timeSegmentTableViewListener = object : TimeSegmentTableView.TimeSegmentTableViewListener {
            override fun onTimeSegmentClicked(timePeriod: TimePeriod, timePeriodList: List<TimePeriod>) {
                editTimePeriod(timePeriod)

                //just for tips
                showTipsIfNeed()
                onTimeTableOperated?.invoke()
            }

            override fun onTimeSegmentChanged(timePeriodList: List<TimePeriod>) {
                val timePeriodListResult = timePeriodList.toMutableList()
                addTimeIcon(timePeriodListResult)
                segmentAdapter.setDataSource(timePeriodListResult, true)
                timePeriodOperator.replace(timePeriodListResult)
                onTimePeriodChangedListener?.invoke(this@TimeSegmentView)

                //just for tips
                showTipsIfNeed()
                onTimeTableOperated?.invoke()
            }

        }
    }

    private fun editTimePeriod(timePeriod: TimePeriod) {
        showSelectPeriodDialog(context) {
            showDeleteIcon = timePeriod.type == 0
            initPeriod = timePeriod
            isEdit = true
            onSelectGuardPeriodListener = {
                if (!it.is24Hours()) {
                    timePeriodOperator.changePeriod(timePeriod, it)
                    notifyGuardPeriodChanged()
                }
            }
            onDeletePeriodListener = {
                timePeriodOperator.deletePeriod(timePeriod)
                TipsManager.showMessage(R.string.delete_period_successfully_tips)
                notifyGuardPeriodChanged()

                //just for tips
                showTipsIfNeed()
            }
        }
    }

    private fun notifyGuardPeriodChanged() {
        val timePeriodResult = timePeriodOperator.result.toMutableList()
        segmentTableView.setSelectedSegments(timePeriodResult)
        segmentAdapter.setDataSource(timePeriodResult, true)
        onTimePeriodChangedListener?.invoke(this)
    }

    fun setData(list: List<TimePeriod>?) {
        val timePeriodResult = list?.toMutableList()
        segmentTableView.setSelectedSegments(timePeriodResult)
        segmentAdapter.setDataSource(timePeriodResult, true)
        timePeriodOperator.replace(timePeriodResult ?: mutableListOf())
    }

    private fun addTimeIcon(list: MutableList<TimePeriod>) {
        if (list.none { it.type == 1 }) {
            list.add(TimePeriod(Time(), Time(), type = 1))
        }
    }


    fun getData(): MutableList<TimePeriod> {
        return timePeriodOperator.result
    }

    //------------------------------------------ show tips ------------------------------------------

    private fun showTipsIfNeed() {
        opvTimesWidget.visibleOrGone(showTimeTableOperationTips && !segmentAdapter.isEmpty)
    }

    private var showTimeTableOperationTips: Boolean = false

    fun setTimeBlockOperationTipsVisibility(visible: Boolean) {
        showTimeTableOperationTips = visible
        showTipsIfNeed()
    }

    var onTimeTableOperated: (() -> Unit)? = null
    var onTimeBlockOperated: (() -> Unit)? = null

    private fun setupForTips() {
        opvTimesWidget.setOnClickListener {
            onTimeBlockOperated?.invoke()
        }
    }

}

/**
 * [isMakeTimeGuardType]是否是制定时间计划的
 */
internal class SegmentAdapter(context: Context, private var isMakeTimeGuardType: Boolean = false) : SimpleRecyclerAdapter<TimePeriod>(context) {

    var onItemClickListener: ((TimePeriod) -> Unit)? = null

    private val _onClickListener = View.OnClickListener {
        onItemClickListener?.invoke(it.tag as TimePeriod)
    }

    override fun provideLayout(parent: ViewGroup, viewType: Int) = SuperTextView(parent.context).apply {
        textSize = 14F
        solid = mContext.colorFromId(R.color.opacity_10_green_main)
        pressBgColor = mContext.colorFromId(R.color.opacity_30_green_main)
        setTextColor(mContext.colorFromId(R.color.green_main))
        gravity = Gravity.CENTER
        corner = dip(2F)
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setPadding(0, dip(5), 0, dip(5))
    }

    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: KtViewHolder, item: TimePeriod) {
        (viewHolder.itemView as TextView).run {
            tag = item
            if (item.type == 0 || isMakeTimeGuardType) {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(0, dip(5), 0, dip(5))
                text = "${item.startHour()}:${item.startMinute().to2BitText()}-${item.endHour()}:${item.endMinute().to2BitText()}"
                background = null
            } else {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(0, 0, 0, 0)
                text = ""
                background = ContextCompat.getDrawable(context, R.drawable.times_add)
            }
            setOnClickListener(if (isMakeTimeGuardType) null else _onClickListener)
        }
    }
}