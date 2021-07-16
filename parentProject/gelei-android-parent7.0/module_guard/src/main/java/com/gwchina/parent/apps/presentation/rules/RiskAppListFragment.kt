package com.gwchina.parent.apps.presentation.rules

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.mvvm.getViewModelFromActivity
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardNavigator
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.apps_fragment_app_risk.*
import me.drakeet.multitype.register
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-05 11:28
 */
class RiskAppListFragment : InjectorBaseListFragment<Any>() {

    @Inject
    lateinit var appGuardNavigator: AppGuardNavigator

    internal val appRulesViewModel by lazy {
        getViewModelFromActivity<AppRulesViewModel>(viewModelFactory)
    }

    private val listAdapter by lazy {
        MultiTypeAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.apps_fragment_app_risk

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        rvAppsRiskList.layoutManager = LinearLayoutManager(requireContext())
        setDataManager(listAdapter)
        val appHeaderItemBinders = AppHeaderItemBinders(this)
        listAdapter.register(appHeaderItemBinders)

        val riskAppItemViewBinder = RiskAppItemViewBinder(this)
        listAdapter.register(riskAppItemViewBinder)
        riskAppItemViewBinder.onAllowListener = { app ->
            //可用时段
            if (app.soft_fragments == null) {
                app.soft_fragments = emptyList()
            }
            showConfirmDialog {
                messageId = R.string.guard_allow_risk_message
                positiveListener = {
                    appGuardNavigator.openAppPermissionSettingPage(app, isForbid = appRulesViewModel.isRestricted())
                }
            }?.setCanceledOnTouchOutside(false)

        }
        listAdapter.register(EmptyRiskAppItemViewBinder())
        rvAppsRiskList.adapter = listAdapter
    }


    override fun onRefresh() {
        super.onRefresh()
        appRulesViewModel.refreshList()
    }

    private fun subscribeViewModel() {
        val headerData = HeaderData(showWhatIsGroup = false, showAddGroupLayout = false)
        appRulesViewModel.riskList.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                headerData.showAppsRiskAppTips = false
                listAdapter.replaceAll(listOf(""))
            } else {
                headerData.showAppsRiskAppTips = true
                listAdapter.replaceAll(it)
            }
            listAdapter.addAt(0, headerData)
            refreshCompleted()
        })
    }

}