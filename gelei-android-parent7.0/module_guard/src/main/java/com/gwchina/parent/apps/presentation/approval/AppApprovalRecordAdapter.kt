package com.gwchina.parent.apps.presentation.approval

import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.leftDrawable
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.common.RULE_TYPE_DISABLE
import com.gwchina.parent.apps.common.RULE_TYPE_FREE
import com.gwchina.parent.apps.common.RULE_TYPE_LIMITED
import com.gwchina.sdk.base.utils.displayAppIcon
import kotlinx.android.synthetic.main.apps_item_app_record.*
import kotlinx.android.synthetic.main.apps_item_app_with_switch.ivAppsItemAppIcon
import kotlinx.android.synthetic.main.apps_item_app_with_switch.tvAppsItemAppName


class AppApprovalRecordAdapter(private val host: AppApprovalRecordFragment) : RecyclerAdapter<AppRecordWrapper, KtViewHolder>(host.requireContext()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.apps_item_app_record, parent, false)
        return KtViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val appRecordWrapper = getItem(position)
        val item = appRecordWrapper.app
        ImageLoaderFactory.getImageLoader().displayAppIcon(host, viewHolder.ivAppsItemAppIcon, item.soft_icon)
        viewHolder.tvAppsItemAppName.text = appRecordWrapper.appName
        viewHolder.tvAppsItemAppTime.text = appRecordWrapper.appCreatingTime

        when {
            item.rule_type == RULE_TYPE_FREE -> {
                viewHolder.tvAppsItemAppRecordDesc.leftDrawable(R.drawable.icon_status_normal)
                viewHolder.tvAppsItemAppRecordDesc.text = mContext.getString(R.string.already_add_to_list_mask, host.appGuardResource.getRuleTypeName(RULE_TYPE_FREE))
            }
            item.rule_type == RULE_TYPE_LIMITED -> {
                viewHolder.tvAppsItemAppRecordDesc.leftDrawable(R.drawable.icon_status_waiting)
                viewHolder.tvAppsItemAppRecordDesc.text = mContext.getString(R.string.already_add_to_list_mask, mContext.getString(R.string.limited_usable))
            }
            item.rule_type == RULE_TYPE_DISABLE -> {
                viewHolder.tvAppsItemAppRecordDesc.leftDrawable(R.drawable.icon_status_disable)
                viewHolder.tvAppsItemAppRecordDesc.text = mContext.getString(R.string.already_add_to_list_mask, mContext.getString(R.string.disabled))
            }
        }
    }

}
