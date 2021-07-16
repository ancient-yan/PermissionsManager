package com.gwchina.parent.level.presentation

import android.support.v4.widget.NestedScrollView
import com.android.base.kotlin.dip
import com.android.base.utils.android.ViewUtils
import kotlinx.android.synthetic.main.level_fragment_setup.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-05-05 10:22
 */
fun SetGuardLevelFragment.installScrollingVisibleTitleBar(onReceiveAlpha: (Float) -> Unit) {
    if (levelSvContent != null && levelTvTitle != null) {
        if (levelTvTitle.measuredHeight == 0) {
            ViewUtils.measureWithScreenSize(levelTvTitle)
        }
        ScrollingVisibleTitleBar(levelSvContent, levelTvTitle.measuredHeight + dip(10), onReceiveAlpha)
    }
}

private class ScrollingVisibleTitleBar(private val scrollView: NestedScrollView, headerHeight: Int, private val onReceiveAlpha: (Float) -> Unit) {

    private val maxDarkAlpha = 1F
    private var maxScrollDistanceToVisible = headerHeight

    init {
        setTitleAlpha(scrollView.scrollY)
        scrollView.post { setupScrollingDarken() }
    }

    private fun setupScrollingDarken() {
        setTitleAlpha(scrollView.scrollY)
        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            setTitleAlpha(scrollY)
        })
    }

    private fun setTitleAlpha(verticalScrollOffset: Int) {
        if (verticalScrollOffset >= maxScrollDistanceToVisible) {
            onReceiveAlpha(maxDarkAlpha)
        } else {
            onReceiveAlpha((verticalScrollOffset * 1F / maxScrollDistanceToVisible) * maxDarkAlpha)
        }
    }

}