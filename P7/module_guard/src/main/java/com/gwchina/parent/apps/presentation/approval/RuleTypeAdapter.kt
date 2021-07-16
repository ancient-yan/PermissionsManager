package com.gwchina.parent.apps.presentation.approval

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.guard.R
import kotlinx.android.synthetic.main.apps_item_app_approval_choice.*

internal data class Selection(val iconRes: Int, val name: String, val ruleType: Int)

internal class RuleTypeAdapter(
        context: Context,
        list: List<Selection>,
        var selectedRuleType: Int
) : RecyclerAdapter<Selection, KtViewHolder>(context, list) {

    var onRuleTypeChanged: ((ruleType: Int) -> Unit)? = null

    private val onItemClickListener = View.OnClickListener {
        selectedRuleType = (it.tag as Selection).ruleType
        onRuleTypeChanged?.invoke(selectedRuleType)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            KtViewHolder(LayoutInflater.from(mContext).inflate(R.layout.apps_item_app_approval_choice, parent, false))

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.ivAppItemPermissionIcon.setImageResource(item.iconRes)
        viewHolder.tvAppItemPermissionName.text = item.name
        if (item.ruleType == selectedRuleType) {
            viewHolder.ivAppItemPermissionCheck.setImageResource(R.drawable.icon_radio_button_checked)
        } else {
            viewHolder.ivAppItemPermissionCheck.setImageResource(R.drawable.icon_radio_button_normal)
        }
        viewHolder.itemView.tag = item
        viewHolder.itemView.setOnClickListener(onItemClickListener)
    }

}