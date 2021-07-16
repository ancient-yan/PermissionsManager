package com.gwchina.parent.member.presentation.center

import com.gwchina.parent.member.data.MemberItem
import com.gwchina.sdk.base.data.models.User

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-28 20:21
 */
data class MemberCenterVO(
        val user: User,
        val memberItemList: List<MemberItem>? = null,
        val endTime: Long?,
        val memberServiceItemList: List<MemberItem>? = null
)