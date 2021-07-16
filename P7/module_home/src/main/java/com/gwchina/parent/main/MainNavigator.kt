package com.gwchina.parent.main

import android.os.Bundle
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.findFragmentByTag
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.toArrayList
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.ChildLocation
import com.gwchina.parent.main.presentation.approval.AppApprovalFragment
import com.gwchina.parent.main.presentation.approval.PhoneApprovalFragment
import com.gwchina.parent.main.presentation.device.BoundDeviceFragment
import com.gwchina.parent.main.presentation.device.DeviceManagingFragment
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO.SoftWrapper
import com.gwchina.parent.main.presentation.home.HomeUsingRecordFragment
import com.gwchina.parent.main.presentation.home.PermissionDetailFragment
import com.gwchina.parent.main.presentation.home.ReturningSelectedChildInfoJsCallInterceptor
import com.gwchina.parent.main.presentation.home.card.isDeviceNoPermission
import com.gwchina.parent.main.presentation.home.card.isPermissionLose
import com.gwchina.parent.main.presentation.mine.ShareJsCallInterceptor
import com.gwchina.parent.main.presentation.weekly.WeeklyListFragment
import com.gwchina.parent.main.utils.showPermissionTipDialog
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.data.DataContext
import com.gwchina.sdk.base.data.api.APP_RULE_TYPE_PENDING_APPROVAL
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.api.isFlagPositive
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.*
import com.gwchina.sdk.base.router.AppInfo
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.router.RouterPath.FamilyPhone.PHONE_PLAN_HAS_BE_SET
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.TOTAL_SECONDS_OF_ONE_DAY
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import javax.inject.Inject

/**
 * page navigator of main module.
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-01-19 17:49
 */
