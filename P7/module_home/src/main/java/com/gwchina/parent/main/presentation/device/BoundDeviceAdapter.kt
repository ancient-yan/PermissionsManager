package com.gwchina.parent.main.presentation.device

import android.content.Context
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.data.api.DEVICE_STATUS_ONLINE
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.utils.mapGuardLevelName
import kotlinx.android.synthetic.main.bound_device_item_device.*

internal class BoundDeviceAdapter(context: Context) : SimpleRecyclerAdapter<Device>(context) {

    var onUnbindListener: ((Device) -> Unit)? = null
    var onSwitchGuardLevelListener: ((Device) -> Unit)? = null

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.bound_device_item_device

    override fun bind(viewHolder: KtViewHolder, item: Device) {
        viewHolder.tvDeviceItemUnbind.setOnClickListener {
            onUnbindListener?.invoke(item)
        }

        viewHolder.llDeviceItemSwitchGuardLevel.setOnClickListener {
            onSwitchGuardLevelListener?.invoke(item)
        }

        viewHolder.tvDeviceItemDeviceName.text = item.device_name
        if (item.index >= 1) {
            viewHolder.tvDeviceItemDeviceIndex.visible()
            viewHolder.tvDeviceItemDeviceIndex.text = (item.index).toString()
        } else {
            viewHolder.tvDeviceItemDeviceIndex.gone()
        }

        viewHolder.tvDeviceItemSwitchGuardLevel.text = mapGuardLevelName(item.guard_level)

        if (isMemberGuardExpired(item.status)) {
            viewHolder.tvDeviceItemDeviceStatus.setTextColor(mContext.colorFromId(R.color.red_level1))
            viewHolder.tvDeviceItemDeviceStatus.text = mContext.getString(R.string.guard_expired)
        } else {
            if (item.on_line_flag == DEVICE_STATUS_ONLINE) {
                viewHolder.tvDeviceItemDeviceStatus.setTextColor(mContext.colorFromId(R.color.green_main))
                viewHolder.tvDeviceItemDeviceStatus.text = mContext.getText(R.string.in_normal_control)
            } else {
                viewHolder.tvDeviceItemDeviceStatus.setTextColor(mContext.colorFromId(R.color.gray_disable))
                viewHolder.tvDeviceItemDeviceStatus.text = mContext.getString(R.string.device_offline_already)
            }
        }
    }

}