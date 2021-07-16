package com.gwchina.parent.member.presentation.center

import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.android.base.utils.android.ResourceUtils
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.presentation.purchase.MemberPlanVO
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.enableSpanClickable
import com.gwchina.sdk.base.widget.dialog.TipsManager
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.member_center_goods_layout.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 11:01
 * 暂时用显示隐藏的方式来处理的，每点击一次不同的tab,会刷新数据
 * 后续考虑使用viewpager+fragment形式进行替换（CoordinatorLayout+AppBarLayout+ViewPager+TabLayout）
 */
internal class GoodsViewHolder(private val interactor: MemberCenterInteractor, itemView: View) : BaseItemViewHolder<Cards.GoodsItemList>(interactor, itemView) {


    private var currentPosition = 1

    private var currentPlanPosition = 0

    private var isLoaded = false

    companion object {
        const val SVIP_BUY_VIP = 0
        const val REPEAT_BUY_VIP = 1
        const val VIP_BUY_SVIP = 2
    }

    fun showData(memberPlans2: List<MemberPlanVO>?) {
        val memberPlans = memberPlans2?.toMutableList()

        if (memberPlans.isNullOrEmpty()) {
            tvMemberAgreement.gone()
            btnMemberPay.gone()
            clMemberIndicator.gone()
        } else {
            //顺序不能乱
            goodsLinearLayout.onItemClickListener = { position, memberPlan ->
                btnMemberPay.text = "以${if (memberPlan.finalPrice % 1 == 0.0) {
                    memberPlan.finalPrice.toInt().toString()
                } else {
                    memberPlan.finalPrice.toString()
                }}元开通"
                currentPlanPosition = position
            }
            goodsLinearLayout.buildData(memberPlans, currentPlanPosition)
            tvMemberAgreement.visible()
            btnMemberPay.visible()
            clMemberIndicator.visible()
        }

        //会员服务协议
        val agreementSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                interactor.openServiceAgreement()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(context, R.color.blue_level2)
                ds.isUnderlineText = false
            }
        }

        tvMemberAgreement.text = SpanUtils()
                .append(ResourceUtils.getText(R.string.user_member_agreement_normal_span))
                .setForegroundColor(ContextCompat.getColor(context, R.color.gray_level3))
                .append(ResourceUtils.getText(R.string.user_member_agreement_click_span))
                .setForegroundColor(ContextCompat.getColor(context, R.color.blue_level2))
                .setClickSpan(agreementSpan)
                .create()
        tvMemberAgreement.enableSpanClickable()

        btnMemberPay.setOnClickListener {
            //todo 判断会员类型和当前购买的套餐
            showBuyTips(VIP_BUY_SVIP, count = 10, memberPlan = memberPlans?.get(currentPlanPosition))
        }
        llVip.setOnClickListener {
            if (currentPosition == 0) return@setOnClickListener
            defaultCheck(0)
        }
        llSVip.setOnClickListener {
            if (currentPosition == 1) return@setOnClickListener
            defaultCheck(1)
        }
        if (!isLoaded) {
            defaultCheck(currentPosition)
            isLoaded = true
        }

    }

    private fun defaultCheck(position: Int) {
        when (position) {
            0 -> {
                tvVip.setTextColor(ContextCompat.getColor(context, R.color.memberCenterColorPrimary))
                tvSVip.setTextColor(ContextCompat.getColor(context, R.color.gray_level2))
                vipIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.member_indicator_selected_color))
                svipIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                tvVipTips.text = "会员尊享特权"
                currentPosition = 0
            }
            1 -> {
                tvVip.setTextColor(ContextCompat.getColor(context, R.color.gray_level2))
                tvSVip.setTextColor(ContextCompat.getColor(context, R.color.memberCenterColorPrimary))
                vipIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                svipIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.member_indicator_selected_color))
                tvVipTips.text = "高级会员尊享特权"
                currentPosition = 1
            }
        }
    }

    /**购买时的对话框
     * 1、从svip买vip
     * 2、当前是包月套餐，连续购买
     * 3、从vip买svip
     * */
    private fun showBuyTips(type: Int, count: Int? = null, memberPlan: MemberPlanVO?) {
        val message = when (type) {
            SVIP_BUY_VIP -> interactor.host.getString(R.string.member_svip_buy_vip_tips)
            REPEAT_BUY_VIP -> interactor.host.getString(R.string.member_repeat_buy_vip_tips)
            else -> interactor.host.getString(R.string.member_vip_buy_svip_tips, count)
        }
        interactor.host.showConfirmDialog {
            this.message = message
            if (type == REPEAT_BUY_VIP) {
                noNegative()
                this.positiveId = R.string.i_got_it
            } else {
                this.negativeId = R.string.cancel_
                this.positiveId = R.string.member_continue_buy
                this.positiveListener = {
                    if (memberPlan != null) {
                        StatisticalManager.onEvent(UMEvent.ClickEvent.MEMBERCENTER_BTN_RENEW)
                        buy(memberPlan)
                    }
                }
            }
        }
    }

    /**创建订单*/
    private fun buy(memberPlanVO: MemberPlanVO) {
        //有折扣，但已过期, 刷新购买项目
        if (memberPlanVO.discountEnable && interactor.isDiscountExpired(memberPlanVO)) {
            TipsManager.showMessage(interactor.host.getString(R.string.user_purchase_plan_discount_expired))
            interactor.host.autoRefresh()
            return
        }
        interactor.showPaymentDialogImmediately(memberPlanVO)
    }
}
