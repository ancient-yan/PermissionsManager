package com.gwchina.parent.main.presentation.mine

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.android.base.kotlin.invisible
import com.android.base.kotlin.onDebouncedClick
import com.android.base.kotlin.visible
import com.android.base.kotlin.visibleOrGone
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.ChildGrowthValue
import com.gwchina.parent.main.data.ChildInfo
import com.gwchina.parent.main.utils.createChildAgeGradeInfo
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.mapChildAvatarSmall
import com.gwchina.sdk.base.utils.mapParentRelation
import kotlinx.android.synthetic.main.main_mine_child_card.view.*
import kotlin.math.min

/**
 * 孩子卡片视图
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-25 11:03
 */
class ChildCardView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var showingChild: Child? = null
    private var childInfo: ChildInfo? = null
    private var maxLevelValue: Int = 0
    private var levelInfo: List<ChildGrowthValue>? = null

    var cardListeners: CardListeners? = null
    val boundChild: Child?
        get() = showingChild

    init {
        inflate(context, R.layout.main_mine_child_card, this)
        setListeners()
    }

    private fun setListeners() {
        // click：已绑设备
        tvMineBoundDevice.setOnClickListener {
            val child = boundChild
            if (child != null) {
                cardListeners?.checkChildBoundDevice?.invoke(child)
            }
            StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_BINDDEVICE)
        }
        // click：守护等级
        tvMineSolutions.setOnClickListener {
            val child = boundChild
            if (child != null) {
                cardListeners?.checkDeviceGuardLevel?.invoke(child)
            }
            StatisticalManager.onEvent(UMEvent.ClickEvent.MYVIEW_BTN_GROWTLEVEV)
        }
        // click：孩子信息
        clMineLoginedChildInfoLayout.onDebouncedClick {
            val child = boundChild
            if (child != null) {
                cardListeners?.checkChildInfo?.invoke(child)
            }
        }
        // click：成长中心
        clMineLoginedGrowthInfoLayout.setOnClickListener {
            val child = boundChild
            if (child != null) {
                if (isMemberGuardExpired(child.status)) {
                    ToastUtils.showShort(R.string.current_child_device_expire)
                } else {
                    cardListeners?.checkChildGrowthTree?.invoke(child)
                }
            }
        }
        // click：绑定设备或者设置守护等级
        tvMineBindingTips.setOnClickListener {
            val child = boundChild
            if (child != null) {
                cardListeners?.addDeviceOrSetLevelDirectly?.invoke(child)
            }
        }
    }

    fun showChild(child: Child) {
        if (showingChild != child) {
            showingChild = child
            showChildInfo(child)
            //show level info
            val info = childInfo
            val list = levelInfo
            if (info != null && list != null && child.child_user_id == info.child_user_id) {
                showGrowthInfo(list, info)
            }
        }
    }

    fun showChildGrowthInfo(levelInfo: List<ChildGrowthValue>, maxLevelValue: Int, childInfo: ChildInfo) {
        this.childInfo = childInfo
        this.levelInfo = levelInfo
        this.maxLevelValue = maxLevelValue
        val child = showingChild
        if (child != null && child.child_user_id == childInfo.child_user_id) {
            showGrowthInfo(levelInfo, childInfo)
        }
    }

    private fun showGrowthInfo(levelInfo: List<ChildGrowthValue>, childInfo: ChildInfo) {
        tvMineChildGrowProgressValue.text = context.getString(R.string.grow_value_mask, childInfo.child_growth)
        levelInfo.find {
            it.level == childInfo.child_level
        }?.let {
            gvvMineGrowValues.setCurrentProgress((childInfo.child_growth - it.begin_growth).toFloat() / (it.end_growth - it.begin_growth).toFloat())
        }
        tvMineStartLevel.text = context.getString(R.string.grow_level_mask, childInfo.child_level)
        tvMineEndLevel.text = context.getString(R.string.grow_level_mask, min(childInfo.child_level + 1, maxLevelValue))
    }

    private fun showChildInfo(child: Child) {
        //basic info
        tvMineChildAvatar.setImageResource(mapChildAvatarSmall(child.sex))
        tvMineLoginedChildName.text = child.nick_name.foldText(10)
        mtvMineLoginedChildAge.text = createChildAgeGradeInfo(child)
        tvMineChildRelation.text = context.getString(mapParentRelation(child.p_relationship_code, child.sex))
        //expired
        tvMineExpiredFlag.visibleOrGone(isMemberGuardExpired(child.status))
        //device count
        val onDevice = !child.boundDevice()
        val allDeviceNoLevel = !child.device_list.isNullOrEmpty() && child.device_list?.all { !it.hasSetLevel() } == true
        if (onDevice || allDeviceNoLevel) {
            clMineLoginedGrowthInfoLayout.invisible()
            clMineLoginedCutting.invisible()
            clMineEntrance.invisible()
            tvMineBindingTips.visible()
            if (onDevice) {
                tvMineBindingTips.setText(R.string.mine_click_to_binding)
            } else {
                tvMineBindingTips.setText(R.string.mine_click_bind_to_set_level)
            }
        } else {
            clMineLoginedGrowthInfoLayout.visible()
            clMineLoginedCutting.visible()
            clMineEntrance.visible()
            tvMineBindingTips.invisible()
            if (child.device_list?.size?:0 > 1) {
                tvMineBoundDevice.text = context.getString(R.string.bound_devices_count_mask, child.device_list?.size ?: 0)
            } else {
                tvMineBoundDevice.text = context.getString(R.string.bound_devices_mask)
            }
        }
    }

}

class CardListeners(
        val checkChildInfo: ((child: Child) -> Unit),
        val checkChildBoundDevice: ((child: Child) -> Unit),
        val checkDeviceGuardLevel: ((child: Child) -> Unit),
        val checkChildGrowthTree: ((child: Child) -> Unit),
        val addDeviceOrSetLevelDirectly: ((child: Child) -> Unit)
)