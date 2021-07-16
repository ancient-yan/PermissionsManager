package com.gwchina.parent.main.presentation.mine

import com.android.base.kotlin.dip
import com.android.base.utils.android.ResourceUtils.getString
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.widget.views.TextItem

internal const val ENTRANCE_MESSAGE = 1
internal const val ENTRANCE_FEEDBACK = 2
internal const val ENTRANCE_ABOUT = 3
internal const val ENTRANCE_GUARD = 4
internal const val ENTRANCE_INVITATION = 6
//我的收益
internal const val ENTRANCE_INCOME = 7

//没有登录时的item  7.0.1需求：未登录状态需要加上入口
//internal fun loadNotLoginItems() = listOf(
//        TextItem(ENTRANCE_INVITATION, title = getString(R.string.invitation_friend), leftImgId = R.drawable.home_icon_vip_mine, topMargin = dip(8)),
//        TextItem(ENTRANCE_MESSAGE, title = getString(R.string.message_notification), leftImgId = R.drawable.home_icon_notice_mine, topMargin = dip(8)),
//        TextItem(ENTRANCE_FEEDBACK, title = getString(R.string.helps_and_feedback), leftImgId = R.drawable.home_icon_help_mine, topMargin = dip(8)),
//        TextItem(ENTRANCE_ABOUT, title = getString(R.string.about_us), leftImgId = R.drawable.home_icon_on_mine, topMargin = dip(8))
//)

//登录后的item
internal fun loadLoginItems() = listOf(
        TextItem(ENTRANCE_GUARD, title = getString(R.string.guard_report), leftImgId = R.drawable.home_icon_report_mine,
                subtitleColor = R.color.red_level1, subtitleSize = 12),
        TextItem(ENTRANCE_INVITATION, title = getString(R.string.invitation_friend), leftImgId = R.drawable.home_icon_vip_mine, topMargin = dip(8)),
        TextItem(ENTRANCE_INCOME, title = getString(R.string.my_income), leftImgId = R.drawable.home_icon_income, topMargin = dip(8)),
        TextItem(ENTRANCE_MESSAGE, title = getString(R.string.message_notification), leftImgId = R.drawable.home_icon_notice_mine, topMargin = dip(8)),
        TextItem(ENTRANCE_FEEDBACK, title = getString(R.string.helps_and_feedback), subtitle = getString(R.string.helps_and_feedback_subtitle), leftImgId = R.drawable.home_icon_help_mine, topMargin = dip(8)),
        TextItem(ENTRANCE_ABOUT, title = getString(R.string.about_us), leftImgId = R.drawable.home_icon_on_mine, topMargin = dip(8))
)

