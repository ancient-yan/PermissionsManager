package com.gwchina.parent.member.presentation.purchase

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.app.ui.processResultWithStatus
import com.android.base.rx.SchedulerProvider
import com.android.base.utils.android.ResourceUtils
import com.android.base.utils.android.compat.SystemBarCompat
import com.android.sdk.social.ali.AliPayExecutor
import com.android.sdk.social.ali.PayResultCallback
import com.android.sdk.social.common.Status
import com.android.sdk.social.wechat.PayInfo
import com.android.sdk.social.wechat.WeChatManager
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.MemberNavigator
import com.gwchina.parent.member.data.AliPayInfo
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.widget.dialog.TipsManager
import io.reactivex.functions.Consumer
import timber.log.Timber
import javax.inject.Inject

/**
 * 购买会员页面
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 14:49
 */
@Suppress("INACCESSIBLE_TYPE")
class PurchaseMemberFragment : InjectorBaseStateFragment(), PayResultCallback {

    @Inject lateinit var memberNavigator: MemberNavigator
    @Inject lateinit var schedulerProvider: SchedulerProvider
    private lateinit var purchaseViewModel: PurchaseMemberViewModel

    var purchaseLayoutManager: PurchaseLayoutManager? = null

    val weChatPayManager: WeChatManager = WeChatManager.getInstance()

    private val interactor by lazy {
        PurchaseInteractor(this, memberNavigator, purchaseViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        purchaseViewModel = getViewModel(viewModelFactory)
        subscribeToViewModel()
        SystemBarCompat.setStatusBarColor(activity, resources.getColor(R.color.transparent))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.member_fragment_purchase, container, false)

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        purchaseLayoutManager = PurchaseLayoutManager(this, interactor)
        autoRefresh()
    }

    override fun onResume() {
        SystemBarCompat.setStatusBarColor(activity, resources.getColor(R.color.transparent))
        super.onResume()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && isAdded) {
            SystemBarCompat.setStatusBarColor(activity, resources.getColor(R.color.transparent))
        }
    }

    override fun onRefresh() {
        purchaseViewModel.loadPurchaseData()
    }

    private fun subscribeToViewModel() {
        purchaseViewModel.purchaseData
                .observe(this, Observer { resource ->
                    resource ?: return@Observer
                    when {
                        resource.isLoading -> {
                        }
                        resource.isError -> {
                            processErrorWithStatus(resource.error())
                        }
                        resource.isSuccess -> processResultWithStatus(resource.orElse(null)) {
                            purchaseLayoutManager?.showData(it)
                        }
                    }
                })
        purchaseViewModel.wxPayData
                .observe(this, Observer { resource ->
                    resource ?: return@Observer
                    when {
                        resource.isLoading -> {
                            showLoadingDialog(R.string.user_member_submit_order_loading, true)
                        }
                        resource.isError -> {
                            dismissLoadingDialog()
                            errorHandler.handleError(resource.error())
                        }
                        resource.isSuccess -> {
                            dismissLoadingDialog()
                            if (resource.hasData()) {
                                doWxPay(resource.data())
                            }
                        }
                    }
                })
        purchaseViewModel.aliPayData
                .observe(this, Observer { resource ->
                    resource ?: return@Observer
                    when {
                        resource.isLoading -> {
                            showLoadingDialog(R.string.user_member_submit_order_loading, true)
                        }
                        resource.isError -> {
                            dismissLoadingDialog()
                            errorHandler.handleError(resource.error())
                        }
                        resource.isSuccess -> {
                            dismissLoadingDialog()
                            if (resource.hasData()) {
                                doAliPay(resource.data())
                            } else {

                            }
                        }
                    }
                })

        weChatPayManager.wxPayResultData
                .observe(this, Observer {
                    it ?: return@Observer
                    when (it.status) {
                        Status.STATE_CANCEL -> {
                            onPayCancel()
                        }
                        Status.STATE_FAILED -> {
                            onPayFail(it.error.message)
                        }
                        else -> {
                            onPaySuccess()
                        }
                    }
                })
    }

    private fun doAliPay(aliPayInfo: AliPayInfo) {
        AliPayExecutor.doAliPay(activity, aliPayInfo.content)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(AliPayExecutor.PayConsumer(this), Consumer {
                    errorHandler.handleError(it)
                })
    }

    private fun doWxPay(payInfo: PayInfo) {
        weChatPayManager.doPay(payInfo)
    }

    override fun onPayCancel() {
        TipsManager.showMessage(ResourceUtils.getText(R.string.pay_canceled))
    }

    override fun onPayFail(errStr: String?) {
        TipsManager.showMessage(getString(R.string.pay_error))
    }

    override fun onPaySuccess() {
        if (purchaseViewModel.orderNoData.value != null) {
            memberNavigator.paySuccess(purchaseViewModel.orderNoData.value!!)
        }
    }

    override fun onPayNeedConfirmResult() {
        Timber.d("支付需要确认")
        TODO("确认支付结果：查询订单状态")
    }
}