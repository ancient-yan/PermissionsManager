package com.gwchina.parent.main.presentation.home

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.adapter.recycler.SimpleItemViewBinder
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.visibleOrGone
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.presentation.home.card.*
import com.gwchina.parent.main.presentation.home.card.ContentItemViewBinder
import com.gwchina.parent.main.presentation.home.card.DayItemViewBinder
import com.gwchina.parent.main.presentation.home.card.HalfDayItemViewBinder
import com.gwchina.parent.main.presentation.home.card.TrajectoryItemDecoration
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import kotlinx.android.synthetic.main.home_using_record_fragment.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-01 10:27
 */
class HomeUsingRecordFragment : InjectorBaseListFragment<Any>() {

    private val viewModel by lazy {
        getViewModel<HomeViewModel>(viewModelFactory)
    }

    private val trajectoryAdapter by lazy {
        MultiTypeAdapter(context)
    }

    override fun provideLayout(): Any? {
        return R.layout.home_using_record_fragment
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        viewModel.deviceFlag.observe(this, Observer {
            tvDeviceName.visibleOrGone(!it.isNullOrEmpty())
            tvDeviceName.text = it
        })
        setupList()
        observableData()
    }

    private fun setupList() {
        trajectoryAdapter.register(UsingTrajectoryItem::class.java)
                .to(
                        DayItemViewBinder(false),
                        HalfDayItemViewBinder(),
                        ContentItemViewBinder(),
                        FooterViewBinder(),
                        EmptyItemViewBinder()
                ).withLinker { _, t ->
                    when (t.type) {
                        UsingTrajectoryItem.TYPE_DAY -> 0
                        UsingTrajectoryItem.TYPE_HALF_DAY -> 1
                        UsingTrajectoryItem.TYPE_ITEM -> 2
                        UsingTrajectoryItem.TYPE_FOOTER->3
                        UsingTrajectoryItem.TYPE_EMPTY->4
                        else -> throw IllegalStateException("unsupported type")
                    }
                }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(TrajectoryItemDecoration(requireContext(), trajectoryAdapter))
        setDataManager(trajectoryAdapter)
        recyclerView.adapter = trajectoryAdapter
        autoRefresh()
    }

    override fun onRefresh() {
        super.onRefresh()
        viewModel.loadUsingRecordData()
    }

    private fun observableData() {
        viewModel.loadUsingRecordData()
        viewModel.usingRecordData.observe(this, Observer {
            it?.onLoading {
                showLoadingIfEmpty()
            }?.onSuccess { data ->
                val result = data?.toMutableList()
                result?.add(UsingTrajectoryItem(type = 0))
                processListResultWithStatus(result)
            }?.onError { err ->
                processErrorWithStatus(err)
            }
        })
    }

    internal class FooterViewBinder : SimpleItemViewBinder<UsingTrajectoryItem>() {

        override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = AppCompatTextView(parent.context).apply {
            setTextColor(colorFromId(R.color.gray_level2))
            textSize = 12F
        }

        override fun onBindViewHolder(holder: KtViewHolder, item: UsingTrajectoryItem) {
            (holder.itemView as TextView).text = "哎唷，已经到底啦~"
        }

    }
}