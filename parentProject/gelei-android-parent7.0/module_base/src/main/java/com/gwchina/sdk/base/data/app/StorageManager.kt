package com.gwchina.sdk.base.data.app

import android.content.Context
import com.android.sdk.cache.MMKVStorageFactoryImpl
import com.android.sdk.cache.Storage
import com.android.sdk.cache.TypeFlag
import com.gwchina.sdk.base.data.models.currentChildDeviceId
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-19 13:51
 */
class StorageManager internal constructor(
        private val context: Context,
        private val appDataSource: AppDataSource
) {

    companion object {
        private const val STABLE_CACHE_ID = "gwchina-parent-forever-cache-id"
        private const val USER_ASSOCIATED_CACHE_ID = "gwchina-parent-UserAssociated-default-cache-id"
        private const val ALL_USER_ASSOCIATED_CACHE_ID_KEY = "all_user_associated_cache_id_key"
        private const val EMPTY_DEVICE_ID = "empty_device_id"//avoid app crash when no device bound
    }

    private val storageFactory = MMKVStorageFactoryImpl()

    private val _userAssociated: Storage = storageFactory
            .newBuilder(context)
            .storageId(USER_ASSOCIATED_CACHE_ID)
            .enableMultiProcess(true)
            .build()

    private val _stable: Storage = storageFactory
            .newBuilder(context)
            .storageId(STABLE_CACHE_ID)
            .enableMultiProcess(true)
            .build()

    private val _userAssociatedIdList by lazy {
        _stable.getEntity<MutableList<String>>(
                ALL_USER_ASSOCIATED_CACHE_ID_KEY,
                object : TypeFlag<MutableList<String>>() {}.rawType)
                ?: mutableListOf()
    }

    private val storageCache = HashMap<String, WeakReference<Storage>>()

    /**  全局默认用户相关缓存，不支持跨进程，用户退出后缓存也会被清理。 */
    fun userStorage() = _userAssociated

    /** 全局默认永久缓存，不支持跨进程，用户退出后缓存不会被清理。 */
    fun stableStorage() = _stable

    /** 获取当前设备对应的 [Storage] ，所有设备相关的数据都需要通过此方法获取 [Storage]  对象来存储，设备解绑之后，应该清除该设备对应的缓存*/
    fun currentDeviceStorage(): Storage {
        val currentChildDeviceId = appDataSource.user().currentChildDeviceId ?: EMPTY_DEVICE_ID
        return newStorage(currentChildDeviceId, true)
    }

    /** 获取指定设备对应的 [Storage] ，所有设备相关的数据都需要通过此方法获取 [Storage]  对象来存储，设备解绑之后，应该清除该设备对应的缓存*/
    fun deviceStorage(deviceId: String): Storage {
        return newStorage(deviceId, true)
    }

    @Synchronized
    fun newStorage(storageId: String, userAssociated: Boolean = false): Storage {
        if (userAssociated) {
            if (!_userAssociatedIdList.contains(storageId)) {
                _userAssociatedIdList.add(storageId)
                stableStorage().putEntity(ALL_USER_ASSOCIATED_CACHE_ID_KEY, _userAssociatedIdList)
            }
        }

        val weakReference = storageCache[storageId]

        if (weakReference != null) {
            val storage = weakReference.get()
            if (storage != null) {
                return storage
            }
        }

        val storage = storageFactory.newBuilder(context)
                .storageId(storageId)
                .enableMultiProcess(true)
                .build()

        storageCache[storageId] = WeakReference(storage)

        return storage
    }

    /**仅由[AppDataSource.logout]在退出登录时调用*/
    internal fun clearUserAssociated() {
        userStorage().clearAll()
        currentDeviceStorage().clearAll()

        if (_userAssociatedIdList.isEmpty()) {
            return
        }
        for (cacheId in _userAssociatedIdList) {
            if (cacheId.isEmpty()) {
                continue
            }
            storageFactory.newBuilder(context).storageId(cacheId).enableMultiProcess(true).build().clearAll()
            Timber.d("clear user associated cache：$cacheId")
        }
        _userAssociatedIdList.clear()

        stableStorage().remove(ALL_USER_ASSOCIATED_CACHE_ID_KEY)
    }

}