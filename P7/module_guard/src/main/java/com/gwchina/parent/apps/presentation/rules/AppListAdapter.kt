package com.gwchina.parent.apps.presentation.rules

import android.arch.lifecycle.Observer
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.adapter.recycler.SmartViewHolder
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.*
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.common.checkedTypeName
import com.gwchina.parent.apps.common.isMustFreeUsable
import com.gwchina.parent.apps.data.App
import com.gwchina.sdk.base.utils.displayAppIcon

class AppListAdapter(
        private val host: Fragment,
        private val ruleTypeInfo: RuleTypeInfo
) : RecyclerAdapter<App, SmartViewHolder>(host.requireContext()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ADD = 1
        private const val TYPE_NORMAL = 2
        private const val HEADER_COUNT = 2
    }

    var canAddMore: Boolean = false

    var onAddAppListener: (() -> Unit)? = null

    var onAppClickedListener: ((App) -> Unit)? = null

    private var isObservable = false

    private val _onClickAddAppListener = View.OnClickListener {
        onAddAppListener?.invoke()
    }

    private val mOnAppClickedListener = View.OnClickListener {
        onAppClickedListener?.invoke(it.tag as App)
    }

    init {
        setHeaderSize { HEADER_COUNT }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmartViewHolder {
        return when (viewType) {
            TYPE_HEADER -> SmartViewHolder(LayoutInflater.from(mContext).inflate(R.layout.apps_header_item_binder_layout, parent, false))
            TYPE_ADD -> SmartViewHolder(LayoutInflater.from(mContext).inflate(R.layout.apps_item_limited_add_app, parent, false)).apply {
                with(helper()) {
                    getView<TextView>(R.id.tvAppsItemAddAppTitle).text = ruleTypeInfo.typeName
                    getView<TextView>(R.id.tvAppsItemAddApp).setOnClickListener(_onClickAddAppListener)

                    getView<TextView>(R.id.tvAppsItemEmptyAdd).text = mContext.getString(R.string.add_x_mask, ruleTypeInfo.typeName)
                    getView<TextView>(R.id.tvAppsItemEmptyAdd).setOnClickListener(_onClickAddAppListener)

                    getView<ImageView>(R.id.tvAppsItemEmptyIcon).setImageResource(ruleTypeInfo.emptyAppsIcon)

                    getView<TextView>(R.id.tvAppsItemEmptyTips).text = ruleTypeInfo.emptyAppsText
                    getView<TextView>(R.id.ivAppsAddAppDivider).gone()
                }
            }
            else -> SmartViewHolder(LayoutInflater.from(mContext).inflate(R.layout.apps_item_app_with_arrow, parent, false))
        }
    }

    override fun onBindViewHolder(viewHolder: SmartViewHolder, position: Int) {
        when {
            viewHolder.itemViewType == TYPE_HEADER -> {
                if (!isObservable)
                    (host as BaseAppListFragment).appRulesViewModel.deviceFlagName
                            .observe(host, Observer {
                                isObservable = true
                                viewHolder.helper().getView<TextView>(R.id.tvAppsDeviceFlag).visibleOrGone(!it.isNullOrEmpty())
                                viewHolder.helper().getView<TextView>(R.id.tvAppsDeviceFlag).text = it
                            })
            }
            viewHolder.itemViewType == TYPE_ADD -> bindHeader(viewHolder)
            viewHolder.itemViewType == TYPE_NORMAL -> bindAppView(viewHolder, position)
        }
    }

    private fun bindAppView(viewHolder: SmartViewHolder, position: Int) {
        val helper = viewHolder.helper()
        val item = getItem(position)

        ImageLoaderFactory.getImageLoader().displayAppIcon(host, helper.getView(R.id.ivAppsItemWithArrowAppIcon), item.soft_icon)
        helper.getView<TextView>(R.id.tvAppsItemWithArrowAppName).text = item.soft_name
        helper.getView<TextView>(R.id.tvAppsItemWithArrowCategory).text = item.checkedTypeName()

        if (item.isMustFreeUsable()) {
            helper.getView<TextView>(R.id.tvAppsItemWithArrowDesc).invisible()
            viewHolder.itemView.setOnClickListener(null)
        } else {
            helper.getView<TextView>(R.id.tvAppsItemWithArrowDesc).visible()
            viewHolder.itemView.tag = item
            viewHolder.itemView.setOnClickListener(mOnAppClickedListener)
        }
    }

    private fun bindHeader(viewHolder: SmartViewHolder) {
        val addAppTv = viewHolder.helper().getView<TextView>(R.id.tvAppsItemAddApp)

        if (canAddMore) {
            addAppTv.leftDrawable(R.drawable.icon_add)
            addAppTv.setTextColor(mContext.colorFromId(R.color.green_main))
        } else {
            addAppTv.leftDrawable(R.drawable.icon_add_disable)
            addAppTv.setTextColor(mContext.colorFromId(R.color.gray_disable))
        }

        viewHolder.helper().getView<View>(R.id.groupAppsItemEmptyViews).visibleOrGone(itemCount <= HEADER_COUNT)
        viewHolder.helper().getView<View>(R.id.tvAppsItemEmptyAdd).visibleOrGone(itemCount <= HEADER_COUNT)
        viewHolder.helper().getView<View>(R.id.groupAppsItemAddingViews).visibleOrGone(itemCount > HEADER_COUNT)
    }

    override fun getItemCount(): Int {
        return HEADER_COUNT + super.getItemCount()
    }

    override fun getItemViewType(position: Int) =
            when (position) {
                0 -> TYPE_HEADER
                1 -> TYPE_ADD
                else -> TYPE_NORMAL
            }

}