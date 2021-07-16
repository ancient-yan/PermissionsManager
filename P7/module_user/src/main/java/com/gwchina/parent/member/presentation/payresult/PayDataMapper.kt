package com.gwchina.parent.member.presentation.payresult

import android.content.Context
import android.support.v4.content.ContextCompat
import com.android.base.app.dagger.ContextType
import com.android.base.utils.android.ResourceUtils
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.data.OrderInfo
import javax.inject.Inject

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-28 15:06
 */
class PayDataMapper @Inject constructor(@ContextType private val context: Context) {

    fun transform(orderInfo: OrderInfo): PayResultVO {
        val isPayFailed = (orderInfo.status != "02" && orderInfo.status != "99")
        return PayResultVO(

                payPrice = buildPayPriceItemText(orderInfo.order_amount),
                payGoods = ResourceUtils.getString(R.string.purchase_member, orderInfo.order_desc),
                payType = when (orderInfo.pay_type) {
                    "01" -> ResourceUtils.getText(R.string.pay_wx)
                    "02" -> ResourceUtils.getText(R.string.pay_ali)
                    "03" -> ResourceUtils.getText(R.string.pay_union)
                    "05" -> ResourceUtils.getText(R.string.pay_ccb)
                    else -> ""
                },
                orderNo = orderInfo.order_no,
                status = when (orderInfo.status) {
                    "01" -> ResourceUtils.getText(R.string.pay_status_pre_pay)
                    "02" -> ResourceUtils.getText(R.string.pay_status_paid)
                    "03" -> ResourceUtils.getText(R.string.pay_status_pay_timeout)
                    "04" -> ResourceUtils.getText(R.string.pay_status_refund)
                    "99" -> ResourceUtils.getText(R.string.pay_status_order_complete)
                    "00" -> ResourceUtils.getText(R.string.pay_status_order_cancel)
                    else -> ""
                },
                payStatusIconRes = when (orderInfo.status) {
                    "01" -> R.drawable.member_icon_pre_pay
                    "02" -> R.drawable.member_icon_pay_success
                    "99" -> R.drawable.member_icon_pay_success
                    else -> {
                        //todo
                        R.drawable.member_icon_pre_pay
                    }
                },
                isPayProblems = isPayFailed

        )
    }

    private fun buildPayPriceItemText(price: Double): CharSequence {
        return SpanUtils().append(ResourceUtils.getText(R.string.currency_unit))
                .setFontSize(16, true)
                .setForegroundColor(ContextCompat.getColor(context, R.color.gray_level1))
                .append(price.toString())
                .setForegroundColor(ContextCompat.getColor(context, R.color.gray_level1))
                .setFontSize(30, true)
                .setBold()
                .create()
    }
}