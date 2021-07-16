package com.gwchina.parent.main.presentation.home.card

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.adapter.recycler.SimpleItemViewBinder
import com.android.base.kotlin.*
import com.android.base.utils.android.ResourceUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.presentation.home.CardInteractor
import com.gwchina.parent.main.presentation.home.UsingTrajectoryItem
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.utils.layoutCommonEdge
import com.gwchina.sdk.base.widget.views.setClickFeedback
import kotlinx.android.synthetic.main.home_card_using_trajectory.view.*
import kotlinx.android.synthetic.main.home_card_using_trajectory_item_day.*
import timber.log.Timber

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-01 16:08
 */
class UsingTrajectoryCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var interactor: CardInteractor

    private var usingTrajectory: List<UsingTrajectoryItem>? = null

    private val trajectoryAdapter by lazy {
        MultiTypeAdapter(context)
    }

    private val dayItemViewBinder by lazy {
        DayItemViewBinder()
    }

    init {
        orientation = VERTICAL
        setPadding(layoutCommonEdge, 0, layoutCommonEdge, 0)
        View.inflate(context, R.layout.home_card_using_trajectory, this)
    }

    fun setup(cardInteractor: CardInteractor) {
        interactor = cardInteractor
        setupList()
        subscribeData()
    }

    private fun setupList() {
        trajectoryAdapter.register(UsingTrajectoryItem::class.java)
                .to(
                        dayItemViewBinder,
                        HalfDayItemViewBinder(),
                        ContentItemViewBinder(),
                        EmptyItemViewBinder()
                ).withLinker { _, t ->
                    when (t.type) {
                        UsingTrajectoryItem.TYPE_DAY -> 0
                        UsingTrajectoryItem.TYPE_HALF_DAY -> 1
                        UsingTrajectoryItem.TYPE_ITEM -> 2
                        UsingTrajectoryItem.TYPE_EMPTY -> 3
                        else -> throw IllegalStateException("unsupported type")
                    }
                }

        rvHomeUsingTrajectory.addItemDecoration(TrajectoryItemDecoration(context, trajectoryAdapter))
        rvHomeUsingTrajectory.adapter = trajectoryAdapter
    }

    private fun subscribeData() {
        interactor.cardDataProvider.observeUser {
            if (!it.logined() || it.currentChild == null || it.currentDevice == null) {
                tvHomeUsingTrajectoryDesc.visible()
                ivHomeNoUsingTrajectory.visible()

                rvHomeUsingTrajectory.gone()
                rvHomeUsingTrajectoryBottomTips.gone()
            } else {
                tvHomeUsingTrajectoryDesc.gone()
                ivHomeNoUsingTrajectory.gone()

                rvHomeUsingTrajectory.visible()
                rvHomeUsingTrajectoryBottomTips.visible()
                openUsingRecord()
            }
        }

        interactor.cardDataProvider.observeHomeData {
            if (usingTrajectory != it?.usingTrajectory) {
                dayItemViewBinder.electricQuantity = it?.deviceInfo?.battery_level ?: 0
                trajectoryAdapter.setDataSource(it?.usingTrajectory ?: emptyList(), true)
                usingTrajectory = it?.usingTrajectory
            } else if (dayItemViewBinder.electricQuantity != it?.deviceInfo?.battery_level ?: 0 && trajectoryAdapter.dataSize > 0) {
                dayItemViewBinder.electricQuantity = it?.deviceInfo?.battery_level ?: 0
                trajectoryAdapter.notifyItemChanged(0)
            }
            val size = it?.usingTrajectory?.count {
                it.type == UsingTrajectoryItem.TYPE_ITEM
            }

            rvHomeUsingTrajectoryBottomTips.apply {
                if (size != null && size >= 10) {
                    text = "查看更多使用记录"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.gray_level1))
                    background = ContextCompat.getDrawable(context, R.drawable.shape_gray_solid_round_5)
                } else {
                    text = context.getString(R.string.reach_bottom_tips)
                    textSize = 12f
                    background = null
                    setTextColor(ContextCompat.getColor(context, R.color.gray_level2))
                }
            }
        }
    }

    private fun openUsingRecord() {
        rvHomeUsingTrajectoryBottomTips.setClickFeedback()
        rvHomeUsingTrajectoryBottomTips.setOnClickListener {
            if (rvHomeUsingTrajectoryBottomTips.text == context.getString(R.string.reach_bottom_tips))
                return@setOnClickListener
            interactor.navigator.openUsingRecordPage()
        }
    }

}

