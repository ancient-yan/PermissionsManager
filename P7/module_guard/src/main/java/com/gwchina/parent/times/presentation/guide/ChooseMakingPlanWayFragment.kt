package com.gwchina.parent.times.presentation.guide

import android.os.Bundle
import android.view.View
import com.android.base.kotlin.onDebouncedClick
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.TimeGuardNavigator
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import kotlinx.android.synthetic.main.times_fragment_choose_making_plan_way.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-23 11:08
 */
class ChooseMakingPlanWayFragment : InjectorBaseFragment() {

    @Inject lateinit var timeGuardNavigator: TimeGuardNavigator

    override fun provideLayout() = R.layout.times_fragment_choose_making_plan_way

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        tvTimesUseSparePlan.onDebouncedClick {
            timeGuardNavigator.openSelectingSparePlanPage()
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_START_BTN_SPAREPLAN)
        }
        tvTimesMakeNewPlan.onDebouncedClick {
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_START_BTN_ADDPLAN)
            timeGuardNavigator.openSetGuardTimePlanPage()
        }
    }

}