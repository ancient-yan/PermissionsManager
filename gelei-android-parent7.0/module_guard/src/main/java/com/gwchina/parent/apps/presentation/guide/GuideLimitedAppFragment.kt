package com.gwchina.parent.apps.presentation.guide

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.dagger.Injectable
import com.android.base.app.mvvm.getViewModelFromActivity
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardNavigator
import com.gwchina.parent.apps.widget.AppGuardMemberDialog
import com.gwchina.sdk.base.app.InjectorBaseFragment
import kotlinx.android.synthetic.main.apps_fragment_guide_free_app.*
import javax.inject.Inject

/**
 * 引导流程：限时可用列表
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-24 20:51
 */
class LimitAppFlowFragment : InjectorBaseFragment(), Injectable {

    @Inject
    lateinit var appGuardNavigator: AppGuardNavigator

    private val guideFlowViewModel by lazy {
        getViewModelFromActivity<GuideFlowViewModel>(viewModelFactory)
    }

    private val appsAdapter by lazy {
        GuideFlowAppsAdapter(this)
    }

    override fun provideLayout() = R.layout.apps_fragment_guide_free_app

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        //explain
        tvAppsSetFlowName.setText(R.string.limited_time_usable_apps)
        tvAppsSetFlowDesc.setText(R.string.app_group_limited_usable_apps_setting_tips)

        //list
        rvAppsSetFlowContent.layoutManager = LinearLayoutManager(context)
        rvAppsSetFlowContent.adapter = appsAdapter
        appsAdapter.replaceAll(guideFlowViewModel.appListLimited())
        appsAdapter.checkIsLimitCount = {
            val isLimit = guideFlowViewModel.isLimit(appsAdapter.checkedList.size)
            if (isLimit) {
                AppGuardMemberDialog.showTips(requireContext(), getString(R.string.app_guard_limit_tips3, guideFlowViewModel.maxCount), R.string.i_got_it)
            }
            isLimit
        }

        //completed
        btnAppsSetFlowNextStep.setOnClickListener {
            guideFlowViewModel.setLimitedList(appsAdapter.checkedList)
            appGuardNavigator.openGuideFlowSetFreeAppPage()
        }
    }
}
