package com.gwchina.parent.main.presentation.home.card

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.RelativeLayout
import com.android.base.imageloader.DisplayConfig
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.imageloader.Source
import com.android.base.kotlin.*
import com.android.base.utils.android.ResourceUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.TopChildInfo
import com.gwchina.parent.main.presentation.home.CardInteractor
import com.gwchina.parent.main.presentation.home.ChangeBgPresenter
import com.gwchina.parent.main.presentation.home.HomeDataListener
import com.gwchina.parent.main.presentation.home.HomeVO
import com.gwchina.parent.main.utils.createChildAgeGradeInfo
import com.gwchina.parent.main.widget.SevereGuardGuidePop
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.data.models.*
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.TextViewUtils
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.formatSecondsToTimeText
import com.gwchina.sdk.base.utils.mapChildAvatarBig
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.gwchina.sdk.base.widget.dialog.showSwitchDeviceDialog
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import com.gwchina.sdk.base.widget.views.setClickFeedback
import kotlinx.android.synthetic.main.home_card_device.view.*
import timber.log.Timber

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-29 19:48
 */
class TopDeviceCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val PHOTO_RED_DOT_FLAG = "photo_red_dot_flag"
        /**
         * 显示远程截屏小红点（家长id相关）
         */
        private val SCREEN_SHOW_RED_DOT_KEY = AppContext.appDataSource().user().patriarch.user_id + "_showScreenDot"
        /**
         * 首次出现该功能入口时（包括新绑定的和原绑定定制机升级版本后的）
         */
        private const val SCREEN_SHOW_LEFT_SCROLL_TIP = "show_left_scroll_tip"
        /**
         * 显示“点击可制定时间计划，xxxx”
         */
        private val SHOW_MAKE_TIME_PLAN_TIP_KEY = AppContext.appDataSource().user().patriarch.user_id + "_showMakeTimeTip"
    }

    private lateinit var interactor: CardInteractor

    internal val deviceViewPresenter: DeviceViewPresenter
    private val topViewPresenter: TopViewPresenter
    private lateinit var changeBgPresenter: ChangeBgPresenter

    var mPrivilegeData: PrivilegeData? = null

    init {
        View.inflate(context, R.layout.home_card_device, this)
        deviceViewPresenter = DeviceViewPresenter()
        topViewPresenter = TopViewPresenter()
        tvHomeTopBg.setOnClickListener {
            if (!AppContext.appDataSource().user().logined() || AppContext.appDataSource().user().childList.isNullOrEmpty()) {
                return@setOnClickListener
            }
            changeBgPresenter.showBottomDialog()
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_CHANGEPHOTO_YES)
        }
    }

    fun setup(cardInteractor: CardInteractor) {
        this.interactor = cardInteractor
        changeBgPresenter = ChangeBgPresenter(cardInteractor.host, tvChangeBg)
        topViewPresenter.setup()
        deviceViewPresenter.setup()
        subscribeData()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        changeBgPresenter.onActivityResult(requestCode, resultCode, data)
    }

    private fun subscribeData() {
        interactor.cardDataProvider.observeUser {
            if (it.currentDevice == null) {
                Timber.d("observeUser showNoDevice")
                deviceViewPresenter.showNoDeviceStatus(it)
            } else {
                Timber.d("observeUser showInitializing")
                deviceViewPresenter.showInitializing(context.getString(R.string.loading_device_status))
            }
            topViewPresenter.showUserInfo(it)
        }

        interactor.cardDataProvider.observeHomeData(object : HomeDataListener {

            override fun onHasChildNoDevice(data: HomeVO?) {
                topViewPresenter.showTopInfo(data?.childInfo)
            }

            override fun onSuccess(data: HomeVO?) {
                Timber.d("observeHomeData onSuccess")
                data?.deviceInfo?.let(deviceViewPresenter::showDeviceInfo)
                topViewPresenter.showTopInfo(data?.childInfo)
                //滑动到最左边
                scrollView.smoothScrollTo(0, 0)
            }

            override fun onError() {
                Timber.d("observeHomeData onError")
                deviceViewPresenter.showInitializing(context.getString(R.string.device_status_loading_failure))
            }

            override fun onLoading() {
                Timber.d("observeHomeData onLoading")
                deviceViewPresenter.showInitializing(context.getString(R.string.loading_device_status))
            }
        })
    }

    private fun showMemberExpiredDialog() {
        OpenMemberDialog.show(context) {
            messageId = R.string.as_member_expired_support_one_tips
        }
    }

    fun showGuardGuidePop(guard_level: Int) {
        SevereGuardGuidePop(interactor.host.activity).show(fblHomeGuardItems, guard_level)
    }

    /**顶部视图------------------------------------------------------------------------------------------------------------------------*/
    inner class TopViewPresenter {

        fun setup() {
            tvHomeLoginTips.onDebouncedClick {
                interactor.navigator.commonJump()
            }
            tvHomeAddDevice.onDebouncedClick {
                StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_ADDDEVICE)
                addDeviceChecked()
            }
            tvHomeGrowDiary.onDebouncedClick {
                StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_DIARY)
                interactor.navigator.openGrowDiaryPage()
            }
            tvHomeGrowParadise.onDebouncedClick {
                StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_TREE)
                interactor.navigator.openGrowthTreePage(interactor.usingUser.currentChild?.child_user_id
                        ?: "")
            }
            llHomeDescriptionFile.onDebouncedClick {
                interactor.navigator.openIosDescriptionFileInstallingGuidePage()
            }

            tvHomeAddDevice.setClickFeedback()
            tvHomeGrowDiary.setClickFeedback()
            tvHomeGrowParadise.setClickFeedback()
        }

        private val switchChildListeners by lazy {
            SwitchChildListeners(
                    onSelectNewChild = {
                        if (isMemberGuardExpired(it.status)) {
                            showMemberExpiredDialog()
                        } else {
                            interactor.cardDataProvider.switchChild(it)
                        }
                    }
            )
        }

        private fun switchChild() {
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_SWITCHCHILD)
            val user = interactor.usingUser
            showSwitchChildPopup(ivHomeChildAvatar, user, switchChildListeners)
        }

        fun showUserInfo(user: User) {
            val currentChild = user.currentChild

            if (currentChild == null) {
                tvHomeLoginTips.visible()
                tvHomeChildName.gone()
                tvHomeChildInfo.gone()
                tvHomeAddDevice.gone()
                llHomeDescriptionFile.gone()
                viewPhotoRedDot.gone()

                if (user.logined()) {
                    tvHomeLoginTips.setText(R.string.binding_devices)
                } else {
                    tvHomeLoginTips.setText(R.string.login_register)
                }

                ivHomeChildAvatar.setImageResource(R.drawable.img_default_avatar)

                ivHomeChildAvatar.onDebouncedClick {
                    interactor.navigator.commonJump()
                }
                showDefaultBg()

            } else {//has child
                tvHomeLoginTips.gone()
                tvHomeChildName.visible()
                tvHomeChildInfo.visible()
                tvHomeAddDevice.visibleOrGone(user.deviceCount() > 0)

                tvHomeChildName.text = currentChild.nick_name.foldText(5)
                tvHomeChildInfo.text = createChildAgeGradeInfo(currentChild)
                ivHomeChildAvatar.setImageResource(mapChildAvatarBig(currentChild.sex))
                ivHomeChildAvatar.onDebouncedClick {
                    interactor.navigator.openChildInfoPage(currentChild.child_user_id, isDeviceNoPermission(mPrivilegeData))
                    if (viewPhotoRedDot.isVisible()) {
                        AppSettings.settingsStorage().putBoolean(PHOTO_RED_DOT_FLAG + user.patriarch.user_id, false)
                        viewPhotoRedDot.gone()
                    }
                }
                setupPhotoRedDot(currentChild, user.patriarch.user_id)

                setupSwitchChildIfNeed(user)
                checkIfIosDeviceDescFileHasBeenDeleted(user)
            }
        }

        /**
         * 初始化头像红点
         */
        private fun setupPhotoRedDot(currentChild: Child, userId: String) {
            val severeDevice = currentChild.device_list?.any {
                //至少有一个重度守护&&会员未过期
                it.guard_level.isSevereMode() && it.status == 1
            }
            val isShowRedDot = AppSettings.settingsStorage().getBoolean(PHOTO_RED_DOT_FLAG + userId, true)
            if (isShowRedDot && severeDevice == true) {
                viewPhotoRedDot.visible()
            } else {
                viewPhotoRedDot.gone()
            }
        }

        private fun checkIfIosDeviceDescFileHasBeenDeleted(user: User) {
            val iosDeviceList = user.childList?.flatMap {
                it.device_list?.filter { device -> device.isIOS() && device.ios_description_flag == FLAG_NEGATIVE }
                        ?: emptyList()
            }

            if (iosDeviceList.isNullOrEmpty()) {
                llHomeDescriptionFile.gone()
            } else {
                llHomeDescriptionFile.visible()
                val deviceListFlag = iosDeviceList.joinToString("、") {
                    if (user.childList?.size ?: 0 > 1) {
                        val childName = user.findChildByDeviceId(it.device_id)?.nick_name
                        context.getString(R.string.x_of_y, childName, it.device_name)
                    } else {
                        it.device_name ?: ""
                    }
                }
                tvHomeDescriptionFileDetail.text = context.getString(R.string.ios_description_file_deleted_tips_mask, deviceListFlag)
                //解决中英文换行排版问题
                tvHomeDescriptionFileDetail.post {
                    tvHomeDescriptionFileDetail.text = TextViewUtils.autoSplitText(tvHomeDescriptionFileDetail)
                }
            }
        }

        private fun setupSwitchChildIfNeed(user: User) {
            //是否支持多孩子多设备
            if (user.childList?.size ?: 0 > 1 && user.vipRule?.home_mine_add_device_enabled == FLAG_POSITIVE_ACTION) {
                tvHomeChildName.rightDrawable(R.drawable.home_icon_solid_down_white)
                tvHomeChildName.setOnClickListener { switchChild() }
                tvHomeChildInfo.setOnClickListener { switchChild() }
            } else {
                tvHomeChildName.rightDrawable(0)
                tvHomeChildName.setOnClickListener(null)
                tvHomeChildInfo.setOnClickListener(null)
            }
        }

        @Suppress("UNUSED_PARAMETER")
        fun showTopInfo(childInfo: TopChildInfo?) {
            val displayConfig = DisplayConfig.create().setLoadingPlaceholder(R.drawable.home_img_top).setErrorPlaceholder(R.drawable.home_img_top)
            ImageLoaderFactory.getImageLoader().display(tvHomeTopBg, childInfo?.home_top_img, displayConfig)
        }

        private fun showDefaultBg() {
            ImageLoaderFactory.getImageLoader().display(tvHomeTopBg, Source.create(R.drawable.home_img_top))
        }

    }

    private fun addDeviceChecked() {
        /*是会员，或者非会员允许添加一个孩子一个设备*/
        val vipRule = interactor.usingUser.vipRule
        if (interactor.usingUser.reachMaximumChildAndDeviceLimit() && vipRule?.home_mine_add_device_enabled == FLAG_POSITIVE_ACTION) {
            interactor.showMessage("绑定设备数量已达上限啦")
        } else if (vipRule?.home_mine_add_device_enabled == FLAG_POSITIVE_ACTION || interactor.usingUser.deviceCount() == 0) {
            interactor.navigator.openAddDevicePage()
        } else {
            OpenMemberDialog.show(interactor.host.requireContext()) {
                message = interactor.host.requireContext().getString(R.string.multi_device_guard_requirement_member_tips_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
                messageDesc = interactor.host.requireContext().getString(R.string.open_member_to_get_more_function_tips_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
                positiveText = interactor.host.requireContext().getString(R.string.open_vip_to_experience_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
            }
        }
    }

    /**设备------------------------------------------------------------------------------------------------------------------------*/
    inner class DeviceViewPresenter {

        internal lateinit var showingDevice: Device

        private val guardEntranceViews = arrayOf(tvHomeTimeGuard, tvHomeAppGuard, tvHomeNetGuard, tvHomeFamilyNumber)

        private val entranceViewsDrawable = arrayOf(
                R.drawable.home_icon_time_guard, R.drawable.home_icon_app_guard,
                R.drawable.home_icon_internet, R.drawable.home_icon_dear)

        private val entranceViewsDrawableGray = arrayOf(
                R.drawable.home_icon_time_guard_gray, R.drawable.home_icon_app_guard_gray,
                R.drawable.home_icon_internet_guard_gray, R.drawable.home_icon_dear_gray)

        private val clickSwitchDeviceListener = OnClickListener {
            val child = interactor.usingUser.currentChild ?: return@OnClickListener
            val device = interactor.usingUser.currentDevice ?: return@OnClickListener

            showSwitchDeviceDialog(context, child, device, expiredEnable = true, onSelectedDevice = {

                if (isMemberGuardExpired(it.status)) {
                    showMemberExpiredDialog()
                } else if (device.device_id != it.device_id) {
                    Timber.e("switch device")
                    interactor.cardDataProvider.switchChild(child, it)
                    StatisticalManager.onEvent(UMEvent.ClickEvent.HOMTPAGE_BTN_SWITCHDEVICE)
                }
            })
        }


        private val clickLoginOrBindingListener = fun(_: View) {
            val user = interactor.usingUser
            val child = user.currentChild
            if (!user.logined()) {
                interactor.navigator.openLoginPage()
            } else {
                interactor.navigator.openAddDevicePage(child?.child_user_id)
            }
        }

        private val loginTips by lazy {
            SpanUtils()
                    .appendImage(R.drawable.icon_member_flag, SpanUtils.ALIGN_CENTER)
                    .append("  ")
                    .append(ResourceUtils.getString(R.string.register_get_member_tips))
                    .setFontSize(16, true)
                    .setForegroundColor(colorFromId(R.color.gray_level1))
                    .appendLine()
                    .appendLine()
                    .setFontSize(6, true)
                    .append(ResourceUtils.getString(R.string.home_login_tips))
                    .create()
        }

        fun setup() {
            //调整尺寸
            showScreenShot(true)

            //时间守护
            tvHomeTimeGuard.onDebouncedClick {
                StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_TIME)
                if (interactor.usingData?.deviceInfo?.isTemporarilyUsable() == true) {
                    val strResId = if (isFlagPositive(interactor.usingData?.deviceInfo?.temp_usable_time?.mode)) R.string.temporarily_lock_cannot_opt_tips else R.string.temporarily_usable_cannot_opt_tips
                    interactor.showMessage(context.getString(strResId))
                } else {
                    interactor.navigator.openTimeGuardPage(interactor.usingData?.deviceInfo, mPrivilegeData)
                    AppSettings.settingsStorage().putBoolean(SHOW_MAKE_TIME_PLAN_TIP_KEY, true)
                }
            }
            //应用守护
            tvHomeAppGuard.onDebouncedClick {
                StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_SOFT)
                interactor.navigator.openAppGuardPage(interactor.usingData?.deviceInfo, mPrivilegeData)
            }
            //网址守护
            tvHomeNetGuard.onDebouncedClick {
                StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_GUARDWEB)
                interactor.navigator.openNetGuardPage(interactor.usingData?.deviceInfo)
            }
            //亲情号码
            tvHomeFamilyNumber.onDebouncedClick {
                StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_FAMILYNUMBER)
                interactor.navigator.openFamilyNumberPage(interactor.usingData?.deviceInfo)
            }
            //远程截屏
            tvHomeScreenShot.onDebouncedClick {
                interactor.navigator.openScreenshotPage()
                AppSettings.settingsStorage().putBoolean(SCREEN_SHOW_RED_DOT_KEY, false)
            }
            setHorizontalScrollViewListener()
        }

        fun showNoDeviceStatus(user: User) {
            //入口图标
            val entranceTextColor = colorFromId(R.color.gray_level3)
            guardEntranceViews.forEachIndexed { index, view ->
                view.layoutParams.width = (ScreenUtils.getScreenWidth() / 4)
                view.topDrawable(entranceViewsDrawableGray[index])
                view.setTextColor(entranceTextColor)
            }
            //展示入口
            fblHomeGuardItems.visible()
            tvHomeTimeGuard.visible()
            tvHomeFamilyNumber.visible()
            //隐藏功能
            rlHomeDeviceInfo.gone()
            tvHomeDeviceStatus.gone()
            //登录绑定提示
            tvHomeLoginStatus.visible()
            tvHomeLoginStatus.text = if (!user.logined()) {
                loginTips
            } else {
                ResourceUtils.getText(R.string.home_binding_tips)
            }
            llHomeDeviceInfo.onDebouncedClick(clickLoginOrBindingListener)
            //隐藏远程截屏
            tvHomeScreenShot.gone()
            rlScroll.gone()
            tvScrollTip.gone()
        }

        fun showInitializing(statusMsg: String) {
            //入口图标
            val entranceTextColor = colorFromId(R.color.gray_level2)
            guardEntranceViews.forEachIndexed { index, view ->
                view.topDrawable(entranceViewsDrawable[index])
                view.setTextColor(entranceTextColor)
            }
            //隐藏登录提示
            tvHomeLoginStatus.gone()
            //去掉点击事件
            llHomeDeviceInfo.setOnClickListener(null)
            //状态提示
            tvHomeDeviceStatus.visible()
            tvHomeDeviceStatus.clearComponentDrawable()
            tvHomeDeviceStatus.gravity = Gravity.CENTER
            tvHomeDeviceStatus.text = statusMsg
            //设备名称也可以先展示出来
            val currentDevice = interactor.usingUser.currentDevice
            if (currentDevice != null) {
                rlHomeDeviceInfo.visible()
                hideTempSwtich()
                tvHomeGuardingStatus.gone()
                tvHomeDeviceName.text = currentDevice.device_name
            } else {
                rlHomeDeviceInfo.gone()
            }
            //如果有多个设备支持切换
            setupSwitchDeviceListenerIfNeed()
        }

        fun showDeviceInfo(deviceInfo: Device) {
            showingDevice = deviceInfo
            adjustLayoutByGuardLevel()
        }

        private fun adjustLayoutByGuardLevel() {
            resetLayout()
            setupSwitchDeviceListenerIfNeed()
            showDeviceCommonInfo()
            when {
                showingDevice.guard_level.isMildMode() -> adjustToMildModeLayout()
                showingDevice.guard_level.isModerateMode() -> adjustToModerateModeLayout()
                showingDevice.guard_level.isSevereMode() -> adjustToSevereModeLayout()
                else -> showNoDeviceLevel()
            }
        }

        private fun showDeviceCommonInfo() {
            tvHomeDeviceName.text = interactor.usingUser.currentDevice?.device_name
        }

        private fun setupSwitchDeviceListenerIfNeed() {
            val user = interactor.usingUser
            val currentChild = user.currentChild
            val currentDevice = user.currentDevice
            //并且是多孩子多设备
            if (currentDevice != null && currentChild?.moreThanOneDevice() == true && user.vipRule?.home_mine_add_device_enabled == FLAG_POSITIVE_ACTION) {

                ivHomeSwitchDevice.visible()
                llDeviceName.setOnClickListener(clickSwitchDeviceListener)

                if (currentDevice.index > 0) {
                    tvHomeDeviceIndex.visible()
                    tvHomeDeviceIndex.text = currentDevice.index.toString()
                } else {
                    tvHomeDeviceIndex.gone()
                }

            } else {
                ivHomeSwitchDevice.gone()
                tvHomeDeviceIndex.gone()
                llDeviceName.setOnClickListener(null)
            }
        }

        private fun resetLayout() {
            tvHomeDeviceStatus.gravity = Gravity.START
            tvHomeDeviceStatus.onDebouncedClick {
                if (context.getString(R.string.severe_mode_set_time_guard_tips) == tvHomeDeviceStatus.text) {
                    interactor.navigator.openTimeGuardPage(interactor.usingData?.deviceInfo, mPrivilegeData)
                    AppSettings.settingsStorage().putBoolean(SHOW_MAKE_TIME_PLAN_TIP_KEY, true)
                }
            }
            llHomeDeviceInfo.setOnClickListener(null)
        }

        private fun adjustToMildModeLayout() {
            //模块入口
            fblHomeGuardItems.gone()
            tvScrollTip.gone()
            rlScroll.gone()
            //状态
            showDeviceStatusInMildMode()
        }

        /**
         * 轻(中)度模式两个状态：
         *
         *  - 正常管控中
         *  - 设备离线
         */
        private fun showDeviceStatusInMildMode() {
            //临时可用
            hideTempSwtich()
            //离线或者守护中
            when {
                !interactor.usingUser.isMember -> showMemberGuardExpired()
                //权限未完成设置
                mPrivilegeData?.let { isDeviceNoPermission(it) } == true -> {
                    showTempUsable()
                    tvHomeGuardingStatus.gone()
                    tvHomeDeviceStatus.visible()
                    tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_on_mine)
                    val content = SpanUtils()
                            .append(context.getString(R.string.home_no_permission_state1))
                            .append(context.getString(R.string.home_no_permission_state2))
                            .setForegroundColor(ContextCompat.getColor(context, R.color.blue_level2))
                            .create()
                    tvHomeDeviceStatus.text = content
                    tvHomeDeviceStatus.onDebouncedClick {
                        if (tvHomeDeviceStatus.text == content) {
                            interactor.navigator.openChildDevicePermissionInfo()
                        }
                    }
                }
                //权限丢失
                mPrivilegeData?.let { isPermissionLose(it) } == true -> {
                    showTempUsable()
                    tvHomeGuardingStatus.gone()
                    tvHomeDeviceStatus.visible()
                    tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_on_mine)
                    val content = SpanUtils()
                            .append(context.getString(R.string.home_permission_lose_state1))
                            .append(context.getString(R.string.home_permission_lose_state2))
                            .setForegroundColor(ContextCompat.getColor(context, R.color.blue_level2))
                            .create()
                    tvHomeDeviceStatus.text = content
                    tvHomeDeviceStatus.onDebouncedClick {
                        if (tvHomeDeviceStatus.text == content) {
                            interactor.navigator.openPermissionDetailPage(mPrivilegeData!!.privilegeList!!)
                        }
                    }
                }
                //离线
                showingDevice.isOffline() -> {
                    tvHomeGuardingStatus.gone()
                    tvHomeDeviceStatus.visible()
                    tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_forbidden)
                    val content = SpanUtils()
                            .append(context.getString(R.string.check_device_of_child_status_tips))
                            .append(context.getString(R.string.check_device_of_child_status_tips2))
                            .setForegroundColor(ContextCompat.getColor(context, R.color.blue_level2))
                            .create()
                    tvHomeDeviceStatus.text = content
                    tvHomeDeviceStatus.onDebouncedClick {
                        if (tvHomeDeviceStatus.text == content) {
                            interactor.navigator.openChildDeviceOfflineInfo()
                        }
                    }
                }
                else -> {
                    tvHomeGuardingStatus.visible()
                    tvHomeDeviceStatus.gone()
                }
            }
        }

        /**中度守护*/
        private fun adjustToModerateModeLayout() {
            //模块入口
            fblHomeGuardItems.visible()
            //中度没有时间守护
            tvHomeTimeGuard.gone()
            //孩子端为ios的设备没有亲情号码
            tvHomeFamilyNumber.visibleOrGone(showingDevice.isAndroid())
            //状态
            showDeviceStatusInMildMode()
            showScreenShot(false)
        }

        /**重度*/
        private fun adjustToSevereModeLayout() {
            fblHomeGuardItems.visible()
            tvHomeTimeGuard.visible()
            //孩子端为ios的设备没有亲情号码
            tvHomeFamilyNumber.visibleOrGone(showingDevice.isAndroid())
            tvHomeGuardingStatus.gone()
            showDeviceStatusInSevereMode()
            showScreenShot(false)
        }

        /**
         * - 会员过期
         * - 设备离线
         * - 是否设置了时间守护
         * - 临时可用
         * - 可用时段（自由可用）
         * - 禁用时段
         */
        private fun showDeviceStatusInSevereMode() {
            when {
                //会员过期
                !interactor.usingUser.isMember -> {
                    showMemberGuardExpired()
                }
                mPrivilegeData?.let { isDeviceNoPermission(it) } == true -> {
                    showTempUsable()
                    tvHomeGuardingStatus.gone()
                    tvHomeDeviceStatus.visible()
                    tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_on_mine)
                    val content = SpanUtils()
                            .append(context.getString(R.string.home_no_permission_state1))
                            .append(context.getString(R.string.home_no_permission_state2))
                            .setForegroundColor(ContextCompat.getColor(context, R.color.blue_level2))
                            .create()
                    tvHomeDeviceStatus.text = content
                    tvHomeDeviceStatus.onDebouncedClick {
                        if (tvHomeDeviceStatus.text == content) {
                            interactor.navigator.openChildDevicePermissionInfo()
                        }
                    }
                }
                //权限丢失
                mPrivilegeData?.let { isPermissionLose(it) } == true -> {
                    showTempUsable()
                    tvHomeGuardingStatus.gone()
                    tvHomeDeviceStatus.visible()
                    tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_on_mine)
                    val content = SpanUtils()
                            .append(context.getString(R.string.home_permission_lose_state1))
                            .append(context.getString(R.string.home_permission_lose_state2))
                            .setForegroundColor(ContextCompat.getColor(context, R.color.blue_level2))
                            .create()
                    tvHomeDeviceStatus.text = content
                    tvHomeDeviceStatus.onDebouncedClick {
                        if (tvHomeDeviceStatus.text == content) {
                            interactor.navigator.openPermissionDetailPage(mPrivilegeData!!.privilegeList!!)
                        }
                    }
                }
                //设备离线
                showingDevice.isOffline() -> {
                    hideTempSwtich()
                    tvHomeDeviceStatus.visible()
                    tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_forbidden)
                    val content = SpanUtils()
                            .append(context.getString(R.string.check_device_of_child_status_tips))
                            .append(context.getString(R.string.check_device_of_child_status_tips2))
                            .setForegroundColor(ContextCompat.getColor(context, R.color.blue_level2))
                            .create()
                    tvHomeDeviceStatus.text = content
                    tvHomeDeviceStatus.onDebouncedClick {
                        if (tvHomeDeviceStatus.text == content) {
                            interactor.navigator.openChildDeviceOfflineInfo()
                        }
                    }
                }

                //在临时可用/锁屏
                showingDevice.isTemporarilyUsable() -> {
                    showTempUsable()
                }

                //没有设置时间守护
                isNo(showingDevice.setting_time_flag) -> {
                    hideTempSwtich()
                    val isShowMakeTimeTips = AppSettings.settingsStorage().getBoolean(SHOW_MAKE_TIME_PLAN_TIP_KEY, false)
                    tvHomeDeviceStatus.visible()
                    if (!isShowMakeTimeTips) {
                        tvHomeDeviceStatus.clearComponentDrawable()
                        tvHomeDeviceStatus.gravity = Gravity.CENTER
                        tvHomeDeviceStatus.text = context.getString(R.string.severe_mode_set_time_guard_tips)
                    } else {
                        tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_unlock)
                        tvHomeDeviceStatus.text = context.getString(R.string.not_look_screen)
                        tvHomeDeviceStatus.gravity = Gravity.START
                    }
                }

                /*在可用时段*/
                showingDevice.isInUsablePeriod() -> {
                    //展示下次锁屏时间
                    tvHomeDeviceStatus.visible()
                    tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_unlock)
                    tvHomeDeviceStatus.text = getNextLockTimeDesc(showingDevice)
                    //隐藏临时可用
                    hideTempSwtich()
                }

                //不可用
                showingDevice.isDisable() -> showDisable()
                else -> showDisable()
            }
        }

        private fun showTempUsable() {
            showingDevice.temp_usable_time.ifNonNull {
                //显示临时可用剩余时间
                tvHomeDeviceStatus.visible()
                val stringId1 = if (isFlagPositive(mode)) R.string.remaining_temporarily_lock_mask else R.string.remaining_temporarily_usable_mask
                val stringId2 = if (isFlagPositive(mode)) R.string.confirm_to_delete_look_screen else R.string.confirm_to_delete_temporarily_usable
                val stringId3 = if (isFlagPositive(mode)) R.string.temporary_look_screen else R.string.temporarily_usable

                tvHomeDeviceStatus.text = context.getString(stringId1, formatSecondsToTimeText(showingDevice.getTemporarilyRemainingSeconds(), true))
                tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_limited)
                //展示临时可用
                showTempSwitch(stringId3)
                switchHomeTempUsable.setCheckedImmediatelyNoEvent(true)
                switchHomeTempUsable.setOnCheckedChangeListener { _, checked ->
                    switchHomeTempUsable.setCheckedImmediatelyNoEvent(true)
                    if (!checked) {
                        askCloseTempUsable(rule_id, stringId2)
                    }
                }
            }
        }

        private fun showDisable() {
            //展示何时解锁
            tvHomeDeviceStatus.visible()
            tvHomeDeviceStatus.leftDrawable(R.drawable.home_icon_lock)
            tvHomeDeviceStatus.text = getNextUnlockTime(showingDevice)
            hideTempSwtich()
        }

        private fun showTempSwitch(stringResId: Int) {
            switchHomeTempUsable.visible()
            tvHomeTempAvailableTitle.visible()
            tvHomeTempAvailableTitle.setText(stringResId)
        }

        private fun hideTempSwtich() {
            switchHomeTempUsable.gone()
            tvHomeTempAvailableTitle.gone()
        }

        private fun askCloseTempUsable(tempUsableId: String?, resId: Int) {
            if (tempUsableId.isNullOrEmpty()) {
                return
            }
            showConfirmDialog(context) {
                message = context.getString(resId)
                positiveListener = {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_CLOSE)
                    interactor.cardDataProvider.closeCloseTempUsable(tempUsableId)
                }
            }
        }

        private fun showMemberGuardExpired() {
            hideTempSwtich()
            tvHomeDeviceStatus.visible()
            tvHomeDeviceStatus.clearComponentDrawable()
            tvHomeDeviceStatus.gravity = Gravity.CENTER
            val expiredContent = SpanUtils()
                    .append("会员已到期，")
                    .append("开通会员")
                    .setForegroundColor(ContextCompat.getColor(context, R.color.blue_level2))
                    .append("开启全面守护")
                    .create()
            tvHomeDeviceStatus.text = expiredContent
            tvHomeDeviceStatus.onDebouncedClick {
                if (tvHomeDeviceStatus.text == expiredContent) {
                    interactor.navigator.openPurchasePage()
                }
            }
        }

        private fun showNoDeviceLevel() {
            fblHomeGuardItems.gone()
            tvHomeGuardingStatus.gone()
            hideTempSwtich()
            tvHomeDeviceStatus.gravity = Gravity.CENTER
            tvHomeDeviceStatus.clearComponentDrawable()
            tvHomeDeviceStatus.text = "没有守护等级信息"
            tvHomeDeviceStatus.visible()
        }

        private fun showScreenShot(isInit: Boolean) {
            if (!isInit) {
                if (showingDevice.custom_device_flag != YES_FLAG) {
                    tvHomeScreenShot.gone()
                } else {
                    tvHomeScreenShot.visible()
                    val showRedDot = AppSettings.settingsStorage().getBoolean(SCREEN_SHOW_RED_DOT_KEY, true)
                    if (showRedDot) {
                        tvHomeScreenShot.text = SpanUtils().appendImage(R.drawable.shape_red_dot_screen_notification, SpanUtils.ALIGN_CENTER).append(" ").append(context.getString(R.string.home_screen_shot)).create()
                    } else {
                        tvHomeScreenShot.text = context.getString(R.string.home_screen_shot)
                    }
                }
            }
            val visibleCount = (0 until fblHomeGuardItems.childCount).count {
                fblHomeGuardItems.getChildAt(it).isVisible()
            }
            var viewWidth = ScreenUtils.getScreenWidth().toFloat()
            if (visibleCount == 5) {
                viewWidth = viewWidth * 5 / 4
                if (AppSettings.settingsStorage().getBoolean(SCREEN_SHOW_LEFT_SCROLL_TIP, true)) {
                    tvScrollTip.visible()
                } else {
                    rlScroll.visible()
                }
            } else {
                tvScrollTip.gone()
                rlScroll.gone()
            }
            (0 until fblHomeGuardItems.childCount).forEach {
                fblHomeGuardItems.getChildAt(it).apply {
                    if (isVisible()) {
                        layoutParams.width = (viewWidth / visibleCount).toInt()
                    }
                }
            }
        }

        //设置滑动指示器的位移
        private fun setHorizontalScrollViewListener() {
            scrollView.onScrollListener = { l, _, _, _ ->
                ivScreenShot.post {
                    val marginLeft = ivScreenShot.width * (l.toFloat() / (ScreenUtils.getScreenWidth() / 4.0f))
                    ivScreenShot.translationX = marginLeft
                    //滑动超过一半显示滑动指示条 隐藏提示
                    if (AppSettings.settingsStorage().getBoolean(SCREEN_SHOW_LEFT_SCROLL_TIP, true) && l >= ScreenUtils.getScreenWidth() / 8.0f) {
                        rlScroll.visible()
                        tvScrollTip.gone()
                        AppSettings.settingsStorage().putBoolean(SCREEN_SHOW_LEFT_SCROLL_TIP, false)
                    }
                }
            }
        }
    }
}