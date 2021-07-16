package com.gwchina.parent.profile.presentation.child

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.kotlin.*
import com.gwchina.lssw.parent.user.R
import com.gwchina.sdk.base.data.api.isFlagPositive
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.api.isSevereMode
import com.gwchina.sdk.base.data.api.isYes
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.TempTimePeriodRule
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.setTextColorResId
import kotlinx.android.synthetic.main.profile_child_device_item.*

internal class ChildDeviceStateAdapter(context: Context) : SimpleRecyclerAdapter<Device>(context) {

    var onOppen: ((Device) -> Unit)? = null
    var onClose: ((String, TempTimePeriodRule) -> Unit)? = null

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.profile_child_device_item

    override fun bind(viewHolder: KtViewHolder, item: Device) {
        if (isMemberGuardExpired(item.status)) {
            viewHolder.tvDeviceItemDeviceStatus.gone()
            viewHolder.tvDeviceItemDeviceIndex.gone()
            viewHolder.llChildDeviceItem.gone()
            viewHolder.tvChildDeviceItemNormal.visible()
            viewHolder.tvChildDeviceItemNormal.text = mContext.getString(R.string.guard_expired)
            viewHolder.tvChildDeviceItemNormal.setTextColorResId(R.color.red_level1)
            viewHolder.tvDeviceItemDeviceName.text = item.device_name.foldText(7)
        } else {
            if (item.guard_level.isSevereMode()) {
                viewHolder.tvChildDeviceItemNormal.gone()
                viewHolder.llChildDeviceItem.visible()
                viewHolder.tvDeviceItemDeviceStatus.visible()

                item.temp_usable_time.ifNonNull {
                    viewHolder.sbChildDevice.setCheckedNoEvent(true)
                    //mode:1临时锁屏
                    if (isFlagPositive(mode)) {
                        viewHolder.ivChildDeviceIcon.setImageResource(R.drawable.icon_lock_home)
                        viewHolder.tvChildDeviceSwitchText.setText(R.string.close_look_screen)

                        viewHolder.tvDeviceItemDeviceStatus.setText(R.string.temp_lock_screen_ing)
                        viewHolder.tvDeviceItemDeviceStatus.setTextColorResId(R.color.yellow_level2)
                    } else {
                        viewHolder.ivChildDeviceIcon.setImageResource(R.drawable.icon_jiesuo_home)
                        viewHolder.tvChildDeviceSwitchText.setText(R.string.close_look_usable)

                        viewHolder.tvDeviceItemDeviceStatus.setText(R.string.temp_availability_ing)
                        viewHolder.tvDeviceItemDeviceStatus.setTextColorResId(R.color.green_level1)
                    }
                }

                item.temp_usable_time.ifNull {
                    viewHolder.sbChildDevice.setCheckedNoEvent(false)
                    if (isYes(item.rule_time_flag) && item.surplus_used_time > 0) {
                        viewHolder.ivChildDeviceIcon.setImageResource(R.drawable.icon_lock_home)
                        viewHolder.tvChildDeviceSwitchText.setText(R.string.temporary_look_screen)

                        viewHolder.tvDeviceItemDeviceStatus.setText(R.string.not_look_screen)
                        viewHolder.tvDeviceItemDeviceStatus.setTextColorResId(R.color.yellow_level2)
                    } else {
                        viewHolder.ivChildDeviceIcon.setImageResource(R.drawable.icon_jiesuo_home)
                        viewHolder.tvChildDeviceSwitchText.setText(R.string.temporarily_usable)

                        viewHolder.tvDeviceItemDeviceStatus.setText(R.string.already_look_screen)
                        viewHolder.tvDeviceItemDeviceStatus.setTextColorResId(R.color.gray_level2)
                    }
                }

                viewHolder.sbChildDevice.setOnCheckedChangeListener { _, isChecked ->
                    viewHolder.sbChildDevice.setCheckedNoEvent(!isChecked)
                    if (isChecked) {
                        //开启临时可用、临时锁屏
                        onOppen?.invoke(item)
                    } else {
                        //关闭临时可用、临时锁屏
                        item.temp_usable_time.ifNonNull {
                            onClose?.invoke(item.device_id, this)
                        }
                    }
                }
            } else {
                viewHolder.tvChildDeviceItemNormal.visible()
                viewHolder.tvChildDeviceItemNormal.text = mContext.getString(R.string.guarding_normally)
                viewHolder.tvChildDeviceItemNormal.setTextColor(ContextCompat.getColor(mContext, R.color.green_level1))
                viewHolder.llChildDeviceItem.gone()
                viewHolder.tvDeviceItemDeviceStatus.gone()
            }

            viewHolder.tvDeviceItemDeviceName.text = item.device_name.foldText(7)
            if (item.index >= 1) {
                viewHolder.tvDeviceItemDeviceIndex.visible()
                viewHolder.tvDeviceItemDeviceIndex.text = (item.index).toString()
            } else {
                viewHolder.tvDeviceItemDeviceIndex.gone()
            }
        }

    }
}