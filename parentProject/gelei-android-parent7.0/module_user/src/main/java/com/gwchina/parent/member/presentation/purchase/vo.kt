package com.gwchina.parent.member.presentation.purchase

import com.gwchina.parent.member.data.MemberItem
import com.gwchina.sdk.base.data.models.Advertising
import com.gwchina.sdk.base.data.models.User

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-23 19:38
 */
data class PurchaseDataVO(
        val userInfo: UserInfoVO,
        val user: User,
        val memberPlans: List<MemberPlanVO>?,
        val memberItemList: List<MemberItem>?,
        var selectPlanIndex: Int? = 0,
        val adItems: List<Advertising>? = null
)

data class UserInfoVO(
        val desc: CharSequence,
        val head_photo_path: String,
        val nick_name: String
)

data class MemberPlanVO(
        val discountPriceCharSequence: CharSequence,
        val finalPrice: Double,
        val originalPriceCharSequence: CharSequence? = null,
        val planId: String,
        val planName: String,
        val payGoodsText: CharSequence,
        val payPriceText: CharSequence,
        val planLabel: String? = null,
        val remark: String? = "",
        val discountEnable: Boolean,
        val discountEndTime: Long
)

