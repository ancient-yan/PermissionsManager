package com.gwchina.parent.times.presentation.spare

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.adapter.recycler.SimpleItemViewBinder
import com.android.base.kotlin.*
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.widget.SegmentAdapter
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.layoutCommonEdge
import kotlinx.android.synthetic.main.times_item_spare_plan_bottom.*
import kotlinx.android.synthetic.main.times_item_spare_plan_content.*
import kotlinx.android.synthetic.main.times_item_spare_plan_header.*


class SparePlanItemHeaderViewBinder(private val isChoosingMode: Boolean) : SimpleItemViewBinder<SparePlanHeader>() {

    var onPlanSelected: ((selectedPlanId: String) -> Unit)? = null

    var onDeletePlan: ((SparePlanHeader) -> Unit)? = null

    private var selectedPlan: SparePlanHeader? = null

    val selectedPlanId: String?
        get() = selectedPlan?.batchId

    private val onDeletePlanClickListener = View.OnClickListener {
        onDeletePlan?.invoke(it.tag as SparePlanHeader)
    }

    private val onExpandingFoldingPlanClickListener = View.OnClickListener {
        val item = (it.tag as SparePlanHeader)
        item.expanded = !item.expanded

        if (item.expanded) {
            dataManager.addItemsAt(dataManager.getItemPosition(item) + 1, item.content)
        } else {
            dataManager.removeItems(item.content, true)
        }

        dataManager.notifyEntryChanged(item)
    }

    private val onSelectedPlanClickListener = View.OnClickListener {
        val previous = selectedPlan
        val selected = it.tag as SparePlanHeader
        selectedPlan = selected
        if (previous != null) {
            dataManager.notifyEntryChanged(previous)
        }
        dataManager.notifyEntryChanged(selectedPlan)
        onPlanSelected?.invoke(selected.batchId)
    }

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.times_item_spare_plan_header

    override fun onBindViewHolder(holder: KtViewHolder, item: SparePlanHeader) {
        //名称
        holder.tvTimesItemPlan.text = item.batchName

        if (isChoosingMode) {
            holder.tvTimesItemPlan.tag = item
            holder.tvTimesItemPlan.setOnClickListener(onSelectedPlanClickListener)
            if (item.batchId == selectedPlan?.batchId) {
                holder.tvTimesItemPlan.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_radio_button_checked, 0, 0, 0)
            } else {
                holder.tvTimesItemPlan.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_radio_button_normal, 0, 0, 0)
            }
        } else {
            holder.tvTimesItemPlan.setCompoundDrawablesWithIntrinsicBounds(R.drawable.times_icon_plan, 0, 0, 0)
        }

        //状态
        holder.tvTimesItemPlanStatus.visibleOrGone(item.using)
        holder.tvTimesItemDeletePlan.visibleOrGone(!(item.using || isChoosingMode))
        holder.tvTimesItemDeletePlan.setOnClickListener(onDeletePlanClickListener)
        holder.tvTimesItemDeletePlan.tag = item

        //描述
        holder.tvTimesItemPlanTotalDuration.text = item.weeklyTimeDesc

        //展开折叠
        if (item.expanded) {
            holder.tvTimesItemPlanExpandOrFold.setText(R.string.fold_up_plan)
            holder.tvTimesItemPlanExpandOrFold.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.times_icon_fold, 0)
        } else {
            holder.tvTimesItemPlanExpandOrFold.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.times_icon_expand, 0)
            holder.tvTimesItemPlanExpandOrFold.setText(R.string.expand_plan)
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_SPREAD)
        }

        holder.tvTimesItemPlanExpandOrFold.tag = item
        holder.tvTimesItemPlanExpandOrFold.setOnClickListener(onExpandingFoldingPlanClickListener)
    }

}

class SparePlanItemContentViewBinder : SimpleItemViewBinder<SparePlanContent>() {

    private val recycledViewPool = RecyclerView.RecycledViewPool()

    private val itemDecoration = object : RecyclerView.ItemDecoration() {

        private val offset = dip(5)
        private val bottomOffset = dip(15)

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            outRect.set(offset, 0, offset, bottomOffset)
        }
    }

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.times_item_spare_plan_content

    override fun onViewHolderCreated(viewHolder: KtViewHolder) {
        viewHolder.rvTimesPlanSegment.setRecycledViewPool(recycledViewPool)
        viewHolder.rvTimesPlanSegment.addItemDecoration(itemDecoration)
        viewHolder.rvTimesPlanSegment.adapter = SegmentAdapter(viewHolder.itemView.context)
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: SparePlanContent) {
        holder.tvTimesPlanGroupInfo.text = item.contentDesc
        (holder.rvTimesPlanSegment.adapter as SegmentAdapter).setDataSource(item.timePeriod, true)
        holder.vTimesPlanSegmentDivider.visibleOrGone(item.isBottom)
    }

}

class SparePlanItemBottomViewBinder : SimpleItemViewBinder<SparePlanBottom>() {

    var onStartPlan: ((SparePlanBottom) -> Unit)? = null
    var onStopUsingPlan: ((SparePlanBottom) -> Unit)? = null
    var onEditPlan: ((SparePlanBottom) -> Unit)? = null

    private val onStartUsingPlanClickListener = View.OnClickListener {
        onStartPlan?.invoke(it.tag as SparePlanBottom)
    }

    private val onStopUsingPlanClickListener = View.OnClickListener {
        onStopUsingPlan?.invoke(it.tag as SparePlanBottom)
    }

    private val onEditPlanClickListener = View.OnClickListener {
        onEditPlan?.invoke(it.tag as SparePlanBottom)
    }

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.times_item_spare_plan_bottom

    override fun onBindViewHolder(holder: KtViewHolder, item: SparePlanBottom) {
        holder.tvTimesItemLeftBtn.tag = item
        holder.tvTimesItemRightBtn.tag = item
        if (item.using) {
            holder.groupTimesItemRightSide.gone()
            holder.tvTimesItemLeftBtn.setText(R.string.stop_plan)
            holder.tvTimesItemLeftBtn.setTextColor(holder.itemView.colorFromId(R.color.red_level1))
            holder.tvTimesItemLeftBtn.setOnClickListener(onStopUsingPlanClickListener)
        } else {
            holder.groupTimesItemRightSide.visible()
            holder.tvTimesItemLeftBtn.setTextColor(holder.itemView.colorFromId(R.color.gray_level2))
            holder.tvTimesItemLeftBtn.setOnClickListener(onEditPlanClickListener)
            holder.tvTimesItemRightBtn.setOnClickListener(onStartUsingPlanClickListener)
            holder.tvTimesItemLeftBtn.setText(R.string.edit)
            holder.tvTimesItemRightBtn.setText(R.string.enable)
        }
    }

}

class SparePlanItemTipsViewBinder : SimpleItemViewBinder<String>() {

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = TextView(parent.context).apply {
        setPadding(layoutCommonEdge, dip(20), dip(20), layoutCommonEdge)
        textSize = 12F
        setTextColor(colorFromId(R.color.gray_level3))
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: String) {
        (holder.itemView as TextView).text = "*如果孩子端是iPhone/iPad，备用计划的可用时长会失效喔"
    }

}
