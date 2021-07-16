package com.gwchina.sdk.base.upgrade


import com.android.sdk.cache.flowableOptional
import com.android.sdk.net.kit.concatMultiSource
import com.android.sdk.net.kit.optionalExtractor
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.AppContext
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers

/**
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-04-26 10:49
 */
internal class UpgradeRepository {

    companion object {
        private const val APP_VERSION_KEY = "app_version_key"
        private const val PLATFORM = "11"
        private const val IGNORE_VERSION_PREFIX = "ignore_version_prefix"
    }

    private val updateApi = AppContext.serviceFactory().create(UpgradeApi::class.java)
    private val storageManager = AppContext.storageManager()
    private val schedulerProvider = AppContext.schedulerProvider()
    private var upgradeResponse: UpgradeResponse? = null

    fun appUpdateInfo(appVersionName: String): Flowable<Optional<UpgradeResponse>> {
        val response = upgradeResponse
        if (response != null) {
            return Flowable.just(Optional.of(response))
        }

        val remote = updateApi
                .loadLeastVersion(PLATFORM, appVersionName)
                .optionalExtractor()

        val local = storageManager.currentDeviceStorage()
                .flowableOptional<UpgradeResponse>(APP_VERSION_KEY)
                .subscribeOn(schedulerProvider.io())

        return concatMultiSource(remote, local) {
            storageManager.stableStorage().putEntity(APP_VERSION_KEY, it)
        }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    upgradeResponse = it.orElse(null)
                }
    }

    fun saveIgnored(version: String) {
        storageManager.stableStorage().putBoolean("${IGNORE_VERSION_PREFIX}_$version", true)
    }

    fun isIgnored(version: String): Boolean {
        return storageManager.stableStorage().getBoolean("${IGNORE_VERSION_PREFIX}_$version", false)
    }

}