package com.gwchina.parent.main.presentation.mine;

import android.support.v4.widget.NestedScrollView
import com.android.base.utils.android.compat.AndroidVersion
import com.android.base.utils.android.compat.SystemBarCompat
import kotlinx.android.synthetic.main.mine_fragment.*

fun MineFragment.installScrollingVisibleTitleBar(onReceiveAlpha: (Float) -> Unit) {
    onReceiveAlpha(0F)
    view?.post {
        val statusHeight = if (AndroidVersion.atLeast(19)) SystemBarCompat.getStatusBarHeight(context) else 0
        ScrollingVisibleTitleBar(svMineContent, clMineTitleContent.measuredHeight + statusHeight, llMineTitleWrapper.measuredHeight, onReceiveAlpha)
    }
}

private class ScrollingVisibleTitleBar(private val scrollView: NestedScrollView, headerHeight: Int, titleHeight: Int, private val onReceiveAlpha: (Float) -> Unit) {

    private val maxDarkAlpha = 1F
    private var maxScrollDistanceToVisible = headerHeight - titleHeight

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
        if (verticalScrollOffset > maxScrollDistanceToVisible) {
            onReceiveAlpha(maxDarkAlpha)
        } else {
            onReceiveAlpha((verticalScrollOffset * 1F / maxScrollDistanceToVisible) * maxDarkAlpha)
        }
    }

}
