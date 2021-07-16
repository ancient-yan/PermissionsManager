package com.gwchina.parent.member.presentation.purchase

import com.android.base.kotlin.ifNonNull
import com.android.base.utils.android.ResourceUtils
import com.android.sdk.social.ali.AliPayExecutor
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.MemberNavigator
import com.gwchina.parent.member.widget.PaymentDialogBuilder
import com.gwchina.parent.member.widget.showPaymentDialog
import com.gwchina.sdk.base.widget.dialog.TipsManager
import kotlinx.android.synthetic.main.member_fragment_purchase.*
import java.util.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 16:04
 */
class PurchaseInteractor(val host: PurchaseMemberFragment, private val memberNavigator: MemberNavigator, private val purchaseMemberViewModel: PurchaseMemberViewModel) {

    var purchaseDataVO: PurchaseDataVO? = null

    fun selectPlanItem(memberPlanVO: MemberPlanVO, position: Int) {
        purchaseDataVO?.selectPlanIndex = position
        setPayButtonText(memberPlanVO)
    }

    fun notifyItemChanged() {
        host.purchaseLayoutManager?.contentAdapter?.notifyDataSetChanged()
    }

    private fun setPayButtonText(memberPlanVO: MemberPlanVO) {
        if (memberPlanVO.finalPrice % 1 == 0.0) {
            //去除小数
            host.btnMemberPay.text = host.context!!.getString(R.string.user_purchase_pay, memberPlanVO.finalPrice.toInt().toString())
        } else {
            host.btnMemberPay.text = host.context!!.getString(R.string.user_purchase_pay, memberPlanVO.finalPrice.toString())
        }
    }

    fun openServiceAgreement() {
        memberNavigator.openAgreement()
    }

    fun showPaymentDialogImmediately() {
        purchaseDataVO?.memberPlans.ifNonNull {
            val memberPlanVO = this[purchaseDataVO?.selectPlanIndex!!]
            showPaymentDialog(host.context!!) {
                costText = memberPlanVO.payPriceText
                goodsText = memberPlanVO.payGoodsText
                actionListener = {
                    when (it) {
                        PaymentDialogBuilder.ALI_PAY -> {
                            if (!AliPayExecutor.isAliPayInstalled(host.context)) {
                                TipsManager.showMessage(ResourceUtils.getText(R.string.uninstall_ali_pay))
                            } else {
                                purchaseMemberViewModel.createAliPayBuyInfo(memberPlanVO.planId)
                            }
                        }
                        PaymentDialogBuilder.WX_PAY -> {
                            if (!host.weChatPayManager.isInstalledWeChat) {
                                TipsManager.showMessage(ResourceUtils.getText(R.string.uninstall_wechat))
                            } else {
                                purchaseMemberViewModel.createWXPayBuyInfo(memberPlanVO.planId)
                            }
                        }
                        else -> {

                        }
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