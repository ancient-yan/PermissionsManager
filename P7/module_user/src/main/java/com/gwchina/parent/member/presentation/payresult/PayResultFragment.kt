package com.gwchina.parent.member.presentation.payresult

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.app.ui.processResultWithStatus
import com.android.base.kotlin.gone
import com.android.base.kotlin.ifNonNull
import com.android.base.kotlin.visible
import com.android.base.utils.android.ResourceUtils
import com.android.base.utils.android.compat.SystemBarCompat
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.MemberNavigator
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.utils.enableSpanClickable
import kotlinx.android.synthetic.main.member_fragment_pay_result.*
import javax.inject.Inject

/**
 * 支付结果
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 14:49
 */
@Suppress("INACCESSIBLE_TYPE")
class PayResultFragment : InjectorBaseStateFragment() {

    companion object {

        private const val KEY_ORDER_NO = "order_no_key"

        fun newInstance(orderNo: String) = PayResultFragment().apply {
            arguments = Bundle().apply {
                putString(KEY_ORDER_NO, orderNo)
            }
        }
    }

    @Inject
    lateinit var memberNavigator: MemberNavigator
    private lateinit var payResultViewModel: PayResultViewModel
    private lateinit var orderNo: String
    private var isNeedSyncUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemBarCompat.setStatusBarColor(activity, resources.getColor(R.color.transparent))

        payResultViewModel = getViewModel(viewModelFactory)
        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.member_fragment_pay_result, container, false)

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        orderNo = arguments?.getString(KEY_ORDER_NO)!!

        autoRefresh()
        btnPayComplete.setOnClickListener {
            memberNavigator.backToMemberCenter()
        }
    }

    override fun onRefresh() {
        payResultViewModel.searchOrder(orderNo)
    }

    private fun subscribeToViewModel() {
        payResultViewModel.orderData
                .observe(this, Observer { resource ->
                    resource ?: return@Observer
                    when {
                        resource.isLoading -> {
                        }
                        resource.isError -> {
                            processErrorWithStatus(resource.error())
                        }
                        resource.isSuccess -> processResultWithStatus(resource.orElse(null)) {
                            it.ifNonNull {
                                showData(it)
                            }
                        }
                    }
                })
    }

    private fun showData(it: PayResultVO) {
        tvPayPrice.text = it.payPrice
        tvPayGoods.text = it.payGoods
        tvMemberOrderNoValue.text = it.orderNo
        tvMemberPayTypeValue.text = it.payType
        tvPayResultText.text = it.status
        ivPayResultIcon.setImageResource(it.payStatusIconRes)
        if (it.isPayProblems) {
            tvMemberPayProblems.text = buildPayProblemItemText()
            tvMemberPayProblems.enableSpanClickable()
            tvMemberPayProblems.visible()
            btnPayComplete.gone()
        } else {
            tvMemberPayProblems.gone()
            btnPayComplete.visible()
            //购买成功，点击返回，刷新用户会员信息
            gtlMemberTitle.setOnNavigationOnClickListener {
                memberNavigator.backToMemberCenter()
            }
        }
    }

    private fun buildPayProblemItemText(): CharSequence {
        val customerSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                memberNavigator.openCustomerPage()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.color = ContextCompat.getColor(context!!, R.color.blue_level2)
                ds.isUnderlineText = false
            }
        }
        return SpanUtils().append(ResourceUtils.getText(R.string.pay_has_problem))
                .setFontSize(12, true)
                .setForegroundColor(ContextCompat.getColor(context!!, R.color.gray_level2))
                .append(ResourceUtils.getText(R.string.pay_contact_customer_service))
                .setForegroundColor(ContextCompat.getColor(context!!, R.color.green_main))
                .setClickSpan(customerSpan)
                .setFontSize(12, true)
                .setBold()
                .create()
    }
}