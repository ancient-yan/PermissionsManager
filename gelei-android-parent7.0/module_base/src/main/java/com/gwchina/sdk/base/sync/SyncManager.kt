package com.gwchina.sdk.base.sync

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v4.content.LocalBroadcastManager
import com.android.base.rx.observeOnUI
import com.android.base.rx.subscribeIgnoreError
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.sdk.base.AppContext
import java.lang.ref.WeakReference

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-19 14:34
 */


class SyncManager private constructor() {

    private val instructionSyncService = AppContext.serviceManager().instructionSyncService

    private var mContext: Context? = null

    companion object {
        const val defaultTime = 60

        const val SYNC_STATE = "sync_state"

        const val DEVICE_ID = "deviceId"

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance: SyncManager? = null

        fun getInstance(): SyncManager = sInstance ?: synchronized(this) {
            sInstance ?: SyncManager().also { sInstance = it }
        }
    }

    enum class SyncState {
        //同步中
        SYNCING,
        //同步成功
        SYNC_SUCCESS,
        //同步失败
        SYNC_FAILED
    }

    /**
     * key  = "$childDeviceId-$instructionFlag"
     */
    val intervalHandlerMap = mutableMapOf<String, SyncData>()

    /**
     * 首页本地广播是否注册过
     */
    val localBroadcastRegisterMap = mutableMapOf<String, Boolean>()

    data class SyncData(val childUserId: String, val childDeviceId: String, val instructionFlag: String, var syncHandler: SyncHandler? = null)

    /**
     * Handler全部都在HomeFragment中创建
     */
    class SyncHandler(var time: Int, syncManager: SyncManager) : Handler() {

        private val weakReference = WeakReference<SyncManager>(syncManager)

        var syncState: SyncState? = null

        var syncData: SyncData? = null

        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if (msg?.what == 0) return
            val disposableManager = weakReference.get() ?: return
            if (syncData == null)
                syncData = disposableManager.getSyncDataByHandler(this) ?: return
            val instructionFlag = syncData!!.instructionFlag
            val childDeviceId = syncData!!.childDeviceId
            val childUserId = syncData!!.childUserId

