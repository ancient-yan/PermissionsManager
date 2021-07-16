package com.gwchina.parent.apps.presentation.guide

import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.android.base.kotlin.visibleOrInvisible
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.common.checkedTypeName
import com.gwchina.parent.apps.common.isMustFreeUsable
import com.gwchina.parent.apps.data.App
import com.gwchina.sdk.base.utils.displayAppIcon
import com.kyleduo.switchbutton.SwitchButton
import kotlinx.android.synthetic.main.apps_item_app_with_switch.*


class GuideFlowAppsAdapter(private val host: Fragment, private val visibleSwitch: Boolean = true) : RecyclerAdapter<App, KtViewHolder>(host.requireContext()) {

    private val _checkedList = mutableListOf<App>()

    val checkedList: List<App>
        get() = _checkedList

    var checkIsLimitCount : (() -> Boolean) = { false }

    private val _onCheckedChangeListener = CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
        if (isChecked) {
            if (checkIsLimitCount.invoke()) {
                (buttonView as SwitchButton).setCheckedImmediatelyNoEvent(!isChecked)
            } else {
                _checkedList.add(buttonView.tag as App)
            }
        } else {
            _checkedList.remove(buttonView.tag as App)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.apps_item_app_with_switch, parent, false)
        return KtViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val item = getItem(position)
        ImageLoaderFactory.getImageLoader().displayAppIcon(host, viewHolder.ivAppsItemAppIcon, item.soft_icon)
        val result = SpanUtils().append(item.soft_name?:"").append("(必选)").setForegroundColor(ContextCompat.getColor(host.requireContext(), R.color.gray_level2)).create()
        viewHolder.tvAppsItemAppName.text = if (item.isMustFreeUsable()) result else item.soft_name
        viewHolder.tvAppsItemAppCategory.text = item.checkedTypeName()
        //自由可用 系统默认的给一个默认图标
        if (visibleSwitch && item.isMustFreeUsable()) {
            viewHolder.imageView.visible()
        } else {
            viewHolder.imageView.invisible()
        }
        viewHolder.btnAppsItemSwitch.tag = item
        viewHolder.btnAppsItemSwitch.visibleOrInvisible(visibleSwitch && !item.isMustFreeUsable())
        viewHolder.btnAppsItemSwitch.setCheckedImmediatelyNoEvent(checkedList.contains(item))
        viewHolder.btnAppsItemSwitch.setOnCheckedChangeListener(_onCheckedChangeListener)
    }
}