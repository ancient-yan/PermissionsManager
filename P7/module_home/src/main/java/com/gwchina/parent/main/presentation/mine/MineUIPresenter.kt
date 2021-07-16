package com.gwchina.parent.main.presentation.mine

import android.view.ViewGroup
import com.android.base.kotlin.*
import com.android.base.utils.android.ResourceUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.MineResponse
import com.gwchina.sdk.base.data.api.isFlagPositive
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.formatMilliseconds
import com.gwchina.sdk.base.utils.mapParentAvatarBig
import com.gwchina.sdk.base.widget.member.getMemberTimingRemainingDay
import com.gwchina.sdk.base.widget.views.ItemConfiguration
import com.gwchina.sdk.base.widget.views.ItemManager
import kotlinx.android.synthetic.main.main_mine_stub_logout.*
import kotlinx.android.synthetic.main.mine_fragment.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-21 10:59
 */
internal class MineUIPresenter(private val host: MineFragment) {

    private lateinit var itemManager: ItemManager
    private val context = host.requireContext()
    private var logoutLayout: ViewGroup? = null
    private var childCardPresenter: CardPresenter? = null

    init {
        initEntrances()
        initParentLayout()
    }

    private fun initEntrances() {
        itemManager = ItemManager(context, ItemConfiguration(context.colorFromId(R.color.gray_cutting_line)))
        itemManager.setOnItemClickedListener { _, baseItem ->
            processEntranceClicked(baseItem.id)
        }
        itemManager.setup(host.rvMineEntrances, loadLoginItems())
    }

    private fun processEntranceClicked(entranceId: Int) {
        when (entranceId) {
            ENTRANCE_MESSAGE -> {
                StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_NEWS)
                host.mainNavigator.openMessageCenterPage()
            }
            ENTRANCE_FEEDBACK -> {
                StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_HELP)
                host.mainNavigator.openHelpingCenterPage()
            }
            ENTRANCE_ABOUT ->{
                StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_GELEI)
                host.mainNavigator.openAboutUsPage()
            }
            ENTRANCE_INVITATION ->{
                StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_INVITEFRIEND)
                host.mainNavigator.openInvitingFriendPage()
            }
            ENTRANCE_GUARD ->{
                StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_REPORT)
                host.mainNavigator.openWeeklyList()
            }
            //我的收益
            ENTRANCE_INCOME->{
                host.mainNavigator.openMyIncome()
            }
        }
    }

    private fun initParentLayout() {
        host.clMineTitleContent.setOnClickListener {
            host.mainNavigator.openPatriarchInfoPage()
        }
        host.llMineVipItem.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_MEMBERCENTER)
            host.mainNavigator.openMemberPage()
        }
    }

    fun showLogout() {
        childCardPresenter?.showCards(User.NOT_LOGIN)
        switchToLogoutLayout()
        host.tvMineMyChildSubtitle.setText(R.string.mine_click_to_login)
        showParentInfo(null)
        logoutLayout?.setOnClickListener {
            host.mainNavigator.openLoginPage()
        }
        host.tvMineParentName.clearComponentDrawable()
        setMemberTips(context.getString(R.string.open_member_enjoy_more_privileges))
        itemManager.updateTextItem(ENTRANCE_MESSAGE) {
            it.hasNotification = false
        }
    }

    fun showLoginedButNotBound(user: User) {
        childCardPresenter?.showCards(user)
        switchToLogoutLayout()
        host.tvMineMyChildSubtitle.setText(R.string.mine_click_to_binding)
        showParentInfo(user)
        logoutLayout?.setOnClickListener {
            host.mainNavigator.openAddDevicePage()
        }
    }

    private fun showParentInfo(user: User?) {
        if (user == null) {
            host.tvMineParentName.setText(R.string.login_register)
        }
        host.ivMineAvatar.setImageResource(mapParentAvatarBig(user?.currentChild?.p_relationship_code))
    }

    private fun switchToLogoutLayout() {
        if (logoutLayout == null) {
            logoutLayout = host.mineStubLogout.inflate() as ViewGroup
        }
        childCardPresenter?.hide()
        logoutLayout?.visible()
    }

    fun showChildCards(user: User) {
        //parent avatar
        showParentInfo(user)
        //hide others
        logoutLayout?.gone()
        //show info
        if (childCardPresenter == null) {
            childCardPresenter = CardPresenter(host, host.mineStubLogined.inflate() as ViewGroup)
        }
        childCardPresenter?.showCards(user)
    }

    fun showPageInfo(pageResponse: MineResponse) {
        val isVip = isFlagPositive(pageResponse.is_member)
        //message
        itemManager.updateTextItem(ENTRANCE_MESSAGE) {
            it.hasNotification = isFlagPositive(pageResponse.exist_notification)
        }
        itemManager.updateTextItem(ENTRANCE_GUARD) {
            it.subtitle = if(isFlagPositive(pageResponse.exist_report)) ResourceUtils.getString(R.string.new_weekly_tip) else ""
        }
        host.tvMineVipDiscount.text = if (pageResponse.member_word.isNullOrEmpty()) ResourceUtils.getString(R.string.vip_centre) else pageResponse.member_word
        if (isVip) {
            host.tvMineParentName.rightDrawable(R.drawable.mine_icon_vip)
            val endTime = pageResponse.end_time
            val remainingDay = getMemberTimingRemainingDay(endTime)
            if (remainingDay.shouldShowWillExpire) {
                //显示还剩多少天到期
                setMemberWarn(context.getString(R.string.after_x_days_maturity_mask, remainingDay.remainingDay))
            } else {
                //显示到期时间
                setMemberTips(formatMilliseconds(endTime) + host.getString(R.string.maturity))
            }
        } else {
            host.tvMineParentName.clearComponentDrawable()
            //显示开通会员
            setMemberTips(context.getString(R.string.open_member_enjoy_more_privileges))
        }

        //child growth values
        childCardPresenter?.showChildGrowthValue(pageResponse)
        if (!pageResponse.nick_name.isNullOrEmpty()) {
            host.tvMineParentName.text = pageResponse.nick_name.foldText(8)
        }
    }

    private fun setMemberTips(tips : String) {
        host.tvMineVipTime.text = tips
        host.tvMineVipTime.setTextColor(context.colorFromId(R.color.gold_level1))
        host.tvMineVipTime.background = null
    }

    private fun setMemberWarn(warn : String) {
        host.tvMineVipTime.text = warn
        host.tvMineVipTime.setTextColor(context.colorFromId(R.color.gray_level8))
        host.tvMineVipTime.setBackgroundResource(R.drawable.mine_bg_vip)
    }

}
