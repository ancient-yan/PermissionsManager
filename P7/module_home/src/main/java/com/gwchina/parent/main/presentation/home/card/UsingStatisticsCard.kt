package com.gwchina.parent.main.presentation.home.card

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.LinearLayout
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.UseOverview
import com.gwchina.parent.main.presentation.home.CardInteractor
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.models.isMember
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.data.models.timePlanHasBeSet
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.formatSecondsToTimeText
import com.gwchina.sdk.base.utils.layoutCommonEdge
import kotlinx.android.synthetic.main.home_card_using_statistics.view.*
import kotlinx.android.synthetic.main.home_card_using_statistics_item.view.*
import kotlinx.android.synthetic.main.home_using_statistics_pop.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

/**
 * 数据统计卡片
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-31 14:39
 */
class UsingStatisticsCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var interactor: CardInteractor
    private var useOverview: UseOverview? = null

    init {
        orientation = VERTICAL
        setPadding(layoutCommonEdge, 0, layoutCommonEdge, 0)
        View.inflate(context, R.layout.home_card_using_statistics, this)
    }

    private val onClickOpenStatisticsPageListener = OnClickListener {
        interactor.navigator.openGuardStatisticsPage()
    }

    private val onClickOpenCommonJumpListener = OnClickListener {
        interactor.navigator.commonJump()
        when {
            it.id == R.id.tvHomeUsingStatisticsTime -> StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_TODAYUESD)
            it.id == R.id.tvHomeUsingStatisticsPreference -> StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_TODAYFAVOURITE)
            it.id == R.id.tvHomeUsingStatisticsSteps -> StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_TODAYSTEPNUM)
        }
    }
    //点击问号
    private val onClickTipsListener = OnClickListener {
        setClosedTips()
        interactor.navigator.openUsingStatisticsInfo()
    }

    fun setup(cardInteractor: CardInteractor) {
        interactor = cardInteractor
        subscribeData()
    }

    private fun subscribeData() {
        interactor.cardDataProvider.observeUser {
            if (!it.logined() || it.currentChild == null || it.currentDevice == null) {
                setupViewsWhenNoDevice()
            } else {
                setupViewsNormally()
            }
        }
        interactor.cardDataProvider.observeHomeData {
            Timber.d("observeHomeData $it")
            if (useOverview != it?.useOverview) {
                showValue(it?.useOverview)
                useOverview = it?.useOverview
            }
        }
    }

    private fun setupViewsWhenNoDevice() {
        tvHomeUsingStatisticsTime.run {
            setIcon(R.drawable.home_icon_time_statistics_gray)
            setName(context.getString(R.string.used_at_today))
            setValue(context.getString(R.string.used_at_today_dummy_value))
            setUsable(false)
            setOnClickListener(onClickOpenCommonJumpListener)
        }

        tvHomeUsingStatisticsPreference.run {
            setIcon(R.drawable.home_icon_like_statistics_gray)
            setName(context.getString(R.string.today_preference))
            setValue(context.getString(R.string.today_preference_dummy_value))
            setUsable(false)
            setOnClickListener(onClickOpenCommonJumpListener)
        }

        tvHomeUsingStatisticsSteps.run {
            setIcon(R.drawable.home_icon_motion_statistics_gray)
            setName(context.getString(R.string.today_steps))
            setValue(context.getString(R.string.today_steps_dummy_value))
            setUsable(false)
            setOnClickListener(onClickOpenCommonJumpListener)
        }

        tvHomeUsingStatisticsTitle.run {
            paint.typeface = Typeface.DEFAULT
            gravity = Gravity.CENTER
            textSize = 14F
            setTextColor(colorFromId(R.color.gray_level3))
            text = resources.getString(R.string.home_card_using_statistics_desc)
            invalidate()
        }
        ivTips.gone()
        ivMore.gone()
        space.gone()
        ivTips.setOnClickListener(onClickTipsListener)
        llUsingStatisticsTitle.setOnClickListener(onClickOpenCommonJumpListener)
        setUsingStatisticsTipsVisible(false)
    }

    private fun setUsingStatisticsTipsVisible(isVisible: Boolean) {
        if (isVisible) {
            interactor.host.llHomeUsingStatisticsTips.visible()
            interactor.host.tvHomeUsingStatisticsDelete.setOnClickListener {
                setClosedTips()
            }
            interactor.host.tvHomeUsingStatisticsTips.setOnClickListener(onClickTipsListener)
        } else {
            interactor.host.llHomeUsingStatisticsTips.gone()
        }
    }

    private fun setupViewsNormally() {
        tvHomeUsingStatisticsTime.run {
            setIcon(R.drawable.home_icon_time_statistics)
            setUsable(true)
            setOnClickListener(onClickOpenStatisticsPageListener)
        }

        tvHomeUsingStatisticsPreference.run {
            setIcon(R.drawable.home_icon_statistics_like)
            setUsable(true)
            setOnClickListener(onClickOpenStatisticsPageListener)
        }

        tvHomeUsingStatisticsSteps.run {
            setIcon(R.drawable.home_icon_motion_statistics)
            setUsable(true)
            setOnClickListener(onClickOpenStatisticsPageListener)
        }

        tvHomeUsingStatisticsTitle.run {
           typeface= Typeface.DEFAULT_BOLD
            gravity = Gravity.START
            textSize = 18F
            setTextColor(colorFromId(R.color.gray_level1))
            text = resources.getString(R.string.using_statistics)
            invalidate()
        }
        ivTips.visible()
        ivMore.visible()
        space.visible()
        llUsingStatisticsTitle.setOnClickListener(onClickOpenStatisticsPageListener)
        showTipsRule()
    }

    private fun showValue(useOverview: UseOverview?) {
        Timber.d("showValue $useOverview")
        tvHomeUsingStatisticsTime.setValue(formatSecondsToTimeText(useOverview?.to_day_use_time
                ?: 0))
        tvHomeUsingStatisticsPreference.setValue(useOverview?.to_day_preference_soft_type
                ?: resources.getString(R.string.no_data))
        tvHomeUsingStatisticsSteps.setValue(resources.getString(R.string.steps_mask, (useOverview?.to_day_step_sport
                ?: 0).toString()))
    }

    companion object {
        private const val USING_STATISTICS_TIPS = "usingStatisticsTips"
    }

    private fun setClosedTips() {
        setUsingStatisticsTipsVisible(false)
        AppSettings.settingsStorage().putBoolean(USING_STATISTICS_TIPS, true)
    }

    /**
     * 首页统计说明显示规则：
     * 1.已设置规则的老用户，初次使用新版本时出现提示；
     * 2.首次完成守护时间设置后的第二天0点开始出现提示气泡；
     */
    @SuppressLint("SimpleDateFormat")
    private fun showTipsRule() {
        if (AppSettings.settingsStorage().getBoolean(USING_STATISTICS_TIPS, false)) return
        if (!AppContext.appDataSource().user().isMember) return
        //是会员并且设置过了时间守护
        if (AppContext.appDataSource().user().currentDevice.timePlanHasBeSet()) {
            setUsingStatisticsTipsVisible(true)
        } else {
            val tomorrowDate = AppSettings.settingsStorage().getString("haveSetTimeGuardKey")
            val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
            if (!tomorrowDate.isNullOrEmpty() && currentDate > tomorrowDate) {
                setUsingStatisticsTipsVisible(true)
            }
        }
    }
}

class UsingStatisticsCardItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        setPadding(0, dip(15), 0, dip(15))
        orientation = HORIZONTAL
        View.inflate(context, R.layout.home_card_using_statistics_item, this)
    }

    fun setIcon(@DrawableRes iconRes: Int) {
        tvHomeUsingStatisticsIcon.setImageResource(iconRes)
    }

    fun setValue(value: String?) {
        tvHomeUsingStatisticsValue.text = value
    }

    fun setName(name: String) {
        tvHomeUsingStatisticsName.text = name
    }

    fun setUsable(usable: Boolean) {
        tvHomeUsingStatisticsName.setTextColor(
                if (usable) {
                    colorFromId(R.color.gray_level2)
                } else {
                    colorFromId(R.color.gray_level3)
                }
        )
        tvHomeUsingStatisticsValue.setTextColor(
                if (usable) {
                    colorFromId(R.color.gray_level1)
                } else {
                    colorFromId(R.color.gray_level2)
                }
        )
    }
}