package com.gwchina.parent.times;

import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.*
import com.gwchina.parent.times.common.TimeGuardWeeklyPlanVO
import com.gwchina.parent.times.presentation.guide.ChooseMakingPlanWayFragment
import com.gwchina.parent.times.presentation.make.MakeTimeGuardPlanFragment
import com.gwchina.parent.times.presentation.make.MakeTimeGuardPlanFragment2
import com.gwchina.parent.times.presentation.make.MakingPlanInfo
import com.gwchina.parent.times.presentation.spare.EditSparePlanFragment
import com.gwchina.parent.times.presentation.spare.SparePlanFragment
import com.gwchina.parent.times.presentation.table.TimeGuardTableFragment
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject

@ActivityScope
class TimeGuardNavigator @Inject constructor(
        private val timeGuardActivity: TimeGuardActivity,
        private val appRouter: AppRouter
) {

    fun openSelectingSparePlanPage() {
        timeGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = SparePlanFragment.newInstance(true))
        }
    }

    fun openSetGuardTimePlanPage() {
        timeGuardActivity.inFragmentTransaction {
//            replaceWithStack(fragment = MakeTimeGuardPlanFragment.newInstance(MakingPlanInfo.newInstanceForMakingNormalPlan()))
            replaceWithStack(fragment = MakeTimeGuardPlanFragment2.newInstance(MakingPlanInfo.newInstanceForMakingNormalPlan()))
        }
    }

    fun openChooseMakingPlanWayPage() {
        timeGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = ChooseMakingPlanWayFragment())
        }
    }

    fun openAddSparePlanPage(usedName: List<String>? = null) {
        timeGuardActivity.inFragmentTransaction {
//            val fragment = MakeTimeGuardPlanFragment.newInstance(MakingPlanInfo.newInstanceForMakingSparePlan(usedName))
            val fragment = MakeTimeGuardPlanFragment2.newInstance(MakingPlanInfo.newInstanceForMakingSparePlan(usedName))
            replaceWithStack(fragment = fragment)
        }
    }

    fun openSetGuardTimePlanPage(notOptionalDays: List<Int>, selectedDays: List<Int>) {
        timeGuardActivity.inFragmentTransaction {
//            val fragment = MakeTimeGuardPlanFragment.newInstance(MakingPlanInfo.newInstanceForMakingNormalPlan(notOptionalDays, selectedDays))
            val fragment = MakeTimeGuardPlanFragment2.newInstance(MakingPlanInfo.newInstanceForMakingNormalPlan(notOptionalDays, selectedDays))
            replaceWithStack(fragment = fragment)
        }
    }

    fun openGuardTimePlanTablePage() {
        val guardRulesFragment: TimeGuardTableFragment? = timeGuardActivity.supportFragmentManager.findFragmentByTag(TimeGuardTableFragment::class)
        if (guardRulesFragment != null) {
            timeGuardActivity.supportFragmentManager.backToFragment(TimeGuardTableFragment::class.java.name)
        } else {
            timeGuardActivity.supportFragmentManager.clearBackStack(true)
            timeGuardActivity.window.decorView.postDelayed({
                timeGuardActivity.inSafelyFragmentTransaction {
                    replaceWithDefaultContainer(fragment = TimeGuardTableFragment())
                }
            }, 100)
        }
    }

    fun openSparePlanListPage() {
        val sparePlanFragment: SparePlanFragment? = timeGuardActivity.supportFragmentManager.findFragmentByTag(SparePlanFragment::class)
        if (sparePlanFragment != null) {
            timeGuardActivity.supportFragmentManager.backToFragment(SparePlanFragment::class.java.name)
        } else {
            timeGuardActivity.supportFragmentManager.clearBackStack(true)
            timeGuardActivity.window.decorView.postDelayed({
                timeGuardActivity.inSafelyFragmentTransaction {
                    replaceWithStack(fragment = SparePlanFragment.newInstance(false))
                }
            }, 100)
        }
    }

    fun openEditingSparePlanPage(timeGuardWeeklyPlanVO: TimeGuardWeeklyPlanVO) {
        timeGuardActivity.inFragmentTransaction {
            replaceWithStack(fragment = EditSparePlanFragment.newInstance(timeGuardWeeklyPlanVO))
        }
    }

    fun openIosSuperviseModePage() {
        appRouter.build(RouterPath.IOSSuperviseMode.PATH).navigation()
    }

    fun openMemberCenter() {
        appRouter.build(RouterPath.MemberCenter.PATH)
                .withInt(RouterPath.PAGE_KEY, RouterPath.MemberCenter.CENTER)
                .navigation()
    }

}