internal class TrajectoryItemDecoration(context: Context, private val trajectoryAdapter: MultiTypeAdapter) : RecyclerView.ItemDecoration() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.colorFromId(R.color.gray_cutting_line)
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val childAdapterPosition = parent.getChildAdapterPosition(view)
        val item = trajectoryAdapter.getItem(childAdapterPosition) as? UsingTrajectoryItem ?: return

        when (item.type) {
            UsingTrajectoryItem.TYPE_FOOTER -> {
                outRect.set(dip(8), dip(20), 0, 0)
            }

            UsingTrajectoryItem.TYPE_DAY -> {
                if (childAdapterPosition != 0) {
                    outRect.top = dip(40)
                }
            }
            UsingTrajectoryItem.TYPE_HALF_DAY -> {
                outRect.set(dip(8), dip(36), 0, 0)
            }
            UsingTrajectoryItem.TYPE_ITEM -> {
                outRect.set(dip(39), dip(26), 0, 0)
            }
            UsingTrajectoryItem.TYPE_EMPTY -> {
                outRect.set(dip(39), dip(24), 0, 0)
            }
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val leftOffset = dip(18F)
        val childCount = parent.childCount
        var child: View
        var adapterPosition: Int
        var item: UsingTrajectoryItem?

        for (i in 0 until childCount) {
            child = parent.getChildAt(i)
            adapterPosition = parent.getChildAdapterPosition(child)
            item = trajectoryAdapter.getItem(adapterPosition) as? UsingTrajectoryItem

            Timber.d("childCount $childCount adapterPosition $adapterPosition item $item")

            if (item == null) {
                continue
            }

            when (item.type) {
                UsingTrajectoryItem.TYPE_DAY -> {
                    if (adapterPosition != 0) {
                        c.drawLine(leftOffset, child.top.toFloat() - dip(40), leftOffset, child.top.toFloat(), paint)
                    }
                }
                UsingTrajectoryItem.TYPE_HALF_DAY -> {
                    c.drawLine(leftOffset, child.top.toFloat() - dip(36), leftOffset, child.top.toFloat(), paint)
                }
                UsingTrajectoryItem.TYPE_ITEM -> {
                    c.drawLine(leftOffset, child.top.toFloat() - dip(26), leftOffset, child.bottom.toFloat(), paint)
                }
                UsingTrajectoryItem.TYPE_EMPTY -> {
                    c.drawLine(leftOffset, child.top.toFloat() - dip(24), leftOffset, child.bottom.toFloat(), paint)
                }
            }
        }
    }
}

internal class DayItemViewBinder(private var showBattery: Boolean = true) : SimpleItemViewBinder<UsingTrajectoryItem>() {

    var electricQuantity: Int = 0

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.home_card_using_trajectory_item_day

    override fun onBindViewHolder(holder: KtViewHolder, item: UsingTrajectoryItem) {
        if (holder.adapterPosition == 0 && showBattery) {
            holder.tvHomeItemDayBattery.visible()
            holder.tvHomeItemDayBattery.text = ResourceUtils.getString(R.string.reaming_electric_quantity_mask, electricQuantity.toString())
        } else {
            holder.tvHomeItemDayBattery.gone()
        }
        holder.tvHomeItemDay.text = item.content
    }

}

internal class HalfDayItemViewBinder : SimpleItemViewBinder<UsingTrajectoryItem>() {

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = AppCompatTextView(parent.context).apply {
        leftDrawable(R.drawable.home_icon_time)
        compoundDrawablePadding = dip(11)
        setTextColor(colorFromId(R.color.gray_level2))
        textSize = 12F
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: UsingTrajectoryItem) {
        (holder.itemView as TextView).text = item.content
    }

}

internal class ContentItemViewBinder : SimpleItemViewBinder<UsingTrajectoryItem>() {

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = AppCompatTextView(parent.context).apply {
        setTextColor(colorFromId(R.color.gray_level1))
        textSize = 14F
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: UsingTrajectoryItem) {
        (holder.itemView as TextView).text = item.content
    }

}

class EmptyItemViewBinder : SimpleItemViewBinder<UsingTrajectoryItem>() {
    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.home_card_using_trajectory_item_empty
    override fun onBindViewHolder(holder: KtViewHolder, item: UsingTrajectoryItem) = Unit
}