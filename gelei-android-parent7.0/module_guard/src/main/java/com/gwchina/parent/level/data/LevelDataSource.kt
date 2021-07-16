package com.gwchina.parent.level.data

import com.android.base.app.dagger.ActivityScope
import com.android.sdk.cache.flowableOptional
import com.android.sdk.net.kit.concatMultiSource
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.app.StorageManager
import com.gwchina.sdk.base.data.utils.JsonUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-28 19:57
 */
@ActivityScope
class LevelRepository @Inject constructor(
        private val levelApi: LevelApi,
        private val storageManager: StorageManager,
        private val appDataSource: AppDataSource
) {

    companion object {
        private const val GUARD_LEVEL_KEY = "guard_level_key_"
    }

    /**守护项目*/
    fun guardItems(childDeviceId: String, deviceType: String): Flowable<Optional<List<GuardLevel>>> {
        val remote = levelApi.loadGuardItem(childDeviceId).optionalExtractor()

        val local = storageManager.stableStorage().flowableOptional<List<GuardLevel>>(buildLevelDataKey(deviceType))

        return concatMultiSource(remote, local) {
            storageManager.stableStorage().putEntity(buildLevelDataKey(deviceType), it)
        }
    }

    /**设置守护等级*/
    fun setLevel(childUserId: String, childDeviceId: String, guardLevelId: String, guardItemIdList: List<String>): Completable {
        return levelApi.setGuardLevel(childUserId, childDeviceId,
                guardLevelId, JsonUtils.toJson(guardItemIdList))
                .resultChecker()
                .flatMapCompletable {
                    //修改等级之后需要清空设备相关的缓存
                    storageManager.deviceStorage(childDeviceId).clearAll()
                    //之后需要同步用户数据
                    appDataSource.syncChildren()
                }
    }

    private fun buildLevelDataKey(deviceType: String) = "$GUARD_LEVEL_KEY$deviceType"

}