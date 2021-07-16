package com.gwchina.parent.apps.presentation.rules

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.utils.android.UnitConverter
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardNavigator
import com.gwchina.parent.apps.data.App
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import javax.inject.Inject

/**
 * 自由可用和禁止使用公共行为抽象
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-28 10:51
 */
abstract class BaseAppListFragment : InjectorBaseStateFragment() {

    @Inject
    lateinit var appGuardNavigator: AppGuardNavigator

    internal val appRulesViewModel by lazy {
        getViewModelFromActivity<AppRulesViewModel>(viewModelFactory)
    }

    private lateinit var recyclerView: RecyclerView
    lateinit var appListAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.app_base_refresh_list_fragment

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.base_list_layout)
//        recyclerView.setPaddingTop(dip(10))

        appListAdapter = AppListAdapter(this, provideRuleTypeInfo())

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(MarginDecoration(0, 0, 0, UnitConverter.dpToPx(5)))
            adapter = appListAdapter
        }

        with(appListAdapter) {
            onAddAppListener = {
                onAddApp()
            }
            onAppClickedListener = {
                onAppClicked(it)
            }
        }
    }

    override fun onRefresh() {
        super.onRefresh()
        appRulesViewModel.refreshList()
    }

    open fun subscribeViewModel() {
        selectLiveData(appRulesViewModel).observe(this, Observer {
            appListAdapter.canAddMore = hasElseAppCanBeSet()
            appListAdapter.replaceAll(it)
            refreshCompleted()
        })
    }

    open fun onAppClicked(app: App) {
        appGuardNavigator.openAppPermissionSettingPage(app)
    }

    protected fun processOperationResult(updateAppRuleType: LiveData<Resource<Any>>) {
        updateAppRuleType.observe(this, Observer {
            it?.onLoading {
                showLoadingDialog()
            }?.onSuccess {
                dismissLoadingDialog()
                autoRefresh()
            }?.onError { err ->
                dismissLoadingDialog()
                errorHandler.handleError(err)
            }
        })
    }

    protected abstract fun hasElseAppCanBeSet(): Boolean
    protected abstract fun onAddApp()
    protected abstract fun provideRuleTypeInfo(): RuleTypeInfo
    protected abstract fun selectLiveData(appRulesViewModel: AppRulesViewModel): LiveData<List<App>>

}

data class RuleTypeInfo(
        val typeName: CharSequence,
        val emptyAppsText: CharSequence,
        val emptyAppsIcon: Int
)