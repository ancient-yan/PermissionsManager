package com.gwchina.parent.screenshot.presentation

import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.screenshot.data.ScreenshotData
import kotlinx.android.synthetic.main.home_remote_screentshot_content_item_layout.*
import me.drakeet.multitype.register

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-05 16:13
 *
 */
class ScreenshotItemBinders(val host: Fragment) : ItemViewBinder<List<ScreenshotData>, KtViewHolder>() {

    var mOnItemClickListener: (() -> Unit)? = null

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        return KtViewHolder(inflater.inflate(R.layout.home_remote_screentshot_content_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: List<ScreenshotData>) {
        holder.rvContent.layoutManager = GridLayoutManager(host.requireContext(), 3)
        val adapter = MultiTypeAdapter(host.requireContext())
        adapter.register(ScreenItemBinders(host, true).apply {
            this.onItemClickListener = { _, position ->
                item[position].isSelected = !item[position].isSelected
                adapter.notifyItemChanged(position)
                mOnItemClickListener?.invoke()
            }
        })
        holder.rvContent.adapter = adapter
        adapter.replaceAll(item)
    }
}
