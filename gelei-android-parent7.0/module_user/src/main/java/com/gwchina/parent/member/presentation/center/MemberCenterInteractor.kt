package com.gwchina.parent.member.presentation.center

import com.android.base.utils.android.ResourceUtils
import com.android.sdk.social.ali.AliPayExecutor
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.MemberNavigator
import com.gwchina.parent.member.presentation.purchase.MemberPlanVO
import com.gwchina.parent.member.widget.PaymentDialogBuilder
import com.gwchina.parent.member.widget.showPaymentDialog
import com.gwchina.sdk.base.widget.dialog.TipsManager
import java.util.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 16:04
 */
internal class MemberCenterInteractor(
        val host: MemberCenterFragment,
        private val memberNavigator: MemberNavigator,
        private val memberViewModel: MemberCenterViewModel

) {
    var member: MemberCenterVO? = null

    fun openMember() {
//        memberNavigator.openPurchaseMemberPage(false)
    }

    fun openServiceAgreement() {
        memberNavigator.openAgreement()
    }

    fun showPaymentDialogImmediately(memberPlanVO: MemberPlanVO) {
        showPaymentDialog(host.context!!) {
            costText = memberPlanVO.payPriceText
            goodsText = memberPlanVO.payGoodsText
            actionListener = {
                when (it) {
                    PaymentDialogBuilder.ALI_PAY -> {
                        if (!AliPayExecutor.isAliPayInstalled(host.context)) {
                            TipsManager.showMessage(ResourceUtils.getText(R.string.uninstall_ali_pay))
                        } else {
                            memberViewModel.createAliPayBuyInfo(memberPlanVO.planId)
                        }
                    }
                    PaymentDialogBuilder.WX_PAY -> {
                        if (!host.weChatPayManager.isInstalledWeChat) {
                            TipsManager.showMessage(ResourceUtils.getText(R.string.uninstall_wechat))
                        } else {
                            memberViewModel.createWXPayBuyInfo(memberPlanVO.planId)
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }

    /**
     * 活动是否过期
     */
    fun isDiscountExpired(memberPlanVO: MemberPlanVO): Boolean {
        val calendarNow = Calendar.getInstance()
        calendarNow.set(Calendar.HOUR_OF_DAY, 0)
        calendarNow.set(Calendar.MINUTE, 0)
        calendarNow.set(Calendar.SECOND, 0)
        calendarNow.set(Calendar.MILLISECOND, 0)
        return calendarNow.time.time > memberPlanVO.discountEndTime
    }
}