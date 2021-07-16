package com.gwchina.parent.apps.presentation.group

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.data.App
import com.gwchina.sdk.base.utils.displayAppIcon
import kotlinx.android.synthetic.main.apps_item_app_icon_only.*

internal class GroupAppsAdapter(private val host: Fragment) : RecyclerAdapter<App, KtViewHolder>(host.requireContext()) {

    var onAddAppListener: (() -> Unit)? = null
    var onAddDeleteListener: (() -> Unit)? = null

    init {
        setHeaderSize { 1 }
    }

    private val _onDeleteAppClickListener = View.OnClickListener {
        remove(it.tag as App)
        onAddDeleteListener?.invoke()
    }

    private val _onAddAppClickListener = View.OnClickListener {
        onAddAppListener?.invoke()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        return KtViewHolder(LayoutInflater.from(mContext).inflate(R.layout.apps_item_app_icon_only, parent, false))
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        if (viewHolder.adapterPosition == 0) {
            viewHolder.ivAppsItemAppInGroupClose.invisible()
            viewHolder.ivAppsItemAppInGroupIcon.setImageResource(R.drawable.icon_add_square)
            viewHolder.ivAppsItemAppInGroupIcon.setOnClickListener(_onAddAppClickListener)
        } else {
            val item = this.getItem(position)
            ImageLoaderFactory.getImageLoader().displayAppIcon(host, viewHolder.ivAppsItemAppInGroupIcon, item.soft_icon)
            viewHolder.ivAppsItemAppInGroupIcon.setOnClickListener(null)
            viewHolder.ivAppsItemAppInGroupClose.visible()
            viewHolder.ivAppsItemAppInGroupClose.tag = item
            viewHolder.ivAppsItemAppInGroupClose.setOnClickListener(_onDeleteAppClickListener)
        }
    }

    override fun getItemCount(): Int {
        return 1 + dataSize
    }

}