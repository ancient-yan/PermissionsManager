package com.gwchina.parent.daily

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
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.daily.adapter.MessageListAdapter
import com.gwchina.parent.daily.data.DailyMessageListBean
import com.gwchina.parent.daily.presentation.DailyViewModel
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import kotlinx.android.synthetic.main.daily_message_list_layout.*
import javax.inject.Inject

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-10 09:34
 */
class DailyMessageListFragment : InjectorBaseListFragment<DailyMessageListBean>() {
    @Inject
    lateinit var dailyNavigator: DailyNavigator

    private val adapter: MessageListAdapter by lazy {
        MessageListAdapter(this, viewModel.getUserId()) { position, dataList ->
            dailyNavigator.openPicPrePage(position, dataList)
        }
    }

    private val viewModel by lazy {
        getViewModel<DailyViewModel>(viewModelFactory)
    }

    override fun provideLayout(): Any? {
        return R.layout.daily_message_list_layout
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        gwTitleLayout.setOnNavigationOnClickListener { activity?.onBackPressed() }
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        setDataManager(adapter)
        recyclerView.adapter = setupLoadMore(adapter)
        stateLayoutConfig.setStateMessage(StateLayoutConfig.EMPTY, getString(R.string.no_daily_message))
        autoRefresh()
    }

    override fun onStartLoad() {
        super.onStartLoad()
        viewModel.getMessageList(if (isRefreshing) 0 else pager.itemCount)
                .doOnSubscribe {
                    showLoadingIfEmpty()
                }
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    processListResultWithStatus(it)
                }, {
                    processListErrorWithStatus(it)
                })

    }

}