package com.gwchina.parent.apps.presentation.approval

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.fragment.BaseStateFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processListErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.kotlin.dip
import com.android.base.rx.bindLifecycle
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.common.AppGuardResource
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import kotlinx.android.synthetic.main.apps_fragment_approval_record.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-05-08 16:55
 */
class AppApprovalRecordFragment : InjectorBaseListFragment<AppRecordWrapper>() {

    @Inject lateinit var appGuardResource: AppGuardResource

    private val appApprovalRecordAdapter by lazy {
        AppApprovalRecordAdapter(this)
    }

    private val viewModel by lazy {
        getViewModel<AppApprovalRecordViewModel>(viewModelFactory)
    }

    override fun provideLayout() = R.layout.apps_fragment_approval_record

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        //empty config
        stateLayoutConfig.setStateAction(BaseStateFragment.EMPTY, "")
                .setStateMessage(BaseStateFragment.EMPTY, getString(R.string.no_approval_records_tips))

        //list
        rvAppsRecords.layoutManager = LinearLayoutManager(context)
        rvAppsRecords.addItemDecoration(MarginDecoration(0, 0, 0, dip(20)))
        setDataManager(appApprovalRecordAdapter)
        rvAppsRecords.adapter = setupLoadMore(appApprovalRecordAdapter)

        //fetch data
        autoRefresh()
    }

    override fun onStartLoad() {
        viewModel.loadApprovalList(if (isRefreshing) 0 else pager.itemCount)
                .doOnSubscribe { showLoadingIfEmpty() }
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe(
                        {
                            processListResultWithStatus(it)
                        },
                        {
                            processListErrorWithStatus(it)
                        }
                )
    }

}