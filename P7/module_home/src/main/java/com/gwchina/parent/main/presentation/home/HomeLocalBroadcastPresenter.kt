package com.gwchina.parent.main.presentation.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.android.base.kotlin.gone
import com.android.base.kotlin.isVisible
import com.android.base.kotlin.visible
import com.android.base.utils.android.ResourceUtils.getString
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.data.models.currentChildDeviceId
import com.gwchina.sdk.base.sync.SyncManager
import kotlinx.android.synthetic.main.home_fragment.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-26 18:29
 */
class HomeLocalBroadcastPresenter(val host:HomeFragment) {


     val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceId = intent?.getStringExtra(SyncManager.DEVICE_ID)
            val currentDeviceId = host.viewModel.appDataSource.user().currentChildDeviceId

            val actionApp = SyncManager.getInstance().intervalHandlerMap["$deviceId-$INSTRUCTION_SYNC_APP"]?.childDeviceId
            val actionPhone = SyncManager.getInstance().intervalHandlerMap["$deviceId-$INSTRUCTION_SYNC_PHONE"]?.childDeviceId
            val actionTime = SyncManager.getInstance().intervalHandlerMap["$deviceId-$INSTRUCTION_SYNC_TIME"]?.childDeviceId
            val actionURL = SyncManager.getInstance().intervalHandlerMap["$deviceId-$INSTRUCTION_SYNC_URL"]?.childDeviceId
            val actionLEVEL = SyncManager.getInstance().intervalHandlerMap["$deviceId-$INSTRUCTION_SYNC_LEVEL"]?.childDeviceId
            val state = intent?.getIntExtra(SyncManager.SYNC_STATE, 0)
            when (intent?.action) {
                "$actionApp-$INSTRUCTION_SYNC_APP" -> {
                    if (actionApp != currentDeviceId) return
                    when (state) {
                        1 -> host.instructionStateCard.mIsvHomeApp.showSyncingState(getString(R.string.instruction_app_name))
                        2 -> host.instructionStateCard.mIsvHomeApp.showSyncFailedState(getString(R.string.instruction_app_name), host.deviceName)
                        3 -> host.instructionStateCard.mIsvHomeApp.gone()
                    }
                }
                "$actionPhone-$INSTRUCTION_SYNC_PHONE" -> {
                    if (actionPhone != currentDeviceId) return
                    when (state) {
                        1 -> host.instructionStateCard.mIsvHomePhone.showSyncingState(getString(R.string.instruction_phone_name))
                        2 -> host.instructionStateCard.mIsvHomePhone.showSyncFailedState(getString(R.string.instruction_phone_name), host.deviceName)
                        3 -> host.instructionStateCard.mIsvHomePhone.gone()
                    }
                }
                "$actionTime-$INSTRUCTION_SYNC_TIME" -> {
                    if (actionTime != currentDeviceId) return
                    when (state) {
                        1 -> host.instructionStateCard.mIsvHomeTime.showSyncingState(getString(R.string.instruction_time_name))
                        2 -> host.instructionStateCard.mIsvHomeTime.showSyncFailedState(getString(R.string.instruction_time_name), host.deviceName)
                        3 -> host.instructionStateCard.mIsvHomeTime.gone()
                    }
                }
                "$actionURL-$INSTRUCTION_SYNC_URL" -> {
                    if (actionURL != currentDeviceId) return
                    when (state) {
                        1 -> host.instructionStateCard.mIsvHomeNet.showSyncingState(getString(R.string.instruction_net_name))
                        2 -> host.instructionStateCard.mIsvHomeNet.showSyncFailedState(getString(R.string.instruction_net_name), host.deviceName)
                        3 -> host.instructionStateCard.mIsvHomeNet.gone()
                    }
                }
                "$actionLEVEL-$INSTRUCTION_SYNC_LEVEL" -> {
                    if (actionLEVEL != currentDeviceId) return
                    when (state) {
                        1 -> host.instructionStateCard.mIsvHomeLevel.showSyncingState(getString(R.string.instruction_level_name))
                        2 -> host.instructionStateCard.mIsvHomeLevel.showSyncFailedState(getString(R.string.instruction_level_name), host.deviceName)
                        3 -> host.instructionStateCard.mIsvHomeLevel.gone()
                    }
                }
            }
            var isAllChildViewGone = true
            for (i in 0 until host.instructionStateCard.childCount) {
                if (host.instructionStateCard.getChildAt(i).isVisible()) {
                    isAllChildViewGone = false
                }
            }
            if (isAllChildViewGone) host.instructionStateCard.gone() else host.instructionStateCard.visible()
        }
    }

     fun onDestroy() {
        SyncManager.getInstance().getLocalBroadcastManager(host.requireContext()).unregisterReceiver(localReceiver)
        SyncManager.getInstance().onDestroy()
    }
}