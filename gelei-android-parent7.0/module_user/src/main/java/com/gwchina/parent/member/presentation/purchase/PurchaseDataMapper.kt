package com.gwchina.parent.member.presentation.purchase

import android.content.Context
import android.support.v4.content.ContextCompat
import com.android.base.app.dagger.ContextType
import com.android.base.utils.android.ResourceUtils
import com.android.sdk.social.wechat.PayInfo
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.data.MemberPlan
import com.gwchina.parent.member.data.PurchaseInfo
import com.gwchina.parent.member.data.WXPayInfo
import com.gwchina.sdk.base.data.api.MEMBER_STATUS_INVALID
import com.gwchina.sdk.base.data.models.Advertising
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.utils.formatMilliseconds
import javax.inject.Inject

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-23 19:44
 */
class PurchaseDataMapper @Inject constructor(@ContextType private val context: Context) {

    fun transform(data: PurchaseInfo?, adData: List<Advertising>, user: User): PurchaseDataVO? {
        return if (data == null) {
            return null
        } else {
            PurchaseDataVO(
                    userInfo = UserInfoVO(
                            desc = if (data.status == MEMBER_STATUS_INVALID) {
                                //非会员
                                context.resources.getText(R.string.user_purchase_top_desc)
                            } else {
                                //会员续费
                                context.resources.getString(R.string.user_purchase_top_end_time, formatMilliseconds(data.end_time))
                            },
                            nick_name = data.nick_name,
                            head_photo_path = data.head_photo_path
                    ),
                    user = user,
                    memberPlans = if (data.member_plan_list.isNullOrEmpty()) {
                        null
                    } else {
                        buildMemberPlanVO(data.member_plan_list)
                    },
                    memberItemList = if (data.member_plan_list.isNullOrEmpty()) {
                        null
                    } else {
                        data.member_plan_list[0].member_item_list
                    },
                    adItems = adData

            )
        }
    }

    private fun buildDiscountPriceItemText(price: Double): CharSequence {
        val priceStr = if (price % 1 == 0.0) {
            price.toInt().toString()
        } else {
            price.toString()
        }
        return SpanUtils().append(ResourceUtils.getText(R.string.currency_unit))
                .setFontSize(12, true)
                .setForegroundColor(ContextCompat.getColor(context, R.color.member_price_color))
                .append(priceStr)
                .setForegroundColor(ContextCompat.getColor(context, R.color.member_price_color))
                .setFontSize(26, true)
                .setBold()
                .create()
    }

    private fun buildOriginalPriceItemText(price: String?): CharSequence? {
        if (price == null) {
            return null
        }
        return SpanUtils().append(ResourceUtils.getText(R.string.currency_unit))
                .setForegroundColor(ContextCompat.getColor(context, R.color.gray_level3))
                .append(price.toString())
                .setFontSize(12, true)
                .create()
    }

    private fun buildPayPriceItemText(price: Double): CharSequence {
        return SpanUtils().append(ResourceUtils.getText(R.string.currency_unit))
                .setFontSize(16, true)
                .setForegroundColor(ContextCompat.getColor(context, R.color.gray_level1))
                .setBold()
                .append(price.toString())
                .setForegroundColor(ContextCompat.getColor(context, R.color.gray_level1))
                .setFontSize(30, true)
                .setBold()
                .create()
    }

    fun buildMemberPlanVO(memberPlanList: List<MemberPlan>): List<MemberPlanVO> {
        val memberPlans = ArrayList<MemberPlanVO>()
        memberPlanList.forEach {
            val finalPrice: Double
            val originalPrice: String?
            var discountFlag: Boolean = "1" == it.discount_flag
            if ("1" == it.discount_flag && it.discount_price > 0.0 && it.discount_price < it.original_price) {
                //展示优惠价和原价
                finalPrice = it.discount_price
                originalPrice = if (it.original_price % 1 == 0.0) {
                    it.original_price.toInt().toString()
                } else {
                    it.original_price.toString()
                }
            } else {
                //只展示原价
                finalPrice = it.original_price
                originalPrice = null
                discountFlag = false
            }
            memberPlans.add(MemberPlanVO(
                    discountPriceCharSequence = buildDiscountPriceItemText(finalPrice),
                    finalPrice = finalPrice,
                    originalPriceCharSequence = buildOriginalPriceItemText(originalPrice),
                    planName = it.plan_name,
                    planId = it.plan_id,
                    planLabel = it.plan_label,
                    remark = it.remark,
                    payGoodsText = ResourceUtils.getText(R.string.pay_amount),
                    payPriceText = buildPayPriceItemText(finalPrice),
                    discountEnable = discountFlag,
                    discountEndTime = it.discount_end_time
            ))
        }
        return memberPlans
    }

    fun transformWxPayInfo(wxPayInfo: WXPayInfo): PayInfo? {
        val payInfo = PayInfo()
        payInfo.appId = wxPayInfo.appid
        payInfo.nonceStr = wxPayInfo.nonce_str
        payInfo.partnerId = wxPayInfo.mch_id
        payInfo.timestamp = wxPayInfo.time_stamp
        payInfo.`package` = "Sign=WXPay"
        payInfo.sign = wxPayInfo.pay_sign
        //服务端返回："package_str": "prepay_id=wxxxxx"
        val prepayId = wxPayInfo.package_str.split("=")
        if (prepayId.size == 2) {
            payInfo.prepayId = prepayId[1]
        }
        return payInfo
    }
}