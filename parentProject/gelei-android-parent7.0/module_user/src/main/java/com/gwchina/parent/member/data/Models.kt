package com.gwchina.parent.member.data

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 14:02
 */
/**微信支付报文*/
data class WXPayInfo(
        val appid: String,
        val code_url: String?,
        val mweb_url: String,
        val mch_id: String,
        val nonce_str: String,
        val order_amount: Int,
        val order_desc: String,
        val order_id: String,
        val order_no: String,
        val out_trade_no: String,
        val package_str: String,
        val pay_order_no: String,
        val pay_sign: String,
        val sign_type: String,
        val time_stamp: String,
        val trade_type: String
)

/**支付宝支付报文*/
data class AliPayInfo(
        val content: String,
        val order_amount: Int,
        val order_desc: String,
        val order_id: String,
        val order_no: String,
        val pay_order_no: String
)

/**会员信息*/
data class MemberInfo(
        val end_time: Long?,
        val member_item_list: List<MemberItem>? = null,
        val member_service_list: List<MemberItem>? = null
)

/**会员特权*/
data class MemberItem(
        val member_item_flag: String? = "",
        val member_item_icon: String? = "",
        val member_item_name: String? = "",
        val member_item_desc: String? = ""
)

/**购买记录*/
data class PurchaseRecord(
        val create_time: Long,
        val order_amount: Double,
        val order_no: String,
        val original_price: Double,
        val pay_type: String,
        val pay_type_name: String,
        val plan_code: String,
        val plan_name: String,
        val record_id: String,
        val status: String
)

/**购买会员*/
data class PurchaseInfo(
        val begin_time: Long,
        val end_time: Long = 0,
        val head_photo_path: String,
        val member_level_desc: String,
        val member_level_name: String,
        val member_plan_list: List<MemberPlan>,
        val nick_name: String,
        //00-待生效、01-已生效、02-已失效
        val status: String,
        val banner_list: Banner?
)

/**会员购买项目*/
data class MemberPlan(
        val discount_price: Double = 0.00,
        val discount_flag: String?,
        val member_item_list: List<MemberItem>,
        val original_price: Double = 0.00,
        val plan_label: String,
        val plan_id: String,
        val plan_name: String,
        val valid_days: String,
        val remark: String?,
        val discount_start_time: Long,
        val discount_end_time: Long
)

/**
 * 横幅广告
 */
data class Banner(
        val banner_img: String
)

/**
 * 订单信息
 */
data class OrderInfo(
        val is_delete: String,
        val order_amount: Double,
        val order_attach: Any,
        val order_desc: String,
        val order_id: String,
        val order_no: String,
        val order_time: Long,
        val order_type: String,
        val pay_order_no: String,
        val pay_time: String,
        val pay_type: String,
        val refund_order_no: Any,
        val refund_time: String?,
        val remark: String?,
        val status: String,
        val user_id: String
)

