package com.gwchina.parent.binding.presentation

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.MAX_DEVICE_COUNT_PER_CHILD
import com.gwchina.sdk.base.utils.mapChildAvatarBig
import kotlinx.android.synthetic.main.binding_item_child.*

internal class ChildAdapter(context: Context) : SimpleRecyclerAdapter<Child>(context) {

    var onSelectedChildListener: ((childUser: Child) -> Unit)? = null

    private val _addableBgColor = context.colorFromId(R.color.opacity_10_green_main)
    private val _normalBgColor = Color.parseColor("#1450D7F2")

    private val onItemClickListener = View.OnClickListener {
        onSelectedChildListener?.invoke((it.tag as Child))
    }

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.binding_item_child

    override fun bind(viewHolder: KtViewHolder, item: Child) {
        viewHolder.ivBindingItemChildAvatar.setImageResource(mapChildAvatarBig(item.sex))
        viewHolder.tvBindingItemChildName.text = item.nick_name

        val deviceCount = item.device_list?.size ?: 0

        viewHolder.tvBindingItemDeviceCount.text = mContext.getString(R.string.device_count_mask, deviceCount)

        if (deviceCount >= MAX_DEVICE_COUNT_PER_CHILD) {
            viewHolder.setItemClickListener(null)
            viewHolder.tvBindingItemUpperTips.visible()
            viewHolder.itemView.setBackgroundColor(_normalBgColor)
        } else {
            viewHolder.tvBindingItemUpperTips.gone()
            viewHolder.itemView.setBackgroundColor(_addableBgColor)
            viewHolder.itemView.tag = item
            viewHolder.itemView.setOnClickListener(onItemClickListener)
        }
    }

}