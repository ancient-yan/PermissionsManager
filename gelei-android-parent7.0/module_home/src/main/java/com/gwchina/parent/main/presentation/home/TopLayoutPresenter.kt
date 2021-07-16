package com.gwchina.parent.main.presentation.home

import android.support.v4.widget.NestedScrollView
import com.android.base.kotlin.dip
import com.android.base.kotlin.gone
import com.android.base.kotlin.onDebouncedClick
import com.android.base.kotlin.visible
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.presentation.home.card.SwitchChildListeners
import com.gwchina.parent.main.presentation.home.card.showSwitchChildPopup
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.utils.mapChildAvatarBig
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import kotlinx.android.synthetic.main.home_fragment.*

internal class TopLayoutPresenter(
        private val host: HomeFragment,
        private val viewModel: HomeViewModel
) {

    private var maxScrollDistanceToDarken = dip(150F)

    private val sv: NestedScrollView = host.svHomeScrollContent
    private val topView = host.clHomeTopChildInfo
    private val topAvatarView = host.ivHomeTopChildAvatar

    init {
        topView.alpha = 0F
        sv.post { setupScrollingDarken() }
    }

    fun showTopChildInfo(user: User) {
        val currentChild = user.currentChild

        if (currentChild == null) {
            host.ivHomeTopChildAvatar.setImageResource(R.drawable.img_default_avatar)
            host.ivHomeTopChildAvatarSubscript.gone()
            host.ivHomeTopChildAvatar.onDebouncedClick {
                host.mainNavigator.commonJump()
            }
        } else {
            host.ivHomeTopChildAvatar.setImageResource(mapChildAvatarBig(currentChild.sex))
            if (user.childList?.size ?: 0 > 1) {
                host.ivHomeTopChildAvatarSubscript.visible()
                host.ivHomeTopChildAvatar.onDebouncedClick {
                    showSwitchChildPopup(host.ivHomeTopChildAvatar, user, switchChildListeners)
                }
            } else {
                host.ivHomeTopChildAvatarSubscript.gone()
                host.ivHomeTopChildAvatar.setOnClickListener(null)
            }
        }
    }

    private val switchChildListeners by lazy {
        SwitchChildListeners(
                onSelectNewChild = {
                    if (isMemberGuardExpired(it.status)) {
                        OpenMemberDialog.show(host.requireContext()) {
                            messageId = R.string.as_member_expired_support_one_tips
                        }
                    } else {
                        viewModel.switchChild(it)
                    }
                }
        )
    }


    private fun setupScrollingDarken() {
        setDrawableAlpha(sv.scrollY)
        sv.setOnScrollChangeListener { _: NestedScrollView, _, scrollY, _, _ ->
            setDrawableAlpha(scrollY)
        }
    }

    private fun setDrawableAlpha(verticalScrollOffset: Int) {
        if (maxScrollDistanceToDarken == 0F) {
            maxScrollDistanceToDarken = ScreenUtils.getScreenWidth() / 2F
        }

        if (verticalScrollOffset >= maxScrollDistanceToDarken) {
            topView.alpha = 1F
            topAvatarView.isClickable = true
        } else {
            // (0 -> 1)
            topView.alpha = ((verticalScrollOffset / maxScrollDistanceToDarken))
            topAvatarView.isClickable = false
        }
    }

}