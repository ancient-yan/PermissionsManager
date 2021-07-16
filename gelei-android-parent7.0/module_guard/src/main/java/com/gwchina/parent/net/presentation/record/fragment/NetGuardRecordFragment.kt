package com.gwchina.parent.net.presentation.record.fragment

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.StateLayoutConfig
import com.android.base.app.ui.processListErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.rx.bindLifecycle
import com.android.base.utils.android.compat.SystemBarCompat
import com.blankj.utilcode.util.ColorUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.net.NetGuardNavigator
import com.gwchina.parent.net.data.model.GuardRecord
import com.gwchina.parent.net.presentation.record.adapter.NetGuardRecordAdapter
import com.gwchina.parent.net.presentation.record.viewmodel.NetGuardRecordViewModel
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import kotlinx.android.synthetic.main.net_fragment_record.*
import javax.inject.Inject

class NetGuardRecordFragment : InjectorBaseListFragment<GuardRecord>() {

    @Inject
    lateinit var netGuardNavigator: NetGuardNavigator

    private val viewModel by lazy { getViewModel<NetGuardRecordViewModel>(viewModelFactory) }

    private val adapter: NetGuardRecordAdapter by lazy { NetGuardRecordAdapter(this) }

    override fun provideLayout(): Any? = R.layout.net_fragment_record

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)

        rvNetGuardRecordList.layoutManager = LinearLayoutManager(context)
        adapter.urlClickListener = {
            netGuardNavigator.openInterceptUrl(it)
        }
        setDataManager(adapter)
        rvNetGuardRecordList.adapter = setupLoadMore(adapter)
        loadMoreController.setAutoHiddenWhenNoMore(true)
        stateLayoutConfig.setStateMessage(StateLayoutConfig.EMPTY, getString(R.string.no_intercept_tips_2))

        autoRefresh()

    }

    override fun onResume() {
        super.onResume()
        SystemBarCompat.setStatusBarColor(activity, ColorUtils.getColor(R.color.white))
    }

    override fun onStartLoad() {
        viewModel.getGuardRecordList(if (isRefreshing) 0 else pager.itemCount)
                .doOnSubscribe { showLoadingIfEmpty() }
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    processListResultWithStatus(it)
                }, {
                    processListErrorWithStatus(it)
                })
    }

}