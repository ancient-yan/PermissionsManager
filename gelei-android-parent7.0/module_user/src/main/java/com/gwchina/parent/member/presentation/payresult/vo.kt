package com.gwchina.parent.member.presentation.payresult

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-28 15:04
 */
data class PayResultVO(
        val payPrice: CharSequence,
        val payGoods: CharSequence,
        val payType: CharSequence,
        val orderNo: String,
        val status: CharSequence,
        val payStatusIconRes: Int,
        val isPayProblems: Boolean
)