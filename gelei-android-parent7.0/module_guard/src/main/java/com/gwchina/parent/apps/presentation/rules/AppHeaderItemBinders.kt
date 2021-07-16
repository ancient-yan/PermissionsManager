package com.gwchina.parent.apps.presentation.rules

import android.arch.lifecycle.Observer
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.visibleOrGone
import com.gwchina.lssw.parent.guard.R
import kotlinx.android.synthetic.main.apps_header_item_binder_layout.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-05 16:13
 *      守护应用的头部：包含公共头部的多设备提示、限时可用的添加分组、高危应用的提示
 */
class AppHeaderItemBinders(val host: Fragment) : ItemViewBinder<HeaderData, KtViewHolder>() {

    var appsItemAddGroup: (() -> Unit)? = null
    var appsItemWhatIsGroup: (() -> Unit)? = null
    private var isObservable = false

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        return KtViewHolder(inflater.inflate(R.layout.apps_header_item_binder_layout, parent, false))
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: HeaderData) {
        holder.llAppsAddGroup.visibleOrGone(item.showAddGroupLayout)
        holder.tvAppsItemWhatIsGroup.visibleOrGone(item.showWhatIsGroup)
        holder.tvAppsRiskAppTips.visibleOrGone(item.showAppsRiskAppTips)
        holder.tvAppsItemAddGroup.setOnClickListener {
            appsItemAddGroup?.invoke()
        }
        holder.tvAppsItemWhatIsGroup.setOnClickListener {
            appsItemWhatIsGroup?.invoke()
        }
        if (host is LimitedAppListFragment) {
            if (!isObservable) {
                host.appRulesViewModel.deviceFlagName
                        .observe(host, Observer {
                            isObservable = true
                            holder.tvAppsDeviceFlag.visibleOrGone(!it.isNullOrEmpty())
                            holder.tvAppsDeviceFlag.text = it
                        })
            }
        } else if (host is RiskAppListFragment) {
            if (!isObservable) {
                host.appRulesViewModel.deviceFlagName
                        .observe(host, Observer {
                            isObservable = true
                            holder.tvAppsDeviceFlag.visibleOrGone(!it.isNullOrEmpty())
                            holder.tvAppsDeviceFlag.text = it
                        })
            }
        }
    }
}

data class HeaderData(var showWhatIsGroup: Boolean, var showAddGroupLayout: Boolean, var showAppsRiskAppTips: Boolean = false)