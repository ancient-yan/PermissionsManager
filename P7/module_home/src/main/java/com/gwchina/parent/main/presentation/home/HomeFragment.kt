package com.gwchina.parent.main.presentation.home

import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onSuccess
import com.android.base.kotlin.*
import com.android.base.utils.android.compat.SystemBarCompat
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.MainActivity
import com.gwchina.parent.main.MainFragment
import com.gwchina.parent.main.MainNavigator
import com.gwchina.parent.main.data.PhoneApprovalInfo
import com.gwchina.parent.main.data.Soft
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO.SoftWrapper
import com.gwchina.parent.main.presentation.home.card.isDeviceNoPermission
import com.gwchina.parent.main.presentation.home.card.isPermissionLose
import com.gwchina.parent.main.utils.buildFlag
import com.gwchina.parent.main.utils.showPermissionTipDialog
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.data.models.*
import com.gwchina.sdk.base.sync.SyncManager
import com.gwchina.sdk.base.widget.member.MemberExpiredForceChooseTask
import com.gwchina.sdk.base.widget.member.MemberExpiringTipsTask
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.home_using_statistics_pop.*
import javax.inject.Inject

/**
 * 首页卡片
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-02 14:40
 */
class HomeFragment : InjectorBaseStateFragment(), MainFragment.MainFragmentChild {

    @Inject
    lateinit var mainNavigator: MainNavigator

    private lateinit var topLayoutPresenter: TopLayoutPresenter
    private lateinit var homeLocalBroadcastPresenter: HomeLocalBroadcastPresenter

    internal val viewModel by lazy {
        getViewModel<HomeViewModel>(viewModelFactory)
    }

    private val cardInteractor by lazy {
        CardInteractor(this, mainNavigator, CardDataProviderImpl())
    }

    private val cardDataDispatcher = CardDataDispatcher()

    internal lateinit var deviceName: String
    //会员到期/过期的弹窗是否已经弹过
    private var isVipExpiredDialogShowed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.home_fragment

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        //setup status if need
        ifSDKAtLeast(19) {
            flHomeNetError.setPaddingTop(flHomeNetError.paddingTop + SystemBarCompat.getStatusBarHeight(context))
        }
        topLayoutPresenter = TopLayoutPresenter(this, viewModel)
        homeLocalBroadcastPresenter = HomeLocalBroadcastPresenter(this)
        //setup NetworkStatusListener
        view.post {
            newNetworkListener(this@HomeFragment) {
                autoRefresh()
            }.start()
        }

