package com.gwchina.parent.apps.presentation.guide

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.dagger.Injectable
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.utils.android.UnitConverter
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardNavigator
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.widget.AppGuardMemberDialog
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.apps_fragment_guide_free_app.*
import javax.inject.Inject

/**
 * 引导流程：设置禁用列表
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-24 14:31
 */
class GuideDisabledAppFragment : InjectorBaseStateFragment(), Injectable {

    @Inject lateinit var appGuardNavigator: AppGuardNavigator

    private val guideFlowViewModel by lazy {
        getViewModelFromActivity<GuideFlowViewModel>(viewModelFactory)
    }

    private val appsAdapter by lazy {
        GuideFlowAppsAdapter(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.apps_fragment_guide_free_app

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        //explain
        tvAppsSetFlowName.setText(R.string.disable_app_list)
        tvAppsSetFlowDesc.setText(R.string.set_disable_app_list_tips)

        //list
        rvAppsSetFlowContent.layoutManager = LinearLayoutManager(context)
        rvAppsSetFlowContent.addItemDecoration(MarginDecoration(0, 0, 0, UnitConverter.dpToPx(5)))
        rvAppsSetFlowContent.adapter = appsAdapter
        appsAdapter.checkIsLimitCount = {
            val isLimit = guideFlowViewModel.isLimitDisable(appsAdapter.checkedList.size)
            if (isLimit) {
                AppGuardMemberDialog.showTips(requireContext(), getString(R.string.app_guard_limit_tips3, guideFlowViewModel.maxCount), R.string.i_got_it)
            }
            isLimit
        }

        //next step
        btnAppsSetFlowNextStep.setOnClickListener {
            guideFlowViewModel.setDisableList(appsAdapter.checkedList)
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_START_BTN_PROHIBIT)
            appGuardNavigator.openGuideFlowSetLimitedAppPage()
        }

        //title
        gtlAppsSetFlowTitle.setOnNavigationOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun subscribeViewModel() {
        guideFlowViewModel.appList.observe(this, Observer {
            it?.let { resource ->
                processResource(resource)
            }
        })
    }

    override fun onRefresh() {
        super.onRefresh()
        guideFlowViewModel.reload()
    }


    private fun processResource(resource: Resource<List<App>>) {
        resource.onSuccess {
            val mustFreeList = guideFlowViewModel.appListMustFreeUsable()
            if (mustFreeList.isNullOrEmpty()) {
                showEmptyLayout()
            } else {
                appsAdapter.replaceAll(mustFreeList)
                showContentLayout()
//                checkIsShowSVipDialog()
            }
        }.onError {
            processErrorWithStatus(it)
        }.onLoading {
            showLoadingLayout()
        }
    }

    private fun checkIsShowSVipDialog() {
        if (guideFlowViewModel.maxCount != 0) {
            AppGuardMemberDialog.showTips(requireContext(), getString(R.string.app_guard_limit_tips1, guideFlowViewModel.maxCount), R.string.know_start_setting)
        }
    }

    override fun handleBackPress(): Boolean {
        if (appsAdapter.checkedList.isEmpty()) {
            return false
        }
        showConfirmDialog {
            messageId = R.string.save_edits_tips
            positiveListener = {
                exitFragment()
            }
        }
        return true
    }

}