package com.gwchina.parent.net.presentation.home.fragment

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Observer
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.*
import com.android.base.rx.bindLifecycle
import com.android.base.utils.android.TintUtils
import com.android.base.utils.android.compat.SystemBarCompat
import com.android.sdk.net.NetContext
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.net.NetGuardNavigator
import com.gwchina.parent.net.data.model.RuleUrlInfo
import com.gwchina.parent.net.presentation.home.viewmodel.NetGuardViewModel
import com.gwchina.parent.net.widget.GreenBrowserDialog
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.api.INSTRUCTION_SYNC_URL
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.isAndroid
import com.gwchina.sdk.base.sync.SyncManager
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.setStatusBarDarkMode
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import com.gwchina.sdk.base.widget.views.InstructionStateView
import kotlinx.android.synthetic.main.net_fragment_guard.*
import javax.inject.Inject


class NetGuardFragment : InjectorBaseStateFragment() {

    companion object {
        private const val NET_CONFIRM_DO_NOT_TIPS_FLAG = "net_confirm_do_not_tips_flag"
    }

    @Inject
    lateinit var netGuardNavigator: NetGuardNavigator

    @Inject
    lateinit var appDataSource: AppDataSource
    private var isInstallGreenNet: Boolean = true
    private var isAndroidDevice = false
    private lateinit var deviceName: String

    private val navigationIcon by lazy {
        TintUtils.tint(drawableFromId(R.drawable.icon_back)?.mutate(), colorFromId(R.color.white))
    }

    private lateinit var viewModel: NetGuardViewModel

    override fun provideLayout(): Any? = R.layout.net_fragment_guard

