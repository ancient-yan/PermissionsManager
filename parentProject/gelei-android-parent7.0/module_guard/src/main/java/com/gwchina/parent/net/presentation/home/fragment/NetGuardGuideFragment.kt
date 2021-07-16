package com.gwchina.parent.net.presentation.home.fragment

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.data.onSuccessWithData
import com.android.base.rx.bindLifecycle
import com.android.base.utils.android.compat.SystemBarCompat
import com.android.sdk.net.NetContext
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.net.NetGuardNavigator
import com.gwchina.parent.net.presentation.home.viewmodel.NetGuardViewModel
import com.gwchina.parent.net.widget.GreenBrowserDialog
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.api.YES_FLAG
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.isAndroid
import com.gwchina.sdk.base.utils.SuperviseModeSynchronizer
import com.gwchina.sdk.base.utils.setStatusBarLightMode
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import kotlinx.android.synthetic.main.net_fragment_guide.*
import javax.inject.Inject

/**
 *@author hujie
 *      Email: hujie1991@126.com
 *      Date : 2019-08-22 10:14
 */
class NetGuardGuideFragment : InjectorBaseStateFragment() {

    override fun provideLayout() = R.layout.net_fragment_guide

    @Inject
    lateinit var appDataSource: AppDataSource
    @Inject
    lateinit var netGuardNavigator: NetGuardNavigator
    private lateinit var viewModel: NetGuardViewModel
    private var isInstallGreenNet: Boolean = false
    private lateinit var superviseModeSynchronizer: SuperviseModeSynchronizer
    private var isAndroidDevice = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //修改状态栏颜色和字体颜色
        activity?.setStatusBarLightMode()
        SystemBarCompat.setStatusBarColor(activity, Color.WHITE)
        viewModel = getViewModel(viewModelFactory)
        isAndroidDevice = appDataSource.user().currentDevice.isAndroid()
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        setupListener()
        queryAppInfo()
        if (!isAndroidDevice) {
            //是IOS设备才去同步监督模式
            superviseModeSynchronizer = SuperviseModeSynchronizer(viewModel.childUserId, viewModel.childDeviceId)
            subscribeSuperviseMode()
        }
    }

    private fun setupListener() {
        sbOpenNetGuard.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (!NetContext.get().connected()) {
                    showMessage(R.string.error_net_error)
                    sbOpenNetGuard.setCheckedNoEvent(false)
                    return@setOnCheckedChangeListener
                }
                val vipRule = appDataSource.user().vipRule
                val isCanNet = vipRule?.home_net_enter == FLAG_POSITIVE_ACTION
                if (!isCanNet) {
                    OpenMemberDialog.show(requireContext()) {
                        message = requireContext().getString(R.string.net_guard_need_to_open_member_mask, vipRule?.home_net_enter_minimum_level)
                        messageDesc = requireContext().getString(R.string.open_member_to_get_more_function_tips_mask, vipRule?.home_net_enter_minimum_level)
                        positiveText = requireContext().getString(R.string.open_vip_to_experience_mask, vipRule?.home_net_enter_minimum_level)
                    }
                    sbOpenNetGuard.setCheckedNoEvent(false)
                } else if (isAndroidDevice && !isInstallGreenNet) {
                    //显示安装绿网浏览器的弹窗
                    GreenBrowserDialog(requireContext(), onConfirmListener = { exitFragment() }).show()
                    sbOpenNetGuard.setCheckedNoEvent(false)
                } else if (!isAndroidDevice) {
                    //ios设备需要同步守护模式状态
                    superviseModeSynchronizer.startSync()
                } else {
                    //调用接口开启上网守护
                    updateIntelligentControll(true)
                }
            }
        }
    }

    private fun subscribeSuperviseMode() {
        superviseModeSynchronizer.state.observe(this, Observer {
            it?.onSuccessWithData { result ->
                if (result) {
                    updateIntelligentControll(true)
                } else {
                    dismissLoadingDialog()
                    netGuardNavigator.openIosSuperviseModePage()
                    sbOpenNetGuard.setCheckedNoEvent(false)
                }
            }?.onLoading {
                showLoadingDialog(false)
            }?.onError { err ->
                dismissLoadingDialog()
                errorHandler.handleError(err)
            }
        })
    }

    private fun queryAppInfo() {
        if (!isAndroidDevice) {
            isInstallGreenNet = true
            refreshCompleted()
            return
        }
        viewModel.queryAppInfo()
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    isInstallGreenNet = it.rule_id.isNullOrEmpty() == false
                    checkInstallGreenNet()
                }, {
                    isInstallGreenNet = true
                    refreshCompleted()
                })

    }

    private fun updateIntelligentControll(isChecked: Boolean) {
        viewModel.updateIntelligentControll(isChecked).observe(this, Observer {
            it?.onSuccess {
                dismissLoadingDialog()
                appDataSource.user().currentDevice?.setting_url_pattern_flag = YES_FLAG
                netGuardNavigator.openNetGuardPage()
            }?.onError {
                dismissLoadingDialog()
                sbOpenNetGuard.setCheckedNoEvent(false)
            }?.onLoading {
                showLoadingDialog(false)
            }
        })
    }

    private fun checkInstallGreenNet() {
        if (isRefreshing) {
            refreshCompleted()
        } else if (!isInstallGreenNet) {
            GreenBrowserDialog(requireContext(), onConfirmListener = { exitFragment() }).show()
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        queryAppInfo()
    }
}

