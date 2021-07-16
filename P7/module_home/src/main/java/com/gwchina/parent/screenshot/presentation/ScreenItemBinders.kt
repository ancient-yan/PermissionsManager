package com.gwchina.parent.screenshot.presentation

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.visibleOrGone
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.daily.adapter.GlideImageLoader
import com.gwchina.parent.screenshot.data.ScreenshotData
import kotlinx.android.synthetic.main.home_remote_screentshot_item_layout.*
import java.text.SimpleDateFormat
import java.util.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-05 16:13
 *
 */
class ScreenItemBinders(val host: Fragment, private val showIndicator: Boolean) : ItemViewBinder<ScreenshotData, KtViewHolder>() {

    var onItemClickListener: ((ScreenshotData, Int) -> Unit)? = null

    private val simpleDataFormat = SimpleDateFormat("HH:mm:ss")

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        return KtViewHolder(inflater.inflate(R.layout.home_remote_screentshot_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: ScreenshotData) {
        holder.tvUploadTime.text = item.upload_time.toString()
        holder.ivIndicator.visibleOrGone(showIndicator)
        if (item.isSelected) holder.ivIndicator.setImageResource(R.drawable.home_icon_screenshot_delete_selected) else holder.ivIndicator.setImageResource(R.drawable.home_icon_screenshot_delete_normal)
        holder.tvUploadTime.text = simpleDataFormat.format(Date(item.upload_time))
        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(item, holder.adapterPosition)
        }
        ImageLoaderFactory.getImageLoader().display(holder.ivScreenShot, item.size_url)
    }
}