    private lateinit var mIsvNet: InstructionStateView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //修改状态栏颜色和字体颜色
        activity?.setStatusBarDarkMode()
        SystemBarCompat.setStatusBarColor(activity, Color.parseColor("#4FCA8F"))
        viewModel = getViewModel(viewModelFactory)
        isAndroidDevice = appDataSource.user().currentDevice.isAndroid()
        subscribeViewModel()
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_GUARDWEB)
    }

    private fun setupSync() {
        val childDeviceId = viewModel.appDataSource.user().currentDevice!!.device_id
        deviceName = viewModel.appDataSource.user().currentDevice?.device_name ?: ""
        val childId = viewModel.appDataSource.user().currentChild?.child_user_id!!
        SyncManager.getInstance().getLocalBroadcastManager(requireContext()).registerReceiver(localReceiver, IntentFilter(SyncManager.getInstance().getIntentFilterAction(INSTRUCTION_SYNC_URL, childDeviceId)))
        SyncManager.getInstance().querySyncState(requireContext(), INSTRUCTION_SYNC_URL, childId, childDeviceId) { isSuccess ->
            if (isSuccess) {
                isvNet.gone()
            } else {
                isvNet.showInitFailedState(getString(R.string.instruction_net_name))
            }
        }

        isvNet.onRetryClickListener = {
            SyncManager.getInstance().sendSync(childDeviceId, INSTRUCTION_SYNC_URL, childId)
        }
    }


    private fun subscribeViewModel() {
        viewModel.ruleUrlInfo
                .observe(this, Observer {
                    it?.let {
                        processResource(it)
                    }
                })

        queryAppInfo()
    }

    private fun queryAppInfo() {
        if (!isAndroidDevice) {
            isInstallGreenNet = true
            return
        }
        viewModel.queryAppInfo()
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    isInstallGreenNet = it.rule_id.isNullOrEmpty() == false
                    checkInstallGreenNet()
                }, {
                    isInstallGreenNet = true
                })

    }

    private fun checkInstallGreenNet() {
        if (!isRefreshing && !isInstallGreenNet) {
            GreenBrowserDialog(requireContext(), onConfirmListener = { exitFragment() }).show()
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        queryAppInfo()
        viewModel.getRuleUrlBaseInfo()
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        mIsvNet = isvNet
        net_title.toolbar.apply {
            setTitleTextColor(Color.WHITE)
            navigationIcon = this@NetGuardFragment.navigationIcon
            if (isAndroidDevice) {
                inflateMenu(R.menu.net_intercept_record_menu)
                setOnMenuItemClickListener {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_INTERCEPT)
                    when (it.itemId) {
                        R.id.intercapt_record -> netGuardNavigator.openNetGuardRecordPage()
                    }

                    true
                }
            }
        }

        net_sb_intelligent_control.setOnCheckedChangeListener { _, isChecked ->

            if (!NetContext.get().connected()) {
                showMessage(R.string.error_net_error)
                net_sb_intelligent_control.setCheckedNoEvent(!isChecked)
                return@setOnCheckedChangeListener
            }
            if (!isChecked && !AppSettings.settingsStorage().getBoolean(NET_CONFIRM_DO_NOT_TIPS_FLAG, false)) {
                showCloseGuardDialog(isChecked)
                return@setOnCheckedChangeListener
            }
            if (isChecked) {
                val vipRule = appDataSource.user().vipRule
                val isCanNet = vipRule?.home_net_enter == FLAG_POSITIVE_ACTION
                if (!isCanNet) {
                    OpenMemberDialog.show(requireContext()) {
                        message = requireContext().getString(R.string.net_guard_need_to_open_member_mask, vipRule?.home_net_enter_minimum_level)
                        messageDesc = requireContext().getString(R.string.open_member_to_get_more_function_tips_mask, vipRule?.home_net_enter_minimum_level)
                        positiveText = requireContext().getString(R.string.open_vip_to_experience_mask, vipRule?.home_net_enter_minimum_level)
                    }
                    net_sb_intelligent_control.setCheckedNoEvent(!isChecked)
                    return@setOnCheckedChangeListener
                } else if (isAndroidDevice && !isInstallGreenNet) {
                    net_sb_intelligent_control.setCheckedNoEvent(!isChecked)
                    GreenBrowserDialog(requireContext(), onConfirmListener = { exitFragment() }).show()
                    return@setOnCheckedChangeListener
                }
            }
            updateIntelligentControll(isChecked, true)
        }

        net_cl_internet_manage.setOnClickListener {
            netGuardNavigator.openNetWorkManagementPage()
            StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_WEBMANAGE)
        }

        deviceShow()
        setupSync()
    }

    private fun showCloseGuardDialog(isChecked: Boolean) {
        showConfirmDialog {
            messageId = R.string.intelligent_control_close_reminders
            positiveId = R.string.i_got_it
            checkBoxId = R.string.do_not_tips_again
            checkBoxChecked = true
            noNegative()
            positiveListener2 = { _, checked ->
                AppSettings.settingsStorage().putBoolean(NET_CONFIRM_DO_NOT_TIPS_FLAG, checked)
                updateIntelligentControll(isChecked, false)
            }
        }?.setCanceledOnTouchOutside(false)
    }

    private fun updateIntelligentControll(isChecked: Boolean, isShowCloseToast: Boolean) {
        StatisticalManager.onEvent(if (isChecked) UMEvent.ClickEvent.GUARDWEB_BTN_OPEN else UMEvent.ClickEvent.GUARDWEB_BTN_CLOSE)
        viewModel.updateIntelligentControll(isChecked).observe(this, Observer { it ->
            it?.onSuccess {
                dismissLoadingDialog()
                if (net_sb_intelligent_control.isChecked) {
                    net_tv_intelligent_control.visibility = View.VISIBLE
                } else {
                    net_tv_intelligent_control.visibility = View.GONE
                    if (isShowCloseToast) {
                        ToastUtils.showShort(R.string.intelligent_control_off)
                    }
                }
            }?.onError {
                showMessage(errorHandler.createMessage(it))
                net_sb_intelligent_control.setCheckedImmediatelyNoEvent(!net_sb_intelligent_control.isChecked)
                dismissLoadingDialog()
            }?.onLoading {
                showLoadingDialog(false)
            }
        })
    }

    private fun deviceShow() {
        val childList = appDataSource.user().childList
        if (childList != null && childList.size > 1) {
            net_tv_device_info.visible()
            net_tv_device_info.text = appDataSource.user().currentChild?.nick_name
        } else {
            net_tv_device_info.invisible()
        }
    }


    private fun processResource(resource: Resource<RuleUrlInfo>) {
        resource.onSuccess {
            it?.let {

                if (it.week_intercept_count <= 0 && it.total_intercept_count <= 0) {
                    net_tv_no_intercapt_tips.visible()
                    net_cl_intercept_count.gone()
                } else {
                    net_tv_no_intercapt_tips.gone()
                    net_cl_intercept_count.visible()
                    net_tv_week_intercept_count.text = "${it.week_intercept_count}"
                    net_tv_total_intercept_count.text = "${it.total_intercept_count}"
                }

                if (viewModel.childDeviceIsAndroid) {
                    net_cl_guard_switch.visibility = View.VISIBLE
                    net_sb_intelligent_control.isEnabled = true
                    if (it.pattern_type == "0") {
                        //没有开启
                        net_sb_intelligent_control.setCheckedNoEvent(false)
                        net_tv_intelligent_control.visibility = View.GONE
                    } else if (it.pattern_type == "1") {
                        net_sb_intelligent_control.setCheckedNoEvent(true)
                        net_tv_intelligent_control.visibility = View.VISIBLE
                    }
                } else {
                    net_tv_intelligent_control.visibility = View.GONE
                    net_cl_guard_switch.visibility = View.GONE
                }
                if (isRefreshing) {
                    refreshCompleted()
                }
            }
        }.onError {
            showMessage(errorHandler.createMessage(it))
            if (isRefreshing) {
                refreshCompleted()
            }
        }
    }

    private val localReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val deviceId = intent?.getStringExtra(SyncManager.DEVICE_ID)
            val action = SyncManager.getInstance().intervalHandlerMap["$deviceId-$INSTRUCTION_SYNC_URL"]?.childDeviceId
            if (action != null && ("$action-$INSTRUCTION_SYNC_URL") == intent?.action) {
                when (intent.getIntExtra(SyncManager.SYNC_STATE, 0)) {
                    1 -> mIsvNet.showSyncingState(getString(R.string.instruction_net_name))
                    2 -> mIsvNet.showSyncFailedState(getString(R.string.instruction_net_name), deviceName)
                    3 -> mIsvNet.gone()
                }
            }
        }
    }

    override fun onDestroy() {
        SyncManager.getInstance().getLocalBroadcastManager(requireContext()).unregisterReceiver(localReceiver)
        super.onDestroy()
    }
}