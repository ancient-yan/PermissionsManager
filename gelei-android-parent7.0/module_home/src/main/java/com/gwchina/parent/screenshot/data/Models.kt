package com.gwchina.parent.screenshot.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-18 15:36
 */
data class ScreenshotResponse(val record_id: String? = "")

/**
 * [isSelected]:是否选中
 * [isOpenPage]:是否进入页面
 * [is_normal]0:锁屏下的截图  1：非锁屏的截图
 */
@Parcelize
data class ScreenshotData(val record_id: String?, val pic_hash: String? = "", val upload_time: Long = 0, val size_url: String? = "", val url: String? = "", val is_normal: Int, var isSelected: Boolean = false) : Parcelable

data class ScreenshotStatisticsData(val day_count: Int, val day_del_count: Int, val sum_count: Int, val sum_three_count: Int, var isOpenPage: Boolean = false)