package com.gwchina.parent.member.presentation.purchase.plan

import android.graphics.Paint
import android.view.View
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.presentation.purchase.BaseItemViewHolder
import com.gwchina.parent.member.presentation.purchase.Cards
import com.gwchina.parent.member.presentation.purchase.MemberPlanVO
import com.gwchina.parent.member.presentation.purchase.PurchaseInteractor
import kotlinx.android.synthetic.main.member_purchase_plan_item.*

/**
 * 购买项目
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 11:01
 */
internal class PurchasePlanViewHolder(private val interactor: PurchaseInteractor, itemView: View) : BaseItemViewHolder<Cards.PlanCard>(interactor, itemView) {

    fun showData(memberPlanVO: MemberPlanVO) {
        val currentPosition = position - 1

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
        tvPurchaseDiscountPrice.text = memberPlanVO.discountPriceCharSequence
        if (memberPlanVO.originalPriceCharSequence != null && memberPlanVO.originalPriceCharSequence.isNotEmpty()) {
            tvPurchaseOriginPrice.text = memberPlanVO.originalPriceCharSequence
            tvPurchaseOriginPrice.visibility = View.VISIBLE
            tvPurchaseOriginPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            tvPurchaseOriginPrice.visibility = View.GONE
        }

        //remark
        if (memberPlanVO.remark != null && memberPlanVO.remark.isNotEmpty()) {
            tvPurchasePlanRemark.text = memberPlanVO.remark
        } else {
            tvPurchasePlanRemark.text = ""
        }

        //是否选中
        onSelectItemChanged(this, currentPosition, memberPlanVO)
        itemView.setOnClickListener {
            if (currentPosition == interactor.purchaseDataVO?.selectPlanIndex) return@setOnClickListener
            interactor.selectPlanItem(memberPlanVO, currentPosition)
            interactor.notifyItemChanged()
        }
    }

    private fun onSelectItemChanged(viewHolder: KtViewHolder, position: Int, memberPlanVO: MemberPlanVO) {
        if (interactor.purchaseDataVO?.selectPlanIndex == position) {
            viewHolder.ivPurchaseCornerMark.visibility = View.VISIBLE
            viewHolder.itemView.setBackgroundResource(R.drawable.member_purchase_shape_plan_round_selected)
            interactor.selectPlanItem(memberPlanVO, position)
        } else {
            viewHolder.ivPurchaseCornerMark.visibility = View.INVISIBLE
            viewHolder.itemView.setBackgroundResource(R.drawable.member_purchase_shape_plan_round_normal)
        }
    }
}
