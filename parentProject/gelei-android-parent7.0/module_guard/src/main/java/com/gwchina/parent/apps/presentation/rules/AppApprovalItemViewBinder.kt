package com.gwchina.parent.apps.presentation.rules

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.utils.android.ResourceUtils.getString
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.data.App
import com.gwchina.sdk.base.utils.displayAppIcon
import com.gwchina.sdk.base.utils.emptyElse
import kotlinx.android.synthetic.main.apps_item_app_approval.*

open class AppApprovalItemViewBinder(private val host: Fragment) : ItemViewBinder<App, KtViewHolder>() {

    var onProhibitListener: ((App) -> Unit)? = null
    var onAllowListener: ((App) -> Unit)? = null

    private val mOnProhibitClickListener = View.OnClickListener {
        onProhibitListener?.invoke(it.tag as App)
    }

    private val mOnAllowClickListener = View.OnClickListener {
        onAllowListener?.invoke(it.tag as App)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        return KtViewHolder(inflater.inflate(R.layout.apps_item_app_approval, parent, false))
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, item: App) {
        ImageLoaderFactory.getImageLoader().displayAppIcon(host, viewHolder.ivAppsItemAppIcon, item.soft_icon)
        viewHolder.tvAppsItemAppName.text = item.soft_name
        viewHolder.tvAppsItemAppCategory.text =  item.type_name.emptyElse(getString(R.string.unknow))
        viewHolder.tvAppsItemProhibit.setOnClickListener(mOnProhibitClickListener)
        viewHolder.tvAppsItemProhibit.tag = item
        viewHolder.tvAppsItemAllow.setOnClickListener(mOnAllowClickListener)
        viewHolder.tvAppsItemAllow.tag = item
    }

}