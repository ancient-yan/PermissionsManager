package com.gwchina.parent.member.presentation.purchase.ad

import android.view.Gravity
import android.view.View
import com.android.base.kotlin.dip
import com.android.base.kotlin.ifNonNull
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.presentation.purchase.BaseItemViewHolder
import com.gwchina.parent.member.presentation.purchase.Cards
import com.gwchina.parent.member.presentation.purchase.PurchaseInteractor
import com.gwchina.sdk.base.linker.JUMP_ID_PURCHASE_MEMBER
import com.gwchina.sdk.base.linker.JumpInterceptor
import com.gwchina.sdk.base.linker.UrlEntity
import com.gwchina.sdk.base.widget.advertising.CommonAdvertisingPageAdapter
import com.tmall.ultraviewpager.UltraViewPager
import kotlinx.android.synthetic.main.member_purchase_item_ad.*

/**
 * 购买页面广告
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-24 18:44
 */
internal class AdViewHolder(private val interactor: PurchaseInteractor, itemView: View) : BaseItemViewHolder<Cards.AdCard>(interactor, itemView) {

    override fun initOnce() {
        interactor.purchaseDataVO.ifNonNull {
            if (adItems != null && adItems.isNotEmpty()) {

                val bannerAdapter = CommonAdvertisingPageAdapter(interactor.host.requireContext(), adItems)

                bannerAdapter.onInterceptJumpToAppPage = object : JumpInterceptor {
                    override fun shouldIntercept(urlEntity: UrlEntity): Boolean {
                        return urlEntity.params.isNotEmpty() && JUMP_ID_PURCHASE_MEMBER == urlEntity.id
                    }
                }

                vpMemberAdvertising.adapter = bannerAdapter
                if (adItems.size > 1) {
                    setAutoScroll()
                } else {
                    vpMemberAdvertising.setInfiniteLoop(false)
                    vpMemberAdvertising.disableIndicator()
                    vpMemberAdvertising.disableAutoScroll()
                }
            }
        }
    }

    private fun setAutoScroll() {
        //设定页面循环播放
        vpMemberAdvertising.setInfiniteLoop(true)
        //设定页面自动切换，间隔5秒
        vpMemberAdvertising.setAutoScroll(5000)
        //标记
        vpMemberAdvertising.initIndicator()
                .setOrientation(UltraViewPager.Orientation.HORIZONTAL)
                .setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM)
                .setFocusResId(R.drawable.icon_indicator_selected)
                .setNormalResId(R.drawable.icon_indicator_unselected)
                .setMargin(0, 0, 0, dip(7))
                .build()
    }

    fun showData() {
        initOnce()
    }
}