package com.gwchina.parent.member.presentation.center

import android.arch.lifecycle.Observer
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.app.ui.processResultWithStatus
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.drawableFromId
import com.android.base.rx.SchedulerProvider
import com.android.base.utils.android.ResourceUtils
import com.android.base.utils.android.TintUtils
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
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.setStatusBarDarkMode
import com.gwchina.sdk.base.widget.dialog.TipsManager
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.member_fragment_center.*
import timber.log.Timber
import javax.inject.Inject

/**
 * 会员中心页面
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 10:46
 */
class MemberCenterFragment : InjectorBaseStateFragment(), PayResultCallback {

    @Inject
    lateinit var schedulerProvider: SchedulerProvider

    companion object {

        private const val KEY_IS_SYNC_USER_DATA = "sync_user_data"

        fun newInstance(isSyncUserData: Boolean) = MemberCenterFragment().apply {
            arguments = Bundle().apply {
                putBoolean(KEY_IS_SYNC_USER_DATA, isSyncUserData)
            }
        }
    }

    private val navigationIcon by lazy {
        TintUtils.tint(drawableFromId(R.drawable.icon_back)?.mutate(), colorFromId(R.color.white))
    }
    val weChatPayManager: WeChatManager = WeChatManager.getInstance()

    private lateinit var memberViewModel: MemberCenterViewModel
    @Inject
    lateinit var memberNavigator: MemberNavigator
    @Inject
    lateinit var memberDataMapper: MemberDataMapper
    private lateinit var centerLayoutManager: CenterLayoutManager
    private var isSyncUserData: Boolean = false
    private val interactor by lazy {
        MemberCenterInteractor(this, memberNavigator,memberViewModel)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemBarCompat.setStatusBarColor(activity, resources.getColor(R.color.memberCenterColorPrimary))
        memberViewModel = getViewModel(viewModelFactory)
        subscribeToViewModel()
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_MEMBERCENTER)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.member_fragment_center, container, false)
    }

    override fun onResume() {
        super.onResume()
        activity.setStatusBarDarkMode()
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        val isNeedSyncUserData = arguments?.getBoolean(KEY_IS_SYNC_USER_DATA, false)
        if (isNeedSyncUserData != null) {
            isSyncUserData = isNeedSyncUserData
        }
        centerLayoutManager = CenterLayoutManager(this, interactor)
        showLoadingLayout()
        gtlMemberTitle.toolbar.setTitleTextColor(Color.WHITE)
        gtlMemberTitle.toolbar.navigationIcon = navigationIcon

        tvBuyRecord.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.MEMBERCENTER_BTN_RECORD)
            memberNavigator.openHistory()
        }

    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden && isAdded) {
            SystemBarCompat.setStatusBarColor(activity, resources.getColor(R.color.memberCenterColorPrimary))
        }
    }

    private fun subscribeToViewModel() {
        memberViewModel.memberCenterVOData
                .observe(this, Observer { resource ->
                    resource ?: return@Observer
                    when {
                        resource.isError -> {
                            processErrorWithStatus(resource.error())
                            errorHandler.handleError(resource.error())
//                            llMemberOpen.visibility = View.GONE
                        }
                        resource.isSuccess -> {
                            val data: MemberCenterVO?
                            if (resource.hasData()) {
                                data = resource.data()
                            } else {
                                data = null
                            }
                            processResultWithStatus(data) { member ->
//                                llMemberOpen.visibility = View.VISIBLE
                                centerLayoutManager.bindDataVO(member)
                            }
                        }
                    }
                })

        memberViewModel.wxPayData
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

        memberViewModel.aliPayData
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

    override fun onRefresh() {
        memberViewModel.loadMemberPageData(isSyncUserData)
        isSyncUserData = false
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
        if (memberViewModel.orderNoData.value != null) {
            memberNavigator.paySuccess(memberViewModel.orderNoData.value!!)
        }
    }

    override fun onPayNeedConfirmResult() {
        Timber.d("支付需要确认")
        TODO("确认支付结果：查询订单状态")
    }

}