@ActivityScope
class MainNavigator @Inject constructor(
        private val mainActivity: MainActivity,
        private val appDataSource: AppDataSource,
        private val appRouter: AppRouter
) {

    fun openMessageCenterPage() {
        if (requireLogined()) {
            appRouter.build(RouterPath.Message.PATH).navigation()
        }
    }

    fun openHelpingCenterPage() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.HELP_FEEDBACK)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    fun openAboutUsPage() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.ABOUT_US)
                .withBoolean(RouterPath.Browser.CACHE_ENABLE, true)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    fun openPatriarchInfoPage() {
        if (requireLogined()) {
            StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_PERSONAL)
            appRouter.build(RouterPath.Profile.PATH).withInt(RouterPath.PAGE_KEY, RouterPath.Profile.PAGE_PATRIARCH_INFO).navigation()
        }
    }

    fun openLoginPage() {
        appRouter.build(RouterPath.Account.PATH).navigation(mainActivity, RouterPath.Account.REQUEST_CODE)
    }

    /** if [childUserId] is not null or empty, than add device for the corresponding child.*/
    fun openAddDevicePage(childUserId: String? = null, selectChild: Boolean = false) {
        if (requireLogined()) {
            appRouter.build(RouterPath.Binding.PATH)
                    .apply {
                        withBoolean(RouterPath.Binding.SELECT_CHILD_KEY, selectChild)
                        if (!childUserId.isNullOrEmpty()) {
                            withString(RouterPath.Binding.CHILD_USER_ID_KEY, childUserId)
                        }
                    }
                    .navigation(mainActivity, RouterPath.Binding.REQUEST_CODE)
        }
    }

    fun openChildInfoPage(childUserId: String, needShowPermissionDialog: Boolean = false) {
        StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_CHILDREN)
        appRouter.build(RouterPath.Profile.PATH)
                .withInt(RouterPath.PAGE_KEY, RouterPath.Profile.PAGE_CHILD_INFO)
                .withString(RouterPath.Profile.CHILD_USER_ID_KEY, childUserId)
                .withBoolean(RouterPath.Profile.CHILD_NEED_SHOW_PERMISSION_KEY, needShowPermissionDialog)
                .navigation()
    }

    fun openGrowDiaryPage() {
        appRouter.build(RouterPath.Diary.PATH).withInt(RouterPath.PAGE_KEY, RouterPath.Diary.PAGE_LIST).navigation()
    }

    fun openAppGuardPage(device: Device? = null, privilegeData: PrivilegeData?) {
        openingGuardFunctionChecked(
                onMemberRequired = onMemberRequired(device.planedAppPlan(),
                        mainActivity.getString(R.string.apps_guard_need_to_open_member_mask, appDataSource.user().vipRule?.home_app_enter_minimum_level),
                        mainActivity.getString(R.string.open_member_to_get_more_function_tips_mask, appDataSource.user().vipRule?.home_app_enter_minimum_level),
                        mainActivity.getString(R.string.open_vip_to_experience_mask, appDataSource.user().vipRule?.home_app_enter_minimum_level)
                ),
                onPermissionRequired = onPermissionRequired(RouterPath.AppsGuard.PATH, privilegeData),
                onPast = { appRouter.build(RouterPath.AppsGuard.PATH).navigation() },
                isCanEnter = appDataSource.user().vipRule?.home_app_enter == FLAG_POSITIVE_ACTION
        )
    }

    fun openTimeGuardPage(device: Device? = null, privilegeData: PrivilegeData?) {

        //是会员，设置了时间守护，并且是ios设备，并且没有打开监督模式，则进入监督模式引导页
        if (appDataSource.user().vipRule?.home_time_enter == FLAG_POSITIVE_ACTION && device.timePlanHasBeSet() && device.isIOS() && !isFlagPositive(device?.ios_supervised_flag)) {
            appRouter.build(RouterPath.IOSSuperviseMode.PATH).navigation()
            return
        }

        openingGuardFunctionChecked(
                onMemberRequired = onMemberRequired(device.timePlanHasBeSet(),
                        mainActivity.getString(R.string.times_guard_need_to_open_member_mask, appDataSource.user().vipRule?.home_time_enter_minimum_level),
                        mainActivity.getString(R.string.open_member_to_get_more_function_tips_mask, appDataSource.user().vipRule?.home_time_enter_minimum_level),
                        mainActivity.getString(R.string.open_vip_to_experience_mask, appDataSource.user().vipRule?.home_time_enter_minimum_level)
                ),
                onPermissionRequired = onPermissionRequired(RouterPath.TimesGuard.PATH, privilegeData),
                onPast = { appRouter.build(RouterPath.TimesGuard.PATH).navigation() },
                isCanEnter = appDataSource.user().vipRule?.home_time_enter == FLAG_POSITIVE_ACTION)
    }

    private fun onMemberRequired(isNotNull: Boolean, msg: String,
                                 msgDesc: String,
                                 btnText: String): (() -> Unit)? {
        return if (isNotNull) {
            {
                OpenMemberDialog.show(mainActivity) {
                    message = msg
                    messageDesc = msgDesc
                    positiveText = btnText
                }
            }
        } else {
            null
        }
    }

    private fun onPermissionRequired(routerPath: String, privilegeData: PrivilegeData?): (() -> Unit)? {
        return if (isPermissionLose(privilegeData) || isDeviceNoPermission(privilegeData)) {
            {
                showPermissionTipDialog(mainActivity, if (isPermissionLose(privilegeData)) 1 else 0, this, privilegeData?.privilegeList) {
                    appRouter.build(routerPath).navigation()
                }
            }
        } else {
            null
        }
    }

    fun openNetGuardPage(device: Device?) {

        //是会员，设置了上网守护，并且是ios设备，并且没有打开监督模式，则进入监督模式引导页
        if (appDataSource.user().vipRule?.home_net_enter == FLAG_POSITIVE_ACTION && device.netPlanHasBeSet() && device.isIOS() && !isFlagPositive(device?.ios_supervised_flag)) {
            appRouter.build(RouterPath.IOSSuperviseMode.PATH).navigation()
            return
        }

        openingGuardFunctionChecked(
                onMemberRequired = onMemberRequired(device.netPlanHasBeSet(),
                        mainActivity.getString(R.string.net_guard_need_to_open_member_mask, appDataSource.user().vipRule?.home_net_enter_minimum_level),
                        mainActivity.getString(R.string.open_member_to_get_more_function_tips_mask, appDataSource.user().vipRule?.home_net_enter_minimum_level),
                        mainActivity.getString(R.string.open_vip_to_experience_mask, appDataSource.user().vipRule?.home_net_enter_minimum_level)
                ),
                onPast = { appRouter.build(RouterPath.NetGuard.PATH).navigation() },
                isCanEnter = appDataSource.user().vipRule?.home_net_enter == FLAG_POSITIVE_ACTION)
    }

    fun openFamilyNumberPage(device: Device?) {
        openingGuardFunctionChecked(onMemberRequired = onMemberRequired(device.phonePlanHasBeSet(),
                mainActivity.getString(R.string.family_number_need_to_open_member_mask, appDataSource.user().vipRule?.home_family_enter_minimum_level),
                mainActivity.getString(R.string.open_member_to_get_more_function_tips_mask, appDataSource.user().vipRule?.home_family_enter_minimum_level),
                mainActivity.getString(R.string.open_vip_to_experience_mask, appDataSource.user().vipRule?.home_family_enter_minimum_level)

        ),
                onPast = { appRouter.build(RouterPath.FamilyPhone.PATH).withBoolean(PHONE_PLAN_HAS_BE_SET, device.phonePlanHasBeSet()).navigation() },
                isCanEnter = appDataSource.user().vipRule?.home_family_enter == FLAG_POSITIVE_ACTION
        )
    }

    /**
     * 截屏页面
     */
    fun openScreenshotPage() {

        if (appDataSource.user().vipRule?.home_screenshot_enter == FLAG_POSITIVE_ACTION) {
            appRouter.build(RouterPath.Screenshot.PATH).navigation()
        } else {
            OpenMemberDialog.show(mainActivity) {
                message = mainActivity.getString(R.string.screenshot_vip_mask, appDataSource.user().vipRule?.home_screenshot_enter_minimum_level)
                messageDesc = mainActivity.getString(R.string.open_member_to_get_more_function_tips_mask, appDataSource.user().vipRule?.home_screenshot_enter_minimum_level)
                positiveText = mainActivity.getString(R.string.open_vip_to_experience_mask, appDataSource.user().vipRule?.home_screenshot_enter_minimum_level)
            }
        }
    }

    private fun openingGuardFunctionChecked(onMemberRequired: (() -> Unit)? = null, onPermissionRequired: (() -> Unit)? = null,
                                            onPast: () -> Unit, isCanEnter: Boolean) {
        if (!requireLogined()) {
            return
        }
        appDataSource.user().run {
            if (currentDevice == null) {
                appRouter.build(RouterPath.Binding.PATH).navigation(mainActivity, RouterPath.Binding.REQUEST_CODE)
                return
            }
            if (!isCanEnter && onMemberRequired != null) {
                onMemberRequired()
            } else if (isCanEnter && onPermissionRequired != null) {
                onPermissionRequired()
            } else {
                onPast()
            }
        }
    }

    fun openMemberPage() {
        if (requireLogined()) {
            appRouter.build(RouterPath.MemberCenter.PATH).navigation()
        }
    }

    /**
     * 会员购买页面
     */
    fun openPurchasePage() {
        if (requireLogined()) {
            appRouter.build(RouterPath.MemberCenter.PATH)
                    .withInt(RouterPath.PAGE_KEY, RouterPath.MemberCenter.CENTER)
                    .navigation()
        }
    }

    fun openGuardLevelForDevice(deviceId: String) {
        appRouter.build(RouterPath.GuardLevel.PATH)
                .withString(RouterPath.GuardLevel.DEVICE_ID_KEY, deviceId)
                .navigation(mainActivity, RouterPath.GuardLevel.REQUEST_CODE)
    }

    fun commonJump() {
        val user = mainActivity.appDataSource.user()
        if (!user.logined()) {
            openLoginPage()
        } else if (user.currentDevice == null) {
            appRouter.build(RouterPath.Binding.PATH).navigation(mainActivity, RouterPath.Binding.REQUEST_CODE)
        }
    }

    private fun requireLogined(): Boolean {
        val user = mainActivity.appDataSource.user()
        //是否登录
        if (!user.logined()) {
            openLoginPage()
            return false
        }
        return true
    }

    fun openAppApprovalPage(appWrapper: SoftWrapper) {
        val app = appWrapper.soft

        val appInfo = AppInfo(
                rule_id = app.rule_id,
                rule_type = APP_RULE_TYPE_PENDING_APPROVAL,
                app_icon = app.soft_icon,
                app_name = app.soft_name,
                type_name = app.type_name,
                /*待审批的应用默认可用时长为一整天*/
                used_time_per_day = TOTAL_SECONDS_OF_ONE_DAY)

        appRouter.build(RouterPath.AppsGuard.PATH)
                .withParcelable(RouterPath.AppsGuard.APP_INFO_KEY, appInfo)
                .withString(RouterPath.AppsGuard.CHILD_USER_ID_KEY, appWrapper.childUserId)
                .withString(RouterPath.AppsGuard.DEVICE_ID_KEY, appWrapper.childDeviceId)
                .navigation()
    }

    fun openChildLocationPageForDefaultChild(childLocation: ChildLocation?) {
        if (!requireLogined()) {
            return
        }
        if (appDataSource.user().currentDevice == null) {
            appRouter.build(RouterPath.Binding.PATH).navigation(mainActivity, RouterPath.Binding.REQUEST_CODE)
            return
        }
        appRouter.build(RouterPath.Main.LOCATION).withParcelable(RouterPath.Main.LOCATION_KEY, childLocation).navigation()
    }

    fun openAppApprovalListPage() {
        if (mainActivity.supportFragmentManager.findFragmentByTag(AppApprovalFragment::class) != null) {
            return
        }
        mainActivity.inFragmentTransaction {
            addWithStack(fragment = AppApprovalFragment())
        }
    }

    fun openPhoneApprovalListPage() {
        if (mainActivity.supportFragmentManager.findFragmentByTag(PhoneApprovalFragment::class) != null) {
            return
        }
        mainActivity.inFragmentTransaction {
            addWithStack(fragment = PhoneApprovalFragment())
        }
    }

    fun openBoundDevicePageForChild(childUserId: String) {
        if (mainActivity.supportFragmentManager.findFragmentByTag(BoundDeviceFragment::class) != null) {
            return
        }
        mainActivity.inFragmentTransaction {
            addWithStack(fragment = BoundDeviceFragment.newInstance(childUserId))
        }
    }

    fun openDeviceManagingPage() {
        if (mainActivity.supportFragmentManager.findFragmentByTag(DeviceManagingFragment::class) != null) {
            return
        }
        mainActivity.inFragmentTransaction {
            addWithStack(fragment = DeviceManagingFragment())
        }
    }

    fun openAppRecommendationPage(recGroupId: String) {
        appRouter.build(RouterPath.Recommend.PATH)
                .withString(RouterPath.Recommend.RECOMMEND_ID_KEY, recGroupId)
                .navigation()
    }

    fun openGuardStatisticsPage() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.GUARD_STATISTICS)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    fun openGrowthTreePage(childUserId: String) {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.GROWING_TREE)
                .withBundle(RouterPath.Browser.ARGUMENTS_KEY, Bundle().apply { putString(CHILD_USER_ID_KEY, childUserId) })
                .withString(RouterPath.Browser.JS_CALL_INTERCEPTOR_CLASS_KEY, ReturningSelectedChildInfoJsCallInterceptor::class.java.name)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    /**打开守护周报详情界面*/
    fun openGuardReportForChild(date: String?, childUserId: String) {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.GUARD_WEEKLY + "?date=$date&user_id=$childUserId")
