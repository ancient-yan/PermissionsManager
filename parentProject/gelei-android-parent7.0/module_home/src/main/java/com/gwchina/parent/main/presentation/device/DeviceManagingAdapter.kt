package com.gwchina.parent.main.presentation.device

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.android.base.kotlin.visibleOrInvisible
import com.android.base.utils.android.ResourceUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.utils.mapChildAvatarSmall
import kotlinx.android.synthetic.main.bound_device_item_child.*
import kotlinx.android.synthetic.main.bound_device_item_device_mamaging.*


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-17 10:29
 */
class ChildItemViewBinder : ItemViewBinder<Child, KtViewHolder>() {

    var isMember = false
    var onAddNewDeviceListener: ((Child) -> Unit)? = null

    private val mItemClickListener = View.OnClickListener {
        onAddNewDeviceListener?.invoke(it.tag as Child)
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: Child) {
        holder.ivDeviceItemChildName.text = ResourceUtils.getString(R.string.devices_of_x_mask, item.nick_name)
        holder.ivDeviceItemChildAvatar.setImageResource(mapChildAvatarSmall(item.sex))

        holder.tvDeviceItemAddDevice.visibleOrInvisible(!item.reachMaxDeviceCount() && isMember)
        holder.tvDeviceItemAddDevice.tag = item
        holder.tvDeviceItemAddDevice.setOnClickListener(mItemClickListener)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup) =
            KtViewHolder(inflater.inflate(R.layout.bound_device_item_child, parent, false))

}

internal class DeviceItemViewBinder : ItemViewBinder<Device, KtViewHolder>() {

    var onUnbindDeviceListener: ((Device) -> Unit)? = null

    private val mItemClickListener = View.OnClickListener {
        onUnbindDeviceListener?.invoke(it.tag as Device)
    }

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup) =
            KtViewHolder(inflater.inflate(R.layout.bound_device_item_device_mamaging, parent, false))

    override fun onBindViewHolder(holder: KtViewHolder, item: Device) {
        holder.tvDeviceItemDeviceName.text = item.device_name
        holder.tvDeviceItemUnbind.tag = item
        if (item.index >= 1) {
            holder.tvDeviceItemDeviceIndex.visible()
            holder.tvDeviceItemDeviceIndex.text = item.index.toString()
        } else {
            holder.tvDeviceItemDeviceIndex.gone()
        }
        holder.tvDeviceItemUnbind.setOnClickListener(mItemClickListener)
    }

}