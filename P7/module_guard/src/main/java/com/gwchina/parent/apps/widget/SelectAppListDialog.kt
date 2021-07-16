package com.gwchina.parent.apps.widget

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.dip
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.android.base.utils.android.ViewUtils
import com.android.base.utils.android.WindowUtils
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.common.*
import com.gwchina.parent.apps.data.App
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.displayAppIcon
import com.gwchina.sdk.base.widget.dialog.showListDialog
import kotlinx.android.synthetic.main.apps_dialog_item_select_app.*
import kotlinx.android.synthetic.main.apps_dialog_item_select_app.view.*

/*it is same as Java's static field*/
private var maxSize = 0

@SuppressLint("InflateParams")
internal fun Fragment.showSelectAppsDialog(selectableList: List<App>, confirmActionText: String? = null,
                                           onSelectedAppList: (List<App>) -> Unit, onSelectionListener: ((currentApp: App, selectinMap: HashMap<String, App>) -> Boolean)? = null) {

    //预先测量
    if (maxSize == 0) {
        val maxWindowSize = (0.8 * ScreenUtils.getScreenWidth()).toInt()
        val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.apps_dialog_item_select_app, null)
        itemView.tvAppsDialogItemCategory.setText(R.string.free_usable)
        itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        ViewUtils.measureWithSize(itemView, maxWindowSize, WindowUtils.getScreenHeight())
        maxSize = maxWindowSize - itemView.measuredWidth - itemView.ivAppsDialogItemChoice.measuredWidth - dip(5)
    }


    showListDialog {
        title = confirmActionText

        maxWidthPercent = 0.8F

        val selectAppsAdapter = SelectAppsAdapter(this@showSelectAppsDialog, selectableList, maxSize)
        selectAppsAdapter.onSelectionListener = onSelectionListener
        adapter = selectAppsAdapter

        positiveListener = { _, _ ->
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_GROUP_BTN_ADDSOFT)
            onSelectedAppList(selectAppsAdapter.selectedItem)
        }

        onDialogPreparedListener = { _, dialogController ->
            dialogController.positiveEnable = false
            selectAppsAdapter.onSelectingStatusListener = {
                dialogController.positiveEnable = it
            }
        }

    }

}

internal class SelectAppsAdapter(
        private val host: Fragment,
        appList: List<App>,
        private val maxAppTextWidth: Int
) : RecyclerAdapter<App, KtViewHolder>(host.requireContext(), appList) {

    private val _selectedItem = HashMap<String, App>()

    private val freeIndicatorDrawable by lazy {
        ContextCompat.getDrawable(mContext, R.drawable.guard_shape_app_category)
    }

    private val disableIndicatorDrawable by lazy {
        createIndicatorDrawable(R.color.opacity_10_red_level1)
    }

    private val limitedIndicatorDrawable by lazy {
        createIndicatorDrawable(R.color.opacity_10_yellow_level1)
    }

    private fun createIndicatorDrawable(color: Int): Drawable? {
        val drawable = ContextCompat.getDrawable(mContext, R.drawable.guard_shape_app_category)
        val mutate = drawable?.mutate()
        (mutate as? GradientDrawable)?.setColor(ContextCompat.getColor(mContext, color))
        return mutate
    }

    private val onClickListener = View.OnClickListener {
        val app = it.tag as App
        if (_selectedItem.containsKey(app.rule_id)) {
            _selectedItem.remove(app.rule_id)
        } else {
            if (onSelectionListener != null && onSelectionListener!!.invoke(app, _selectedItem)) {
                return@OnClickListener
            }
            _selectedItem[app.rule_id] = app
        }
        notifyItemChanged(getItemPosition(app))
        onSelectingStatusListener?.invoke(_selectedItem.isNotEmpty())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        return KtViewHolder(LayoutInflater.from(mContext).inflate(R.layout.apps_dialog_item_select_app, parent, false)).apply {
            tvAppsDialogItemAppName.maxWidth = maxAppTextWidth
        }
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val item = this.getItem(position)
        ImageLoaderFactory.getImageLoader().displayAppIcon(host, viewHolder.tvAppsDialogItemAppIcon, item.soft_icon)
        viewHolder.tvAppsDialogItemAppName.text = item.soft_name
        viewHolder.tvAppsDialogItemAppCategory.text = item.checkedTypeName()
        viewHolder.ivAppsDialogItemChoice.isSelected = _selectedItem.containsKey(item.rule_id)

        if (item.isMustFreeUsable()) {
            viewHolder.itemView.setOnClickListener(null)
        } else {
            viewHolder.itemView.tag = item
            viewHolder.itemView.setOnClickListener(onClickListener)
        }

        when {
            item.rule_type.isDisabled() -> {
                viewHolder.tvAppsDialogItemCategory.visible()
                viewHolder.tvAppsDialogItemCategory.setText(R.string.disabled)
                viewHolder.tvAppsDialogItemCategory.setTextColor(ContextCompat.getColor(mContext, R.color.red_level1))
                viewHolder.tvAppsDialogItemCategory.background = disableIndicatorDrawable
            }
            item.rule_type.isFreeUsable() -> {
                viewHolder.tvAppsDialogItemCategory.visible()
                viewHolder.tvAppsDialogItemCategory.setText(R.string.free_usable)
                viewHolder.tvAppsDialogItemCategory.setTextColor(ContextCompat.getColor(mContext, R.color.green_main))
                viewHolder.tvAppsDialogItemCategory.background = freeIndicatorDrawable
            }
            item.rule_type.isLimitedUsable() -> {
                viewHolder.tvAppsDialogItemCategory.visible()
                viewHolder.tvAppsDialogItemCategory.setText(R.string.limited_usable)
                viewHolder.tvAppsDialogItemCategory.setTextColor(ContextCompat.getColor(mContext, R.color.yellow_level1))
                viewHolder.tvAppsDialogItemCategory.background = limitedIndicatorDrawable
            }
            else -> {
                viewHolder.tvAppsDialogItemCategory.background = null
                viewHolder.tvAppsDialogItemCategory.invisible()
            }
        }

    }

    var onSelectingStatusListener: ((hasSelectedApp: Boolean) -> Unit)? = null

    var onSelectionListener: ((currentApp: App, selectioonSize: HashMap<String, App>) -> Boolean)? = null

    val selectedItem: List<App>
        get() {
            if (_selectedItem.isEmpty()) {
                return emptyList()
            }
            return items.filter {
                _selectedItem.containsKey(it.rule_id)
            }
        }

}