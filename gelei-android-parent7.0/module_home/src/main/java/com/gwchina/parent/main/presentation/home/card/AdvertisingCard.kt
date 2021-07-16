package com.gwchina.parent.main.presentation.home.card

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import com.android.base.kotlin.dip
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.data.models.Advertising
import com.gwchina.sdk.base.widget.advertising.CommonAdvertisingPageAdapter
import com.tmall.ultraviewpager.UltraViewPager
import kotlinx.android.synthetic.main.home_card_advertising.view.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-01 19:50
 */
class AdvertisingCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.home_card_advertising, this)
    }

    private var showingList: List<Advertising>? = null

    fun showData(list: List<Advertising>?) {
        if (showingList == list) {
            return
        }

        if (list.isNullOrEmpty()) {
            vpHomeAdvertising.adapter = null
        } else {
            showAdvertising(list)
        }

        showingList = list
    }

    private fun showAdvertising(it: List<Advertising>) {
        val bannerAdapter = CommonAdvertisingPageAdapter(context, it)
        vpHomeAdvertising.adapter = bannerAdapter
        if (it.size > 1) {
            setAutoScroll()
        } else {
            vpHomeAdvertising.disableAutoScroll()
            vpHomeAdvertising.disableIndicator()
        }
    }

    private fun setAutoScroll() {
        //view pager
        vpHomeAdvertising.initIndicator()
                .setOrientation(UltraViewPager.Orientation.HORIZONTAL)
                .setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
                .setFocusResId(R.drawable.icon_indicator_selected)
                .setNormalResId(R.drawable.icon_indicator_unselected)
                .setMargin(0, 0, 0, dip(7))
                .build()
        //设定页面循环播放
        vpHomeAdvertising.setInfiniteLoop(true)
        //设定页面自动切换，间隔5秒
        vpHomeAdvertising.setAutoScroll(5000)
    }

}