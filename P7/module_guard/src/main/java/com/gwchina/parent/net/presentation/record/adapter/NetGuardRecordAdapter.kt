package com.gwchina.parent.net.presentation.record.adapter

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.utils.android.ResourceUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.net.data.model.GuardRecord
import com.gwchina.sdk.base.utils.subText
import kotlinx.android.synthetic.main.net_item_gurad_record.*

class NetGuardRecordAdapter(private val host: Fragment) : RecyclerAdapter<GuardRecord, KtViewHolder>(host.requireContext()) {

    var urlClickListener: ((url: String) -> Unit)? = null

    private val onClickListener = View.OnClickListener {
        if (urlClickListener != null && it.tag is String) {
            val url = it.tag as String
            urlClickListener?.invoke(url)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.net_item_gurad_record, parent, false)
        view.setOnClickListener(onClickListener)
        return KtViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.itemView.tag = item.url
        viewHolder.net_tv_website.text = item.url
        viewHolder.net_tv_time.text = item.time
        if (item.url_name.isNullOrEmpty()) {
            viewHolder.net_tv_label.text = ResourceUtils.getText(R.string.unknow)
        } else {
            viewHolder.net_tv_label.text = item.url_name.subText(10)
        }

        when (item.type) {
            "02" -> {
                //黑名单拦截
                viewHolder.net_guard_mode.text = host.getString(R.string.blacklist_intercept)
                viewHolder.net_guard_mode.setTextColor(host.resources.getColor(R.color.red_level1))
                viewHolder.net_guard_mode.setBackgroundResource(R.drawable.shape_red_op10_round3)
            }
            else -> {
                //智能拦截
                viewHolder.net_guard_mode.text = host.getString(R.string.intelligent_intercept)
                viewHolder.net_guard_mode.setTextColor(host.resources.getColor(R.color.green_level1))
                viewHolder.net_guard_mode.setBackgroundResource(R.drawable.shape_green_op10_round3)
            }
        }
    }
}