        //setup card
        setupCards()
    }

    private fun setupCards() {
        topDeviceCard.setup(cardInteractor)
        weeklyCard.setup(cardInteractor)
        approvalCard.setup(cardInteractor)
        usingStatisticsCard.setup(cardInteractor)
        locationCard.setup(cardInteractor)
        appRecommendationCard.setup(cardInteractor)
        usingTrajectoryCard.setup(cardInteractor)

        instructionStateCard.onRetryClickListener = {
            val childDeviceId = AppContext.appDataSource().user().currentDevice?.device_id
            val childUserId = AppContext.appDataSource().user().currentChild?.child_user_id
            if (childDeviceId != null && childUserId != null)
                SyncManager.getInstance().sendSync(childDeviceId, it, childUserId)
        }
    }

    private fun registerReceiver(user: User) {
        val currentDeviceId = user.currentChildDeviceId ?: return
        val phone = INSTRUCTION_SYNC_PHONE + currentDeviceId
        val time = INSTRUCTION_SYNC_TIME + currentDeviceId
        val app = INSTRUCTION_SYNC_APP + currentDeviceId
        val net = INSTRUCTION_SYNC_URL + currentDeviceId
        val level = INSTRUCTION_SYNC_LEVEL + currentDeviceId
        registerReceiver(phone, INSTRUCTION_SYNC_PHONE, currentDeviceId)
        registerReceiver(time, INSTRUCTION_SYNC_TIME, currentDeviceId)
        registerReceiver(app, INSTRUCTION_SYNC_APP, currentDeviceId)
        registerReceiver(net, INSTRUCTION_SYNC_URL, currentDeviceId)
        registerReceiver(level, INSTRUCTION_SYNC_LEVEL, currentDeviceId)
    }

    private fun registerReceiver(flag: String, instructionName: String, deviceId: String) {
        if (SyncManager.getInstance().localBroadcastRegisterMap[flag] == null || !SyncManager.getInstance().localBroadcastRegisterMap[flag]!!) {
            SyncManager.getInstance().getLocalBroadcastManager(requireContext()).registerReceiver(homeLocalBroadcastPresenter.localReceiver, IntentFilter(SyncManager.getInstance().getIntentFilterAction(instructionName, deviceId)))
            SyncManager.getInstance().localBroadcastRegisterMap[flag] = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        topDeviceCard.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            view?.post { viewModel.refreshHomePage() }
            checkShowGuardGuidePop()
        }
    }

    private fun checkShowGuardGuidePop() {
        val mainActivity = activity as MainActivity
        val currentDevice = viewModel.appDataSource.user().currentDevice
        val isShow = AppSettings.settingsStorage().getBoolean(viewModel.appDataSource.user().patriarch.user_id + "_checkShowGuardGuidePop", false)
        if (mainActivity.isShowGuardGuidePop && currentDevice != null && currentDevice.hasSetLevel() && !isShow) {
            if (!currentDevice.guard_level.isMildMode()) {
                svHomeScrollContent.scrollTo(0, 0)
                svHomeScrollContent.postDelayed({
                    AppSettings.settingsStorage().putBoolean(viewModel.appDataSource.user().patriarch.user_id + "_checkShowGuardGuidePop", true)
                    mainActivity.isShowGuardGuidePop = false
                    topDeviceCard?.showGuardGuidePop(currentDevice.guard_level)
                }, 100)
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        locationCard.setMapEnable(!hidden)
        if (!hidden) {
            view?.post { viewModel.refreshHomePage() }
            checkShowGuardGuidePop()
        }
    }

    override fun onRefresh() {
        viewModel.refreshHomePage()
    }

    private fun subscribeViewModel() {
        /*用户数据*/
        viewModel.user.observe(this, Observer { user ->
            user ?: return@Observer
            deviceName = user.currentDevice?.device_name ?: ""
            adjustCartsByUserState(user)
            if (user.currentDevice == null) {
                instructionStateCard.gone()
            }
            topLayoutPresenter.showTopChildInfo(user)
            cardDataDispatcher.dispatchUser(user)
            registerReceiver(user)
        })

        /*首页数据*/
        viewModel.homeData
                .observe(this, Observer { resource ->
                    resource ?: return@Observer
                    resource.onSuccess { data ->
                        data?.let(::adjustCarts)
                    }.onError { err ->
                        refreshCompleted()
                        errorHandler.handleError(err)
                    }
                    cardDataDispatcher.dispatchHomeData(resource)
                })

        /*操作状态*/
        viewModel.operationStatus
                .observe(this, Observer {
                    it ?: return@Observer
                    if (it.showLoading) {
                        showLoadingDialog(false)
                    } else {
                        dismissLoadingDialog()
                    }
                    it.message?.let { msg ->
                        showMessage(msg)
                    }
                    if (it.needRefresh) {
                        autoRefresh()
                    }
                    if (it.resetScroll) {
                        svHomeScrollContent.postDelayed({
                            svHomeScrollContent?.scrollToTop()
                        }, 300)
                    }
                })

        /*广告*/
        viewModel.advertising.observe(this, Observer {
            advertisingCard.showData(it)
            advertisingCard.visibleOrGone(!it.isNullOrEmpty())
        })

        /*指令状态*/
        viewModel.instructionsState.observe(this, Observer {
            val allInstructionState = it
            if (allInstructionState == null || allInstructionState.isAllStateSynced()) {
                instructionStateCard.gone()
            } else {
                instructionStateCard.visible()
                instructionStateCard.showState(allInstructionState, cardInteractor.usingUser.currentDevice?.device_name
                        ?: "")
            }
        })

        /*孩子设备权限*/
        viewModel.childDevicePermission.observe(this, Observer {
            it?.onSuccess { privilegeData ->
                refreshCompleted()
                topDeviceCard.mPrivilegeData = privilegeData
                if (privilegeData == null) {
                    if (AppContext.appDataSource().user().logined()) {
                        isVipExpiredDialogShow()
                    }
                } else {
                    topDeviceCard.deviceViewPresenter.showDeviceInfo(topDeviceCard.deviceViewPresenter.showingDevice)
                    if (isVipExpiredDialogShow()) return@onSuccess
                    if (isPermissionLose(privilegeData) || isDeviceNoPermission(privilegeData)) {
                        if (!AppContext.storageManager().stableStorage().getBoolean(buildFlag(isPermissionLose = isPermissionLose(privilegeData)), false)) {
                            showPermissionTipDialog(activity as MainActivity, if (isPermissionLose(privilegeData)) 2 else 0, mainNavigator, privilegeData.privilegeList)
                            AppContext.storageManager().stableStorage().putBoolean(buildFlag(isPermissionLose = isPermissionLose(privilegeData)), true)
                        }
                    }
                }
            }?.onError { err ->
                refreshCompleted()
                errorHandler.handleError(err)
            }
        })
    }

    /**提示会员快到期/过期弹窗*/
    private fun isVipExpiredDialogShow(): Boolean {
        if (isVipExpiredDialogShowed) return false
        val memberExpiredForceChooseTask = MemberExpiredForceChooseTask.getInstance()
        memberExpiredForceChooseTask.run()
        if (memberExpiredForceChooseTask.dialog?.isShowing == true || memberExpiredForceChooseTask.dialogFragment?.isVisible == true) {
            isVipExpiredDialogShowed = true
            return true
        }
        val memberExpiringTipsTask = MemberExpiringTipsTask.getInstance()
        memberExpiringTipsTask.run()
        if (memberExpiringTipsTask.dialog?.isShowing == true) {
            isVipExpiredDialogShowed = true
            return true
        }
        isVipExpiredDialogShowed = false
        return false
    }

    private fun adjustCartsByUserState(user: User) {

        if (!user.logined()) {
            weeklyCard.gone()
            approvalCard.gone()
        }

        if (!user.logined() || user.currentChild == null || user.currentDevice == null) {
            usingStatisticsCard.visible()
            locationCard.visible()
            appRecommendationCard.visible()
            usingTrajectoryCard.visible()
        }
    }

    private fun adjustCarts(data: HomeVO) {
        weeklyCard.visibleOrGone(data.weekly != null)
        approvalCard.visibleOrGone(!data.approvalInfo?.approvalList.isNullOrEmpty())

        if (data.deviceInfo != null) {
            appRecommendationCard.visibleOrGone(!data.peerRecommendation.isNullOrEmpty() && data.deviceInfo.isAndroid())
            locationCard.visibleOrGone(data.deviceInfo.hasGuardItem(GUARD_ITEM_LOCATION))

            if (data.deviceInfo.isIOS()) {
                usingTrajectoryCard.gone()
                usingStatisticsCard.gone()
                llHomeUsingStatisticsTips.gone()
            } else {
                usingTrajectoryCard.visible()
                usingStatisticsCard.visible()
            }

        }

    }

    private inner class CardDataProviderImpl : CardDataProvider {

        override val usingUser: User
            get() = cardDataDispatcher.showingUser

        override val usingData: HomeVO?
            get() = cardDataDispatcher.showingHomeData

        override fun observeHomeData(listener: HomeDataListener) {
            cardDataDispatcher.registerHomePageDataListener(listener)
        }

        override fun observeUser(listener: UserStatusListener) {
            cardDataDispatcher.registerUserListener(listener)
        }

        override fun setTempUsable(tempUsableMinutes: Int) = viewModel.addTemporarilyUsable(tempUsableMinutes)
        override fun closeCloseTempUsable(tempUsableId: String) = viewModel.deleteTemporarilyUsable(tempUsableId)
        override fun forbidNewApp(app: SoftWrapper) = viewModel.forbidNewApp(app)
        override fun switchChild(child: Child, device: Device?) = viewModel.switchChild(child, device)
        override fun installSoft(soft: Soft) = viewModel.installSoft(soft)
        override fun approvalNewNumber(phoneApprovalInfo: PhoneApprovalInfo, allow: Boolean) = viewModel.approvalNewNumber(phoneApprovalInfo, allow)

    }

    override fun onDestroy() {
        homeLocalBroadcastPresenter.onDestroy()
        super.onDestroy()
    }
}