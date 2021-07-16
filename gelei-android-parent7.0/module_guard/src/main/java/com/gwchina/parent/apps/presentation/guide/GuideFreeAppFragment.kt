package com.gwchina.parent.apps.presentation.guide

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.dagger.Injectable
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.kotlin.dip
import com.android.base.rx.bindLifecycle
import com.android.base.rx.observeOnUI
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardNavigator
import com.gwchina.parent.apps.common.AppGuardResource
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import kotlinx.android.synthetic.main.apps_fragment_guide_free_app.*
import javax.inject.Inject

/**
 * 引导流程：自由可用设置
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-24 14:31
 */
class GuideFreeAppFragment : InjectorBaseStateFragment(), Injectable {

    @Inject lateinit var appGuardNavigator: AppGuardNavigator
    @Inject lateinit var appGuardResource: AppGuardResource

    private val guideFlowViewModel by lazy {
        getViewModelFromActivity<GuideFlowViewModel>(viewModelFactory)
    }

    private val appsAdapter by lazy {
        GuideFlowAppsAdapter(this, false)
    }

    override fun provideLayout() = R.layout.apps_fragment_guide_free_app

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        tvAppsSetFlowName.setText(R.string.free_usable_apps)
        tvAppsSetFlowDesc.setText(R.string.free_usable_apps_set_tips)

        //list
        rvAppsSetFlowContent.layoutManager = LinearLayoutManager(context)
        rvAppsSetFlowContent.addItemDecoration(MarginDecoration(0, 0, 0, dip(5)))
        appsAdapter.replaceAll(guideFlowViewModel.appListFree())
        rvAppsSetFlowContent.adapter = appsAdapter

        btnAppsSetFlowNextStep.setText(R.string.complete)
        btnAppsSetFlowNextStep.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_START_BTN_FINISH)
            doSubmit()
        }
    }

    private fun doSubmit() {
        showLoadingDialog(false)
        guideFlowViewModel.submit()
                .observeOnUI()
                .doOnTerminate { dismissLoadingDialog() }
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe(
                        {
                            appGuardNavigator.openAppRulesPage()
                        },
                        {
                            errorHandler.handleError(it)
                        }
                )
    }
}