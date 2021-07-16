package com.gwchina.parent.member.widget

import android.content.Context
import android.graphics.Paint
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.presentation.purchase.MemberPlanVO
import com.gwchina.sdk.base.utils.setTextWithTypeface
import kotlinx.android.synthetic.main.member_goods_item_layout.view.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2020-03-06 18:16
 *      会员中心中的商品列表
 */
class GoodsItemLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {


    init {
        View.inflate(context, R.layout.member_goods_item_layout, this)
    }

    private var isStateSelected: Boolean = false

    var onGoodsItemClick: ((Int,MemberPlanVO) -> Unit)? = null


     var memberPlanVO: MemberPlanVO? = null

    fun buildData(memberPlanVO: MemberPlanVO) {
        this.memberPlanVO = memberPlanVO
        //计划名称
        tvPurchasePlanName.text = memberPlanVO.planName

        //限时优惠
        if (memberPlanVO.planLabel != null && memberPlanVO.planLabel.isNotEmpty()) {
            tvPurchaseSpecialOffer.text = memberPlanVO.planLabel
            tvPurchaseSpecialOffer.visible()
        } else {
            tvPurchaseSpecialOffer.gone()
        }

        //原始价格
        tvPurchaseDiscountPrice.setTextWithTypeface(memberPlanVO.discountPriceCharSequence)
        if (memberPlanVO.originalPriceCharSequence != null && memberPlanVO.originalPriceCharSequence.isNotEmpty()) {
            tvPurchaseOriginPrice.text = memberPlanVO.originalPriceCharSequence
            tvPurchaseOriginPrice.visibility = View.VISIBLE
            tvPurchaseOriginPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tvPurchaseOriginPrice.visibility = View.INVISIBLE
        }

        //remark
        if (memberPlanVO.remark != null && memberPlanVO.remark.isNotEmpty()) {
            tvPurchasePlanRemark.text = memberPlanVO.remark
        } else {
            tvPurchasePlanRemark.text = ""
        }

        this.setOnClickListener { goodsItemLayout ->
            if (isStateSelected) return@setOnClickListener
            if (goodsItemLayout.parent is LinearLayout) {
                val parentView = goodsItemLayout.parent as LinearLayout
                for (i in 0 until parentView.childCount) {
                    if (parentView.getChildAt(i) is GoodsItemLayout) {
                        val itemLayout = parentView.getChildAt(i) as GoodsItemLayout
                        if (itemLayout == goodsItemLayout) {
                            itemLayout.setSelectedState()
                            itemLayout.onGoodsItemClick?.invoke(i,memberPlanVO)
                        } else {
                            itemLayout.resetSelectedState()
                        }
                    }
                }
            }
        }
    }

    fun setSelectedState() {
        if (isStateSelected) return
        goodsLayout.setBackgroundResource(R.drawable.goods_selected_shape)
        isStateSelected = false
    }

    fun resetSelectedState() {
        goodsLayout.setBackgroundResource(R.drawable.goods_normal_shape)
        isStateSelected = false
    }


}