//                .withBundle(RouterPath.Browser.ARGUMENTS_KEY, Bundle().apply { putString(CHILD_USER_ID_KEY, childUserId)  })
//                .withString(RouterPath.Browser.JS_CALL_INTERCEPTOR_CLASS_KEY, ReturningSelectedChildInfoJsCallInterceptor::class.java.name)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    /**打开邀请好友界面*/
    fun openInvitingFriendPage() {
        if (requireLogined()) {
            appRouter.build(RouterPath.Browser.PATH)
                    .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.INVITATION_FRIENDS)
                    .withString(RouterPath.Browser.JS_CALL_INTERCEPTOR_CLASS_KEY, ShareJsCallInterceptor::class.java.name)
                    .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                    .navigation()
        }
    }

    /**打开周报列表页面*/
    fun openWeeklyList() {
        if (requireLogined()) {
            if (mainActivity.supportFragmentManager.findFragmentByTag(WeeklyListFragment::class) != null) {
                return
            }
            mainActivity.inFragmentTransaction {
                addWithStack(fragment = WeeklyListFragment())
            }
        }
    }

    /**
     * 我的收益
     */
    fun openMyIncome() {
        if (requireLogined()) {
            appRouter.build(RouterPath.Browser.PATH)
                    .withString(RouterPath.Browser.URL_KEY, DataContext.saleBaseUrl())
                    .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                    .withBoolean(RouterPath.Browser.LOAD_NO_CACHE_ENABLE, true)//不使用缓存，每次更新
                    .navigation()
        }
    }

    /*fun openWeeklyDetail(url: String) {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, url)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, true)
                .withString(RouterPath.Browser.WEB_TITLE, mainActivity.getString(R.string.guarding_weekly))
                .navigation()
    }*/

    fun openIosDescriptionFileInstallingGuidePage() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.IOS_DESC_FILE_INSTALL)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    /**打开使用统计说明*/
    fun openUsingStatisticsInfo() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.USING_STATISTICS_INFO)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    /**打开孩子设备权限教程*/
    fun openChildDevicePermissionInfo() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.CHILD_DEVICE_PERMISSION_INFO)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    /**孩子设备离线排查*/
    fun openChildDeviceOfflineInfo() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.CHILD_DEVICE_OFFFLINE_REASON)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    /**打开手机使用记录*/
    fun openUsingRecordPage() {
        if (requireLogined()) {
            if (mainActivity.supportFragmentManager.findFragmentByTag(HomeUsingRecordFragment::class) != null) {
                return
            }
            mainActivity.inFragmentTransaction {
                addWithStack(fragment = HomeUsingRecordFragment())
            }
        }
    }

    /**
     * 打开权限详情
     */
    fun openPermissionDetailPage(permissions: List<PermissionDetail>) {
        if (requireLogined()) {
            if (mainActivity.supportFragmentManager.findFragmentByTag(PermissionDetailFragment::class) != null) {
                return
            }
            mainActivity.inFragmentTransaction {
                addWithStack(fragment = PermissionDetailFragment().apply {
                    arguments = Bundle().apply {
                        this.putParcelableArrayList("data", permissions.toArrayList())
                    }
                })
            }
        }
    }
}