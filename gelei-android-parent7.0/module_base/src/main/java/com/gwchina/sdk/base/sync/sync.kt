package com.gwchina.sdk.base.sync

import android.content.Context
import android.support.v4.content.LocalBroadcastManager
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.data.app.AppDataSource


/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-12 15:01
 */

enum class SyncState {
    //同步中
    SYNCING,
    //同步成功
    SYNC_SUCCESS,
    SYNC_FAILED
}

/**
 * [lastTime]剩余同步时间
 */
data class State(var syncState: SyncState, var lastTime: Long = 0)

class SyncStateManager {

    companion object {

        const val LOCAL_ACTION = "sync_localBroadcast"

        /**
         * 首页指令是否在同步 key:指令类型  value：是否在不同
         */
        private val isHomePageSyncingMap = mutableMapOf<String, Boolean>()

        init {
            initHomePageSyncMap()
        }

        private fun initHomePageSyncMap() {
            isHomePageSyncingMap[INSTRUCTION_SYNC_LEVEL] = false
            isHomePageSyncingMap[INSTRUCTION_SYNC_TIME] = false
            isHomePageSyncingMap[INSTRUCTION_SYNC_APP] = false
            isHomePageSyncingMap[INSTRUCTION_SYNC_URL] = false
            isHomePageSyncingMap[INSTRUCTION_SYNC_PHONE] = false
        }

        fun homePageSyncingMap(): MutableMap<String, Boolean> {
            return isHomePageSyncingMap
        }

        /**
         * [syncMap]
         */
        val syncMap = mutableMapOf<String, State?>()

        fun getSyncState(syncName: String): State? {
            return syncMap[syncName]
        }

        fun getLocalBroadcastManager(context: Context): LocalBroadcastManager {
            return LocalBroadcastManager.getInstance(context)
        }

        fun reset() {
            initHomePageSyncMap()
            syncMap.clear()
        }
    }

}