            if (time == 0) {
                syncState = SyncState.SYNC_FAILED
                disposableManager.sendLocalBroadcast(disposableManager.mContext!!, instructionFlag, childDeviceId, SyncState.SYNC_FAILED)
                return
            }
            time--
            if (time.rem(5) == 0) {
                disposableManager.querySyncStateInterval(instructionFlag, childDeviceId, childUserId)
            } else {
                disposableManager.sendLocalBroadcast(disposableManager.mContext!!, instructionFlag, childDeviceId, SyncState.SYNCING)
                sendEmptyMessageDelayed(1, 1000)
            }
        }
    }

    private fun getSyncDataByHandler(syncHandler: SyncHandler): SyncData? {
        intervalHandlerMap.values.forEach {
            if (it.syncHandler == syncHandler) {
                return it
            }
        }
        return null
    }

    /**
     * 循环查询同步状态
     */
    private fun querySyncStateInterval(instructionFlag: String, childDeviceId: String, childUserId: String) {
        val key = "$childDeviceId-$instructionFlag"
        instructionSyncService.instructionSyncState(instructionFlag, childUserId, childDeviceId)
                .observeOnUI()
                .subscribeIgnoreError {
                    //同步成功
                    if (it.isSynced()) {
                        intervalHandlerMap[key]?.syncHandler?.syncState = SyncState.SYNC_SUCCESS
                        mContext?.let { it1 -> sendLocalBroadcast(it1, instructionFlag, childDeviceId, SyncState.SYNC_SUCCESS) }
                        intervalHandlerMap[key]?.syncHandler?.sendEmptyMessage(0)
                    } else {
                        intervalHandlerMap[key]?.syncHandler?.syncState = SyncState.SYNCING
                        mContext?.let { it1 -> sendLocalBroadcast(it1, instructionFlag, childDeviceId, SyncState.SYNCING) }
                        intervalHandlerMap[key]?.syncHandler?.sendEmptyMessageDelayed(1, 1000)
                    }
                }
    }

    /**
     * 首页当前用户当前设备所有状态查询
     * [syncStateMap] key为同步指令名-设备id value:该指令是否同步成功  0:失败  1：成功  2：同步中
     */

    fun homeSyncState(context: Context, syncStateMap: Map<String, Int>, childUserId: String, childDeviceId: String, syncResult: (Map<String, Int>) -> Unit) {
        mContext = context.applicationContext
        val syncResultMap = mutableMapOf<String, Int>()
        syncStateMap.forEach { (instructionFlag, state) ->
            val key = "$childDeviceId-$instructionFlag"
            if (intervalHandlerMap[key] == null) {
                val syncData = SyncData(childUserId, childDeviceId, instructionFlag)
                intervalHandlerMap[key] = syncData

            } /*else {
                if (intervalHandlerMap[key]!!.syncHandler != null) {
                    if (intervalHandlerMap[key]!!.syncHandler!!.syncState == SyncState.SYNCING) {
                        syncResultMap[key] = 2
                    } else {
                        syncResultMap[key] = state
                    }
                }
            }*/
            syncResultMap[key] = state
        }
        syncResult.invoke(syncResultMap)
    }

    private var currentKey: String = ""
    /**
     * 查询指令同步状态
     */
    fun querySyncState(context: Context, instructionFlag: String, childUserId: String, childDeviceId: String, syncResult: (Boolean) -> Unit) {
        mContext = context.applicationContext
        currentKey = "$childDeviceId-$instructionFlag"
        if (intervalHandlerMap[currentKey] == null) {
            val syncData = SyncData(childUserId, childDeviceId, instructionFlag)
            intervalHandlerMap[currentKey] = syncData
        }

        instructionSyncService.instructionSyncState(instructionFlag, childUserId, childDeviceId)
                .observeOnUI()
                .subscribeIgnoreError {
                    //同步成功
                    if (it.isSynced()) {
                        syncResult.invoke(true)
                    } else {
                        syncResult.invoke(false)
                    }
                }
    }

    /**
     * 发送同步指令
     */
    fun sendSync(childDeviceId: String, instructionFlag: String, childUserId: String) {
        val key = "$childDeviceId-$instructionFlag"
        instructionSyncService.sendSyncInstruction(instructionFlag, childUserId, childDeviceId)
                .observeOnUI()
                .subscribe(
                        {
                            val syncData = intervalHandlerMap[key]
                            if (syncData?.syncHandler != null) {
                                val handler = syncData.syncHandler
                                handler!!.time = defaultTime
                                handler.sendEmptyMessage(1)
                            } else {
                                val handler = SyncHandler(defaultTime, sInstance!!)
                                intervalHandlerMap[key] = SyncData(childUserId, childDeviceId, instructionFlag, handler)
                                handler.sendEmptyMessage(1)
                            }
                        },
                        {
                            ToastUtils.showShort("守护设置发送失败")
                        }
                )
    }

    fun getLocalBroadcastManager(context: Context): LocalBroadcastManager {
        return LocalBroadcastManager.getInstance(context)
    }

    /**
     * 本地广播的action
     */
    fun getIntentFilterAction(instructionFlag: String, childDeviceId: String): String = "$childDeviceId-$instructionFlag"

    private fun sendLocalBroadcast(context: Context, instructionFlag: String, childDeviceId: String, syncState: SyncState) {
        val intent = Intent()
        intent.action = getIntentFilterAction(instructionFlag, childDeviceId)
        val state = when (syncState) {
            SyncState.SYNCING -> 1
            SyncState.SYNC_FAILED -> 2
            SyncState.SYNC_SUCCESS -> 3
        }
        intent.putExtra(DEVICE_ID, intervalHandlerMap[childDeviceId]?.childDeviceId
                ?: childDeviceId)
        intent.putExtra(SYNC_STATE, state)
        getLocalBroadcastManager(context).sendBroadcast(intent)
    }

    fun onDestroy() {
        intervalHandlerMap.values.forEach {
            it.syncHandler?.removeCallbacksAndMessages(null)
            it.syncHandler = null
        }
        intervalHandlerMap.clear()
    }
}