package com.gwchina.parent.main.presentation.home.card

import android.annotation.SuppressLint
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.Weekly
import com.gwchina.parent.main.presentation.home.CardInteractor
import com.gwchina.sdk.base.data.models.currentChildId
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.layoutCommonEdge
import kotlinx.android.synthetic.main.home_card_weekly.view.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-01 19:59
 */
class WeeklyCard @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private lateinit var interactor: CardInteractor

    init {
        setPadding(layoutCommonEdge, 0, layoutCommonEdge, 0)
        View.inflate(context, R.layout.home_card_weekly, this)
    }

    fun setup(cardInteractor: CardInteractor) {
        interactor = cardInteractor
        subscribeData()
    }

    private fun subscribeData() {
        interactor.cardDataProvider.observeHomeData {
            it?.weekly?.let(::showWeeklyData)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showWeeklyData(weekly: Weekly) {
        tvHomeWeeklyContent.text = weekly.week_describe
        tvHomeWeeklyTime.text = "${weekly.week_start_date}-${weekly.week_end_date}"
        rlHomeWeekly.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_WEEKLY)
            if (weekly.week_report_count > 1) {
                //有多个周报，进入周报列表
                interactor.navigator.openWeeklyList()
            } else if (weekly.week_guard_report != null) {
                //当前孩子有周报，进入当前孩子的周报详情
                interactor.navigator.openGuardReportForChild(weekly.week_guard_report.record_time, interactor.usingUser.currentChildId
                        ?: "")
            } else {
                //当前孩子没有周报，但其他孩子有周报
                val currentChildId = interactor.usingUser.currentChildId ?: ""
                interactor.usingUser.childList?.forEach {
                    if (currentChildId != it.child_user_id) {
                        interactor.navigator.openGuardReportForChild(weekly.week_guard_report?.record_time, it.child_user_id)
                        return@setOnClickListener
                    }
                }
            }
        }
    }
}