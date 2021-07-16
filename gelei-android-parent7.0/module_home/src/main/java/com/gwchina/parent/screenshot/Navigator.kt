package com.gwchina.parent.screenshot

import android.annotation.SuppressLint
import android.os.Bundle
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.findFragmentByTag
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.parent.daily.PicPreFragment
import com.gwchina.parent.screenshot.data.ScreenshotData
import com.gwchina.parent.screenshot.presentation.DeleteScreenshotFragment
import com.gwchina.parent.screenshot.presentation.RemoteScreenshotFragment
import com.gwchina.parent.screenshot.presentation.ScreenshotPicPreFragment
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 19:15
 */
@ActivityScope
class Navigator @Inject constructor(private val activity: ScreenshotActivity) {

    @SuppressLint("SimpleDateFormat")
    private val simpleDataFormat = SimpleDateFormat("HH:mm:ss")

    fun addFirstFragment(savedInstanceState: Bundle?) {
        savedInstanceState.ifNull {
            activity.inFragmentTransaction {
                addWithDefaultContainer(RemoteScreenshotFragment())
            }
        }
    }

    fun openDeleteScreenshotPage(screenshotPicList: List<ScreenshotData>) {
        if (activity.supportFragmentManager.findFragmentByTag(DeleteScreenshotFragment::class) == null) {
            activity.inFragmentTransaction {
                val deleteFragment = DeleteScreenshotFragment.getInstance(screenshotPicList)
                replaceWithStack(fragment = deleteFragment)
            }
        }
    }

    fun openImageViewerPage(position: Int, picList: List<ScreenshotData>) {
        if (activity.supportFragmentManager.findFragmentByTag(PicPreFragment::class) == null) {
            activity.inFragmentTransaction {
                val list = ArrayList<ScreenshotPicPreFragment.PicPreItem>()
                ScreenshotUtils.parseData(picList).entries.forEach {
                    when (it.key) {
                        0 -> {
                            parseData(it, "今日")?.apply {
                                list.addAll(this)
                            }
                        }
                        1 -> {
                            parseData(it, "昨日")?.apply {
                                list.addAll(this)
                            }
                        }
                        2 -> {
                            parseData(it, "前日")?.apply {
                                list.addAll(this)
                            }
                        }
                    }
                }
                replaceWithStack(fragment = ScreenshotPicPreFragment.newInstance(position, list))
            }
        }
    }

    private fun parseData(it: Map.Entry<Int, List<ScreenshotData>>, string: String): List<ScreenshotPicPreFragment.PicPreItem>? {
        if (it.value.isNullOrEmpty()) return null
        return it.value.map {
            ScreenshotPicPreFragment.PicPreItem(it.record_id
                    ?: "", "$string-" + simpleDataFormat.format(Date(it.upload_time)), it.url ?: "")
        }
    }

}
