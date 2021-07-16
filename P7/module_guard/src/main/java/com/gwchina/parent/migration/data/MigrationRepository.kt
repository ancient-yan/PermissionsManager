package com.gwchina.parent.migration.data

import com.android.base.app.dagger.ActivityScope
import com.android.sdk.cache.getEntity
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.gwchina.parent.migration.presentation.MigratingData
import com.gwchina.parent.migration.presentation.UploadingChild
import com.gwchina.sdk.base.data.app.StorageManager
import com.gwchina.sdk.base.data.utils.JsonUtils
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 14:31
 */
@ActivityScope
class MigrationRepository @Inject constructor(
        private val migrationApi: MigrationApi,
        private val storageManager: StorageManager
) {

    companion object {
        private const val MIGRATING_DATA_KEY = "migrating_data_key"
    }

    fun migrationDeviceList(): Flowable<List<MigratingDevice>> {
        return migrationApi.deviceList().optionalExtractor()
                .map {
                    it.orElse(null).migration_record_list ?: emptyList()
                }
    }

    fun startNewVersion(): Completable {
        /*0 返回旧版（格雷6.0）、1 留在新版（格雷7.0）*/
        return migrationApi.setMigrationFlag(1).resultChecker()
                .ignoreElements()
    }

    fun backToOldVersion(): Completable {
        /*0 返回旧版（格雷6.0）、1 留在新版（格雷7.0）*/
        return migrationApi.setMigrationFlag(0).resultChecker()
                .ignoreElements()
    }

    fun batchBindChildDevice(childList: List<UploadingChild>): Completable {
        return migrationApi.batchBindChildDevice(JsonUtils.toJson(childList))
                .resultChecker()
                .ignoreElements()
    }

    fun migratingData(): MigratingData? {
        return storageManager.userStorage().getEntity<MigratingData>(MIGRATING_DATA_KEY)
    }

    fun saveMigrationData(migrationData: MigratingData) {
        storageManager.userStorage().putEntity(MIGRATING_DATA_KEY, migrationData)
    }

}