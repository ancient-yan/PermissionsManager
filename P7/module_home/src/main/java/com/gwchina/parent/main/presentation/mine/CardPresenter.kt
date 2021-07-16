package com.gwchina.parent.main.presentation.mine

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.android.base.interfaces.adapter.OnPageChangeListenerAdapter
import com.android.base.kotlin.gone
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.android.base.utils.android.ResourceUtils.getString
import com.android.base.utils.android.UnitConverter
import com.android.base.utils.android.ViewUtils
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.MineResponse
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.*
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import kotlinx.android.synthetic.main.main_mine_stub_add_dev_tips.*
import kotlinx.android.synthetic.main.main_mine_stub_logined.*
import kotlinx.android.synthetic.main.mine_fragment.*

/**
 * 孩子卡片控制器
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-25 13:38
 */
class CardPresenter(private val host: MineFragment, private val cardLayout: ViewGroup) {

    companion object {
        private const val MINE_SHOW_MULTI_DEVICES_TIPS_FLAG = "mine_show_multi_devices_tips_flag"
    }

    private val itemCache = mutableListOf<ChildCardView>()
    private var showingUser: User? = null
    private var showingChildList: List<Child>? = null
    private var minePageResponse: MineResponse? = null
    private var reachMaxChildAndDeviceCount = false
    private var addTipsLayout: View? = null

    private val viewPager = host.vpMineChildren
    private lateinit var cardAdapter: CardAdapter


    init {
        setupViewPager()
        setupListeners()
    }

    private fun setupViewPager() {
        viewPager.offscreenPageLimit = 10//所有卡片不回收
        val createCardInteractorListeners = createCardInteractorListeners()
        for (i in 0..2) {
            itemCache.add(ChildCardView(cardLayout.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                cardListeners = createCardInteractorListeners
            })
        }
        ViewUtils.measureWithScreenSize(itemCache[0])
        viewPager.layoutParams = viewPager.layoutParams.apply {
            height = itemCache[0].measuredHeight
        }
        cardAdapter = CardAdapter(itemCache)
        viewPager.adapter = cardAdapter
    }

    private fun setupListeners() {
        host.ivMineAdd.setOnClickListener {
            val vipRule = showingUser?.vipRule ?: return@setOnClickListener
            hideMultiDevicesTips()
            StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_TOBINDDEVICE)
            val user = showingUser ?: return@setOnClickListener

            /*是会员，或者非会员允许添加一个孩子一个设备*/
            if (vipRule.home_mine_add_device_enabled == FLAG_POSITIVE_ACTION || user.guardDeviceCount() == 0) {
                if (reachMaxChildAndDeviceCount) {
                    ToastUtils.showShort(R.string.reach_max_device_count_tips)
                } else {
                    host.mainNavigator.openAddDevicePage(selectChild = true)
                }
            } else {
                OpenMemberDialog.show(host.requireContext()) {
                    message = getString(R.string.multi_device_guard_requirement_member_tips_mask, vipRule.home_mine_add_device_enabled_minimum_level)
                    messageDescId = R.string.multi_device_guard_requirement_member_desc
                    positiveText = getString(R.string.open_vip_to_experience_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
                }
            }
        }

        viewPager.addOnPageChangeListener(object : OnPageChangeListenerAdapter {
            override fun onPageSelected(position: Int) {
                host.indicatorChild.setSelect(position)
            }
        })
    }

    private fun hideMultiDevicesTips() {
        AppSettings.settingsStorage().putBoolean(MINE_SHOW_MULTI_DEVICES_TIPS_FLAG, false)
        addTipsLayout?.gone()
    }

    private fun showMultiDevicesTipsChecked() {
        if (AppSettings.settingsStorage().getBoolean(MINE_SHOW_MULTI_DEVICES_TIPS_FLAG, true) && !reachMaxChildAndDeviceCount) {
            if (addTipsLayout == null) {
                addTipsLayout = host.mineStubAddTips.inflate()
            }
            host.clMineMultiDeviceTips.visible()
            host.tvMineMultiDeviceTipsClose.setOnClickListener {
                hideMultiDevicesTips()
            }
        }
    }

    fun hide() {
        cardLayout.gone()
    }

    fun showCards(user: User) {
        if (showingUser == user) {
            return
        }

        checkIfNewDeviceAdded(showingUser, user)
        showingUser = user

        reachMaxChildAndDeviceCount = user.reachMaximumChildAndDeviceLimit()

        showCardChecked(user)
        cardLayout.visible()

        showMultiDevicesTipsChecked()
    }

    /*用户通过其他途径添加了超多一台设备的话，气泡也自动消失*/
    private fun checkIfNewDeviceAdded(previous: User?, newUser: User) {
        if (previous == null) {
            return
        }
        val previousCount = previous.childList?.fold(0) { total, child ->
            total + (child.device_list?.size ?: 0)
        } ?: 0
        val newCount = newUser.childList?.fold(0) { total, child ->
            total + (child.device_list?.size ?: 0)
        } ?: 0
        if ((previousCount < newCount)) {
            hideMultiDevicesTips()
        }
    }

