package com.gwchina.parent.times.presentation.guide

import android.arch.lifecycle.Observer
import android.os.Bundle
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.data.onSuccessWithData
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.TimeGuardNavigator
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.models.isIOS
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.SuperviseModeSynchronizer
import kotlinx.android.synthetic.main.times_fragment_guide.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-11 15:53
 */
class TimeGuardGuideFragment : InjectorBaseFragment() {

    @Inject lateinit var timeGuardNavigator: TimeGuardNavigator

    private var subscribedLoadingStatus = false

    private lateinit var superviseModeSynchronizer: SuperviseModeSynchronizer

    private val timeGuardGuideViewModel by lazy {
        getViewModel<TimeGuardGuideViewModel>(viewModelFactory)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        superviseModeSynchronizer = SuperviseModeSynchronizer(timeGuardGuideViewModel.childUserId, timeGuardGuideViewModel.childDeviceId)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.times_fragment_guide

    private fun setupWhenNotVIP() {
        btnTimeAddPlan.text = getString(R.string.open_vip_to_experience_mask, timeGuardGuideViewModel.user.value?.vipRule?.home_time_enter_minimum_level)
        tvTimesGuideTips.gone()
        btnTimeAddPlan.setOnClickListener {
            timeGuardNavigator.openMemberCenter()
        }
    }

    private fun setupWhenVIP() {
        tvTimesGuideTips.visible()
        btnTimeAddPlan.setText(R.string.add_guard_time_plan)
        btnTimeAddPlan.setOnClickListener {
            val device = timeGuardGuideViewModel.device
            if (device.isIOS()) {
                superviseModeSynchronizer.startSync()
            } else {
                toSetTimeGuard()
            }
        }
    }

    private fun toSetTimeGuard() {
        val resource = timeGuardGuideViewModel.loadingSparePlanStatus.value
        when {
            resource?.isSuccess == true -> toSetGuardTimePlan(resource.get())
            subscribedLoadingStatus -> timeGuardGuideViewModel.fetchSparePlanStatus()
            else -> {
                subscribedLoadingStatus = true
                subscribeSparePlanStatus()
            }
        }
    }

    private fun subscribeSparePlanStatus() {
        timeGuardGuideViewModel.loadingSparePlanStatus
                .observe(this, Observer {
                    it?.onLoading {
                        showLoadingDialog(false)
                    }?.onSuccess { result ->
                        dismissLoadingDialog()
                        toSetGuardTimePlan(result)
                    }?.onError { error ->
                        dismissLoadingDialog()
                        errorHandler.handleError(error)
                    }
                })
    }

    private fun toSetGuardTimePlan(result: Boolean?) {
        if (result == true) {
            timeGuardNavigator.openChooseMakingPlanWayPage()
        } else {
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_START)
            timeGuardNavigator.openSetGuardTimePlanPage()
        }
    }

    private fun subscribeViewModel() {
        timeGuardGuideViewModel.user
                .observe(this, Observer {
                    if (it?.vipRule?.home_time_enter == FLAG_POSITIVE_ACTION) {
                        setupWhenVIP()
                    } else {
                        setupWhenNotVIP()
                    }
                })

        superviseModeSynchronizer.state.observe(this, Observer {
            it?.onSuccessWithData { result ->
                dismissLoadingDialog()
                if (result) {
                    toSetTimeGuard()
                } else {
                    timeGuardNavigator.openIosSuperviseModePage()
                }
            }?.onLoading {
                showLoadingDialog(false)
            }?.onError { err ->
                dismissLoadingDialog()
                errorHandler.handleError(err)
            }
        })

    }

}