package com.gwchina.parent.screenshot.data

import com.android.base.app.dagger.ActivityScope
import com.android.sdk.net.kit.optionalExtractor
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.utils.JsonUtils
import io.reactivex.Flowable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 11:40
 */
@ActivityScope
class ScreenshotRepository @Inject constructor(
        appDataSource: AppDataSource,
        private val screenshotApi: ScreenshotApi
) {

    val childDeviceId = appDataSource.user().currentDevice?.device_id
    val childUserId = appDataSource.user().currentChild?.child_user_id

    fun addScreenshot(): Flowable<Optional<ScreenshotResponse>>? {
        if (childUserId.isNullOrEmpty() || childDeviceId.isNullOrEmpty()) return null
        return screenshotApi.addScreenshot(childUserId, childDeviceId).optionalExtractor()
    }

    fun delPicList(picList: List<String>): Flowable<Optional<String>> {
        return screenshotApi.deleteScreenshotList(picList.joinToString(separator = ",")).optionalExtractor()
    }

    fun screenPicList(): Flowable<Optional<List<ScreenshotData>>>? {
        if (childDeviceId.isNullOrEmpty() || childUserId.isNullOrEmpty()) return null
        return screenshotApi.screenshotPicList(childDeviceId, childUserId).optionalExtractor()
    }

    fun picDetail(recordId: String): Flowable<Optional<ScreenshotData>> {
        return screenshotApi.screenshotPicDetail(recordId).optionalExtractor()
    }

    fun screenshotStatisticsData(): Flowable<Optional<ScreenshotStatisticsData>>? {
        if (childUserId.isNullOrEmpty() || childDeviceId.isNullOrEmpty()) return null
        return screenshotApi.screenshotStatisticsData(childUserId, childDeviceId).optionalExtractor()
    }

}