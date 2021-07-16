package com.gwchina.parent.recommend.presentation

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.recommend.Navigator
import com.gwchina.parent.recommend.data.SoftItem
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import javax.inject.Inject


/**
 * 应用推荐列表。
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 11:16
 */
class RecommendListFragment : InjectorBaseStateFragment() {

    companion object {
        internal const val LIST_ID_KEY = "list_id_key"
    }

    @Inject lateinit var navigator: Navigator

    private lateinit var viewModel: RecommendViewModel
    private lateinit var adapter: RecommendListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModelFromActivity(viewModelFactory)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.app_base_refresh_list_fragment

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.base_list_layout)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = initAdapter()
        }
    }

    private fun initAdapter(): RecyclerView.Adapter<*>? {
        adapter = RecommendListAdapter(this@RecommendListFragment)
        //when click installing
        adapter.onInstallListener = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.COMMEND_BTN_APPINSTALL)
            showInstallingTips {
                doInstallForChild(it)
            }
        }
        //when click item
        adapter.onItemClickListener = {
            navigator.showAppDetail(it)
        }
        return adapter
    }

    private fun doInstallForChild(softItem: SoftItem) {
        showLoadingDialog(false)
        viewModel.installForChild(softItem)
                .doOnTerminate { dismissLoadingDialog() }
                .subscribe(
                        { /*no op*/ },
                        {
                            errorHandler.handleError(it)
                        }
                )
    }

    private fun subscribeViewModel() {
        val listId = arguments?.getString(LIST_ID_KEY) ?: ""

        viewModel.categoryList(listId)
                .observe(this, Observer {
                    val items = adapter.items
                    if (items != it) {
                        adapter.setDataSource(it, true)
                    }
                    refreshCompleted()
                })

        viewModel.installEvent
                .observe(this, Observer {
                    adapter.items?.indexOfFirst { item ->
                        it == item.bundle_id
                    }?.let {
                        adapter.notifyItemChanged(it)
                    }
                })
    }

    override fun onRefresh() {
        viewModel.refreshList()
    }

}