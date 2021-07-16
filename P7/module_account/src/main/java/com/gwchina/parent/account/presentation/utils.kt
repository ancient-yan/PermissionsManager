package com.gwchina.parent.account.presentation

import android.content.Context
import android.text.SpannableStringBuilder
import com.android.base.kotlin.colorFromId
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.account.R
import com.gwchina.parent.account.AccountNavigator
import com.gwchina.sdk.base.utils.newGwStyleClickSpan

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-09 10:36
 */
/**
 * 会存在这个bug
 * https://www.jianshu.com/p/961b159f7d5a
 * 可以在区域2后增加一个空格，并对空格附加空实现的点击事件
 */
internal fun createPrivacyAndAgreementText(context: Context, accountNavigator: AccountNavigator, clickTextRes: Int): SpannableStringBuilder {

    //用户协议
    val agreementSpan = newGwStyleClickSpan(context) {
        accountNavigator.openAgreementPage()
    }

    //隐私协议
    val privacyPolicySpan = newGwStyleClickSpan(context) {
        accountNavigator.openPrivacyPage()
    }

    //空实现
    val nothingSpan= newGwStyleClickSpan(context){

    }

    return SpanUtils()
            .append(context.getString(clickTextRes))
            .setForegroundColor(context.colorFromId(R.color.gray_level2))
            .append(context.getString(R.string.user_permit_agreement_with_guillemet))
            .setClickSpan(agreementSpan)
            .append("${context.getString(R.string.and)}")
            .append(context.getString(R.string.privacy_policy_with_guillemet))
            .setClickSpan(privacyPolicySpan)
            .append("  ").setClickSpan(nothingSpan)
            .create()
}