    private fun showCardChecked(user: User) {
        val newChildList = user.childList
        val pageCountChanged = showingChildList?.size != newChildList?.size
        showingChildList = newChildList
        val childList = showingChildList

        if (childList != null) {
            //bind data
            val itemSize = itemCache.size
            childList.forEachIndexed { index, child ->
                if (itemSize > index) {
                    itemCache[index].showChild(child)
                }
            }
            //adjust view pager
            if (childList.size <= 1) {
                setViewPagerMargin(UnitConverter.dpToPx(14))
                viewPager.pageMargin = 0
                host.indicatorChild.invisible()
            } else {
                setViewPagerMargin(UnitConverter.dpToPx(20))
                viewPager.pageMargin = UnitConverter.dpToPx(10)
                host.indicatorChild.addIndicatorView(childList.size)
                host.indicatorChild.setSelect(viewPager.currentItem)
                host.indicatorChild.visible()
            }
            //pageCountChanged, notify view pager.
            if (pageCountChanged) {
                //remember old position
                val currentItem = viewPager.currentItem
                cardAdapter.list = childList
                //recovery position
                viewPager.currentItem = currentItem
            }
            //refresh growth value
            val previousResponse = this.minePageResponse
            if (previousResponse != null) {
                showChildGrowthValueReally(previousResponse)
            }
        } else {
            cardAdapter.list = emptyList()
            host.indicatorChild.invisible()
        }
    }

    fun showChildGrowthValue(minePageResponse: MineResponse) {
        val previous = this.minePageResponse
        if (previous != minePageResponse) {
            this.minePageResponse = minePageResponse
            showChildGrowthValueReally(minePageResponse)
        }
    }

    private fun showChildGrowthValueReally(minePageResponse: MineResponse) {
        //check
        val list = minePageResponse.level_list ?: return
        val maxValue = list.maxBy { it.level }?.level ?: 10/*current max value*/
        //bind values
        itemCache.forEach { view ->
            minePageResponse.child_list?.find { info ->
                view.boundChild?.child_user_id == info.child_user_id
            }?.let {
                view.showChildGrowthInfo(list, maxValue, it)
            }
        }
    }

    private fun setViewPagerMargin(margin: Int) {
        val layoutParams = viewPager.layoutParams
        if (layoutParams is ViewGroup.MarginLayoutParams) {
            layoutParams.leftMargin = margin
            layoutParams.rightMargin = margin
            viewPager.layoutParams = layoutParams
        }
    }

    private fun createCardInteractorListeners() = CardListeners(
            //进入成长树
            checkChildGrowthTree = {
                if (isMemberGuardExpired(it.status)) {
                    host.showMessage("当前孩子守护设备已过期喔")
                } else {
                    host.mainNavigator.openGrowthTreePage(it.child_user_id)
                }
            },
            //查看已绑设备
            checkChildBoundDevice = {
                host.mainNavigator.openBoundDevicePageForChild(it.child_user_id)
            },
            //检查孩子信息
            checkChildInfo = {
                host.mainNavigator.openChildInfoPage(it.child_user_id)
            },
            //查看设备守护等级
            checkDeviceGuardLevel = {
                val device = it.device_list?.firstOrNull() ?: return@CardListeners
                if (!isMemberGuardExpired(device.status)) {
                    host.mainNavigator.openGuardLevelForDevice(device.device_id)
                } else {
                    showFunctionNeedMemberDialog()
                }
            },
            //为孩子直接添加设备或者为设备设置守护等级
            addDeviceOrSetLevelDirectly = { it ->
                val user = showingUser ?: return@CardListeners

                if (it.boundDevice()) {
                    //为设备设置守护等级
                    if (isMemberGuardExpired(it.status)) {
                        showFunctionNeedMemberDialog()
                    } else {
                        if (user.currentChildId == it.child_user_id) {
                            user.currentChildDeviceId?.let { deviceId ->
                                host.mainNavigator.openGuardLevelForDevice(deviceId)
                            }
                        } else {
                            //child had bound device then its device list cannot be null or empty.
                            it.device_list?.find { device ->
                                !isMemberGuardExpired(device.status)
                            }?.let { device ->
                                host.mainNavigator.openGuardLevelForDevice(device.device_id)
                            }
                        }
                    }

                } else {
                    //为孩子直接添加设备
                    if (user.vipRule?.home_mine_add_device_enabled == FLAG_POSITIVE_ACTION || user.guardDeviceCount() == 0) {
                        host.mainNavigator.openAddDevicePage(it.child_user_id)
                    } else {
                        showFunctionNeedMemberDialog(user.vipRule)
                    }
                }
            })

    private fun showFunctionNeedMemberDialog(vipRule: VipRule? = null) {
        OpenMemberDialog.show(host.requireContext()) {
            message = getString(R.string.multi_device_guard_requirement_member_tips_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
            messageDesc = getString(R.string.open_member_recovery_guard_function_tips_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
            positiveText = getString(R.string.open_vip_to_experience_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
        }
    }
}

private class CardAdapter(private val itemCache: List<ChildCardView>) : PagerAdapter() {

    var list: List<Child> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return itemCache[position].apply {
            container.addView(this)
        }
    }

    override fun isViewFromObject(p0: View, p1: Any): Boolean {
        return p0 == p1
    }

    override fun getCount() = list.size

    override fun destroyItem(container: ViewGroup, position: Int, item: Any) {
        container.removeView(item as View)
    }

    override fun getItemPosition(`object`: Any) = POSITION_NONE

}