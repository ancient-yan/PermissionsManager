package com.gwchina.parent.apps.presentation.rules

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.*
import com.android.base.utils.android.TintUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.data.App
import com.gwchina.sdk.base.utils.displayAppIcon
import com.gwchina.sdk.base.utils.foldText
import kotlinx.android.synthetic.main.apps_item_limited_add_app.*
import kotlinx.android.synthetic.main.apps_item_limited_add_group.*
import kotlinx.android.synthetic.main.apps_item_limited_app.*
import kotlinx.android.synthetic.main.apps_item_limited_app_group.*

internal const val LIMITED_ADD_GROUP_ITEM_BINDER_TYPE = "limited_add_group_item_binder_type"
internal const val LIMITED_ADD_APP_ITEM_BINDER_TYPE = "limited_add_app_item_binder_type"

data class LimitedTypeHeader(val type: String, val isTypeEmpty: Boolean, val canAddMore: Boolean)

internal class LimitedAddGroupItemBinder(
        private val host: Fragment,
        private val onAddGroupListener: () -> Unit
) : ItemViewBinder<LimitedTypeHeader, KtViewHolder>() {

    private val clickListener = View.OnClickListener {
        onAddGroupListener()
    }

    private val grayAddDrawable by lazy {
        TintUtils.tint(host.drawableFromId(R.drawable.icon_add)?.mutate(), host.colorFromId(R.color.gray_disable))
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        return KtViewHolder(inflater.inflate(R.layout.apps_item_limited_add_group, parent, false))
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: LimitedTypeHeader) {
        holder.tvAppsItemAddGroupGreen.setOnClickListener(clickListener)

        if (item.canAddMore) {
            holder.tvAppsItemAddGroupGreen.setTextColor(host.colorFromId(R.color.green_main))
            holder.tvAppsItemAddGroupGreen.leftDrawable(R.drawable.icon_add)
        } else {
            holder.tvAppsItemAddGroupGreen.setTextColor(host.colorFromId(R.color.gray_disable))
            holder.tvAppsItemAddGroupGreen.leftDrawable(grayAddDrawable)
        }
    }

}

internal class LimitedGroupItemBinder(
        private val host: Fragment,
        onGroupClickListener: (GroupWrapper) -> Unit
) : ItemViewBinder<GroupWrapper, KtViewHolder>() {

    private val mClickListener = View.OnClickListener {
        onGroupClickListener(it.tag as GroupWrapper)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        return KtViewHolder(inflater.inflate(R.layout.apps_item_limited_app_group, parent, false))
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: GroupWrapper) {
        holder.ivAppsItemGroupIcon.display(host, item.groupIcons)
        holder.tvAppsItemGroupName.text = item.appGroup.soft_group_name.foldText(6)
        holder.tvAppsItemGroupDetail.text = item.groupDetails
        holder.tvAppsItemGroupUsableTime.text = item.groupUsedTime
        holder.ivAppsItemGroupTimeProgress.max = item.appGroup.group_used_time_perday
        holder.ivAppsItemGroupTimeProgress.progress = item.appGroup.used_time
        holder.itemView.tag = item
        holder.itemView.setOnClickListener(mClickListener)
    }

}

internal class LimitedAppItemBinder(
        private val host: Fragment,
        onAppClickListener: (App) -> Unit
) : ItemViewBinder<AppWrapper, KtViewHolder>() {

    private val mClickListener = View.OnClickListener {
        onAppClickListener(it.tag as App)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        return KtViewHolder(inflater.inflate(R.layout.apps_item_limited_app, parent, false))
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: AppWrapper) {
        ImageLoaderFactory.getImageLoader().displayAppIcon(host, holder.ivAppsItemAppIcon, item.app.soft_icon)
        holder.tvAppsItemAppName.text = item.appNameWithType
        holder.tvAppsItemAppUsableTime.text = item.appUsedTime
        holder.ivAppsItemAppTimeProgress.max = item.app.used_time_perday
        holder.ivAppsItemAppTimeProgress.progress = item.app.used_time
        holder.itemView.tag = item.app
        holder.itemView.setOnClickListener(mClickListener)
    }

}

internal class LimitedAddAppItemBinder(
        private val host: Fragment,
        private val onAddLimitedAppListener: () -> Unit
) : ItemViewBinder<LimitedTypeHeader, KtViewHolder>() {

    private val _onClickListener = View.OnClickListener {
        onAddLimitedAppListener()
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        val ktViewHolder = KtViewHolder(inflater.inflate(R.layout.apps_item_limited_add_app, parent, false))

        ktViewHolder.tvAppsItemAddAppTitle.setText(R.string.limited_usable)
        ktViewHolder.tvAppsItemAddApp.setOnClickListener(_onClickListener)

        ktViewHolder.tvAppsItemEmptyTips.setText(R.string.no_limited_usable_apps_tips)
        ktViewHolder.tvAppsItemEmptyIcon.setImageResource(R.drawable.app_img_no_limited)

        ktViewHolder.tvAppsItemEmptyAdd.text = parent.context.getString(R.string.add_x_mask, parent.context.getString(R.string.limited_usable))
        ktViewHolder.tvAppsItemEmptyAdd.setOnClickListener(_onClickListener)

        return ktViewHolder
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: LimitedTypeHeader) {
        holder.groupAppsItemEmptyViews.visibleOrGone(item.isTypeEmpty)
        holder.tvAppsItemEmptyAdd.visibleOrGone(item.isTypeEmpty)
        holder.groupAppsItemAddingViews.visibleOrGone(!item.isTypeEmpty)
        holder.ivAppsAddAppDivider.visibleOrGone(!item.isTypeEmpty)

        if (item.canAddMore) {
            holder.tvAppsItemAddApp.leftDrawable(R.drawable.icon_add)
            holder.tvAppsItemAddApp.setTextColor(host.colorFromId(R.color.green_main))
        } else {
            holder.tvAppsItemAddApp.leftDrawable(R.drawable.icon_add_disable)
            holder.tvAppsItemAddApp.setTextColor(host.colorFromId(R.color.gray_disable))
        }
    }

}