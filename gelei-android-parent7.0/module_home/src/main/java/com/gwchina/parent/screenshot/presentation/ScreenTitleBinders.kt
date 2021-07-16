package com.gwchina.parent.screenshot.presentation

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.home.R
import kotlinx.android.synthetic.main.home_remote_screentshot_date_item_layout.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-05 16:13
 *
 */
class ScreenTitleBinders(val host: Fragment) : ItemViewBinder<String, KtViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {


        return KtViewHolder(inflater.inflate(R.layout.home_remote_screentshot_date_item_layout, parent, false))
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: String) {
        holder.tvDate.text = item
    }
}

