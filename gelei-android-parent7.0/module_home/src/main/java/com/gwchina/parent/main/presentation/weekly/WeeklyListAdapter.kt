package com.gwchina.parent.main.presentation.weekly

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.interfaces.OnItemClickListener
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.ifNonNull
import com.android.base.kotlin.visible
import com.android.base.kotlin.visibleOrGone
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.MainActivity
import com.gwchina.parent.main.data.ReportInfo
import com.gwchina.sdk.base.data.models.findChild
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.mapChildAvatarSmall
import kotlinx.android.synthetic.main.week_list_item.*

/**
 *@author hujie
 *      Email: hujie1991@126.com
 *      Date : 2019-08-13 14:36
 */

internal class WeeklyListAdapter(val host: WeeklyListFragment) : SimpleRecyclerAdapter<WeeklyVO>(host.requireContext()) {

    var onDetailListener: OnItemClickListener<ReportInfo>? = null

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.week_list_item

    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: KtViewHolder, item: WeeklyVO) {

        viewHolder.itemView.tag = item.report_list
        viewHolder.itemView.setOnClickListener(onDetailListener)

        viewHolder.tvWeekMonth.text = item.month
        if (viewHolder.adapterPosition > 0) {
            val lastItem = getItem(viewHolder.adapterPosition - 1)
            viewHolder.tvWeekMonth.visibleOrGone(lastItem.month != item.month)
        } else {
            viewHolder.tvWeekMonth.visible()
        }

        item.report_list.ifNonNull {
            viewHolder.tvWeekClid.text = nick_name.foldText(10)
            viewHolder.tvWeekDate.text = "${begin_date}—${end_date}"
            viewHolder.tvWeekSummary.text = comment

            val mainActivity = host.activity as MainActivity
            val child = mainActivity.appDataSource.user().findChild(user_id ?: "")
            viewHolder.ivWeekChildHead.setImageResource(mapChildAvatarSmall(child?.sex))
        }
    }
}