package com.gwchina.parent.screenshot

import com.gwchina.parent.screenshot.data.ScreenshotData
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.models.currentChildDeviceId
import timber.log.Timber
import java.util.*

object ScreenshotUtils {
    /**
     * [beginTime]00:00:00的毫秒数
     * [endTime]23:59:59的毫秒数
     */
    data class DateTime(val beginTime: Long, val endTime: Long)

    /**
     * 成功发送截屏指令的信息
     */
    data class ScreenParams(var recordId: String, var isSuccess: Boolean = false, var createTime: Long = 0)

    /**
     * [offset]:与今天的偏差 0：今天  1：昨天  2：前天 以此类推
     */
    private fun getDataTime(offset: Int): DateTime {
        var calendar = Calendar.getInstance()
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - offset, 0, 0, 0)
        val begin = calendar.time.time
        calendar = Calendar.getInstance()
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - offset, 23, 59, 59)
        val end = calendar.time.time
        return DateTime(begin, end)
    }

    fun parseData(screenshotPicList: List<ScreenshotData>): Map<Int, List<ScreenshotData>> {
        val todayScreenshotData = screenshotPicList.filter {
            it.upload_time in getDataTime(0).beginTime..getDataTime(0).endTime
        }
        val yesterdayScreenshotData = screenshotPicList.filter {
            it.upload_time in getDataTime(1).beginTime..getDataTime(1).endTime
        }
        val theDayBeforeYesterdayScreenshotData = screenshotPicList.filter {
            it.upload_time in getDataTime(2).beginTime..getDataTime(2).endTime
        }
        val map = mutableMapOf<Int, List<ScreenshotData>>()
        map[0] = todayScreenshotData
        map[1] = yesterdayScreenshotData
        map[2] = theDayBeforeYesterdayScreenshotData
        return map
    }

    /**
     * 判断map中的数据的时间是否在对应的日期时间中，现在的数据处理逻辑：增加截屏成功没有刷新接口而是动态添加数据到今日第一条；
     * 成功删除的时候会刷新接口
     * 主要是0:00分之前的数据没有刷新，新增截屏之后今日的数据会和昨天的数据混在一起
     */
    fun isMapDataMatch(map: MutableMap<Int, MutableList<ScreenshotData>>): Boolean {
        var isMatch = true
        run outside@{
            map.entries.forEachIndexed { index, mutableEntry ->
                if (!mutableEntry.value.isNullOrEmpty()) {
                    isMatch = mutableEntry.value.all { it.upload_time in getDataTime(index).beginTime..getDataTime(index).endTime }
                    if (!isMatch) {
                        return@outside
                    }
                }
            }
        }
        return isMatch
    }

    fun putLastScreenParams(recordId: String, isSuccess: Boolean) {
        val screenParams = ScreenParams(recordId, isSuccess, System.currentTimeMillis())
        val currentDeviceId = AppContext.appDataSource().user().currentChildDeviceId
        AppSettings.settingsStorage().putEntity(currentDeviceId, screenParams)
    }

    fun getLastScreenParams(): ScreenParams? {
        val currentDeviceId = AppContext.appDataSource().user().currentChildDeviceId
        if (currentDeviceId.isNullOrEmpty()) return null
        return try {
            AppSettings.settingsStorage().getEntity(currentDeviceId, ScreenParams::class.java)
        } catch (e: Exception) {
            null
        }
    }
}


