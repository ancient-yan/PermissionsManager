package com.gwchina.sdk.base.widget.dialog

import android.app.Dialog
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.app.base.R
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.Device
import kotlinx.android.synthetic.main.dialog_switch_device_item.*

/**展示一个选择对应孩子设备的弹框*/
fun showSwitchDeviceDialog(
        context: Context?,
        child: Child?,
        selectedDevice: Device?,
        expiredEnable: Boolean = false,
        onSelectedDevice: (Device) -> Unit,
        onCancel: (() -> Unit)? = null
): Dialog? {
    if (context == null || child == null || selectedDevice == null) {
        return null
    }
    val deviceList = child.device_list ?: return null
    val selectPosition = deviceList.indexOfFirst { it.device_id == selectedDevice.device_id }

    return showBottomSheetListDialog(context) {
        actionText = context.getText(R.string.cancel_)
        titleText = context.getString(R.string.devices_of_x_mask, child.nick_name)
        customList = { dialog, rv ->
            buildDeviceList(rv, context, child, dialog, onSelectedDevice, selectPosition, expiredEnable)
        }
        if (onCancel != null) {
            actionListener = onCancel
        }
    }

}

private fun buildDeviceList(rv: RecyclerView, context: Context, child: Child, dialog: Dialog, onSelectedDevice: (Device) -> Unit, selectPosition: Int, expiredEnable: Boolean) {
    rv.layoutManager = LinearLayoutManager(context)
    rv.adapter = DeviceAdapter(context, child, selectPosition, expiredEnable) {
        dialog.dismiss()
        onSelectedDevice(it)
    }
}

private class DeviceAdapter(
        context: Context,
        child: Child,
        private val selectPosition: Int,
        private val expiredEnable: Boolean,
        private val onSelectDevice: (Device) -> Unit
) : SimpleRecyclerAdapter<Device>(context, child.device_list) {

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.dialog_switch_device_item

    override fun bind(viewHolder: KtViewHolder, item: Device) {
        viewHolder.tvDialogSwitchDeviceName.text = item.device_name

        if (item.index > 0) {
            viewHolder.tvDialogSwitchDeviceIndex.visible()
            viewHolder.tvDialogSwitchDeviceIndex.text = (item.index).toString()
        } else {
            viewHolder.tvDialogSwitchDeviceIndex.gone()
        }

        if (selectPosition == viewHolder.adapterPosition) {
            viewHolder.ivDialogSwitchDeviceItemTick.visible()
        } else {
            viewHolder.ivDialogSwitchDeviceItemTick.gone()
        }

        if (isMemberGuardExpired(item.status)) {

            viewHolder.tvDialogSwitchDeviceDisable.visible()
            viewHolder.tvDialogSwitchDeviceName.setTextColor(mContext.colorFromId(R.color.gray_disable))

            if (expiredEnable) {
                viewHolder.itemView.setOnClickListener { onSelectDevice(item) }
            } else {
                viewHolder.itemView.setOnClickListener(null)
            }

        } else {

            viewHolder.itemView.setOnClickListener { onSelectDevice(item) }

            viewHolder.tvDialogSwitchDeviceDisable.gone()
            if (selectPosition == viewHolder.adapterPosition) {
                viewHolder.tvDialogSwitchDeviceName.setTextColor(mContext.colorFromId(R.color.green_main))
            } else {
                viewHolder.tvDialogSwitchDeviceName.setTextColor(mContext.colorFromId(R.color.gray_level2))
            }
        }

    }

}