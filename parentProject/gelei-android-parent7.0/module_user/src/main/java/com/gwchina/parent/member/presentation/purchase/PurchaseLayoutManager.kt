package com.gwchina.parent.member.presentation.purchase

import android.graphics.Rect
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.kotlin.dip
import com.android.base.kotlin.ifNonNull
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.android.base.utils.android.ResourceUtils
import com.android.base.utils.android.UnitConverter
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.user.R
import com.gwchina.sdk.base.utils.enableSpanClickable
import com.gwchina.sdk.base.widget.dialog.TipsManager
import kotlinx.android.synthetic.main.member_fragment_purchase.*
import me.drakeet.multitype.register
import timber.log.Timber

/**
 * 购买会员页面构造器
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-24 14:10
 */
class PurchaseLayoutManager(private val host: PurchaseMemberFragment, private val interactor: PurchaseInteractor) {
    private val context = host.context ?: throw NullPointerException("host is not attached")
    private val rvPurchaseList = host.rvPurchaseList
    val contentAdapter = MultiTypeAdapter(context)

    init {
        buildData()
        initContentListView()
        initPayLayout()
    }

    private fun initPayLayout() {
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
        host.tvMemberAgreement.text = SpanUtils()
                .append(ResourceUtils.getText(R.string.user_member_agreement_normal_span))
                .setForegroundColor(ContextCompat.getColor(context, R.color.gray_level3))
                .append(ResourceUtils.getText(R.string.user_member_agreement_click_span))
                .setForegroundColor(ContextCompat.getColor(context, R.color.blue_level2))
                .setClickSpan(agreementSpan)
                .create()
        host.tvMemberAgreement.enableSpanClickable()
        host.btnMemberPay.setOnClickListener {
            if (interactor.purchaseDataVO!!.memberPlans.isNullOrEmpty()) return@setOnClickListener
            val index = interactor.purchaseDataVO?.selectPlanIndex!!
            val memberPlanVO = interactor.purchaseDataVO?.memberPlans!![index]
            //有折扣，但已过期, 刷新购买项目
            if (memberPlanVO.discountEnable && interactor.isDiscountExpired(memberPlanVO)) {
                TipsManager.showMessage(host.getString(R.string.user_purchase_plan_discount_expired))
                host.autoRefresh()
                return@setOnClickListener
            }
            interactor.showPaymentDialogImmediately()
        }
    }

    private fun buildData() {
        interactor.purchaseDataVO.ifNonNull {
            contentAdapter.clear()
            userInfo.ifNonNull {
                contentAdapter.add(Cards.MemberInfoCard())
            }
            memberPlans?.forEach {
                contentAdapter.add(Cards.PlanCard(it))
            }
            if (!adItems.isNullOrEmpty()) {
                Timber.d("adItems:" + adItems.size)
                contentAdapter.add(Cards.AdCard())
            }
        }
    }

    private fun initContentListView() {
        with(rvPurchaseList) {
            layoutManager = android.support.v7.widget.LinearLayoutManager(context)
            addItemDecoration(ContentItemDecoration())
        }

        with(contentAdapter) {
            setHasStableIds(true)
            register(UserInfoItemViewBinder(interactor))
            register(PlanViewBinder(interactor))
            register(AdItemViewBinder(interactor))
        }

        rvPurchaseList.adapter = contentAdapter
    }

    fun showData(purchaseDataVO: PurchaseDataVO) {
        interactor.purchaseDataVO = purchaseDataVO
        buildData()
        if (purchaseDataVO.memberPlans.isNullOrEmpty()){
            host.btnMemberPay.invisible()
            host.tvMemberAgreement.invisible()
        }else{
            host.btnMemberPay.visible()
            host.tvMemberAgreement.visible()
        }
        contentAdapter.notifyDataSetChanged()
    }

    private class ContentItemDecoration : RecyclerView.ItemDecoration() {

        private val mItemVerticalMargin = UnitConverter.dpToPx(15)

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

            val position = parent.getChildAdapterPosition(view)
            val adapter = parent.adapter as MultiTypeAdapter

            when (adapter.getItem(position)) {
                is Cards.MemberInfoCard -> {
                    outRect.top = mItemVerticalMargin
                    outRect.bottom = dip(10)
                    outRect.left = 0
                    outRect.right = 0
                }
                is Cards.PlanCard -> {
                    outRect.top = mItemVerticalMargin
                    outRect.bottom = 0
                    outRect.left = 0
                    outRect.right = 0
                }
                is Cards.AdCard -> {
                    outRect.top = mItemVerticalMargin
                    outRect.bottom = 0
                    outRect.left = 0
                    outRect.right = 0
                }
            }
        }
    }
}