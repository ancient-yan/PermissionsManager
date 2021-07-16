package com.gwchina.parent.apps.presentation.rules

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.Resource
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.colorFromId
import com.android.base.rx.computation2UI
import com.android.base.rx.subscribeIgnoreError
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.AppGuardNavigator
import com.gwchina.parent.apps.common.*
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.data.AppGroup
import com.gwchina.parent.apps.widget.AppGuardMemberDialog
import com.gwchina.parent.apps.widget.showSelectAppsDialog
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import io.reactivex.Flowable
import me.drakeet.multitype.register
import javax.inject.Inject

/**
 * 限时可用列表
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-27 15:05
 */
class LimitedAppListFragment : InjectorBaseStateFragment() {

    @Inject
    lateinit var appGuardNavigator: AppGuardNavigator
    @Inject
    lateinit var appGuardResource: AppGuardResource

    internal val appRulesViewModel by lazy {
        getViewModelFromActivity<AppRulesViewModel>(viewModelFactory)
    }

    private lateinit var adapter: MultiTypeAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.apps_fragment_rule_limited_list

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        setupList(view)
    }

    private fun setupList(view: View) {
        recyclerView = view.findViewById(R.id.base_list_layout)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = MultiTypeAdapter(context)
        recyclerView.adapter = adapter

        //头部分
        val appHeaderItemBinders = AppHeaderItemBinders(this)
        appHeaderItemBinders.appsItemAddGroup = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_BTN_ADDGROUP)
            addGroup()
        }
        appHeaderItemBinders.appsItemWhatIsGroup = {
            appGuardResource.showAppGroupDescDialog(requireContext())
        }
        adapter.register(appHeaderItemBinders)

        val limitedAddGroupItemBinder = LimitedAddGroupItemBinder(this) {
            addGroup()
        }

        val limitedAddAppItemBinder = LimitedAddAppItemBinder(this) {
            addLimitedApp()
        }

        adapter.register(LimitedTypeHeader::class)
                .to(limitedAddGroupItemBinder, limitedAddAppItemBinder)
                .withClassLinker { _, data ->
                    if (data.type == LIMITED_ADD_APP_ITEM_BINDER_TYPE)
                        LimitedAddAppItemBinder::class.java
                    else
                        LimitedAddGroupItemBinder::class.java
                }
        adapter.register(LimitedGroupItemBinder(this) {
            checkGroupDetail(it)
        })
        adapter.register(LimitedAppItemBinder(this) {
            checkAppDetail(it)
        })
    }

    private fun addLimitedApp() {
        val appListCanBeLimited = appRulesViewModel.appListCanBeLimited()
        if (appListCanBeLimited.isEmpty()) {
            showMessage(getString(R.string.no_more_apps_can_be_set_tips_mask, getString(R.string.limited_usable)))
            return
        }
        if (!hasElseAppCanBeSet()) {
            AppGuardMemberDialog.showTips(requireContext(), getString(R.string.app_guard_limit_tips2, appRulesViewModel.maxCount), R.string.i_got_it)
            return
        }
        showSelectAppsDialog(appListCanBeLimited, getString(R.string.add_as_x_mask, getString(R.string.limited_usable)),
                onSelectedAppList = {
                    StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_TIMELIMIT_BTN_ADD)
                    if (it.isNotEmpty()) {
                        processOperationResult(appRulesViewModel.updateAppRuleType(it, RULE_TYPE_LIMITED))
                    }
                },
                onSelectionListener = { app: App, selectMap: HashMap<String, App> ->
                    val allow = appRulesViewModel.isCanAddApp(app, selectMap)
                    if (allow) {
                        showMessage(getString(R.string.app_guard_limit_tips4, appRulesViewModel.maxCount))
                    }
                    allow
                })
    }

    private fun processOperationResult(updateAppRuleType: LiveData<Resource<Any>>) {
        updateAppRuleType.observe(this, Observer {
            it?.onLoading {
                showLoadingDialog()
            }?.onSuccess {
                autoRefresh()
                dismissLoadingDialog()
            }?.onError { err ->
                errorHandler.handleError(err)
                dismissLoadingDialog()
            }
        })
    }

    private fun addGroup() {
        if (adapter.items.any { it is AppWrapper }) {
            appGuardNavigator.openAddEditAppGroupPage()
        } else {
            showMessage(getString(R.string.all_app_grouped_tips))
        }
    }

    private fun checkGroupDetail(groupWrapper: GroupWrapper) {
        appGuardNavigator.openAddEditAppGroupPage(groupWrapper.appGroup)
    }

    private fun checkAppDetail(app: App) {
        appGuardNavigator.openAppPermissionSettingPage(app)
    }

    override fun onRefresh() {
        super.onRefresh()
        appRulesViewModel.refreshList()
    }

    private fun subscribeViewModel() {
        appRulesViewModel.limitedList
                .observe(this, Observer {
                    it?.let(::showList)
                })
    }

    private fun hasElseAppCanBeSet(): Boolean {
        return if (appRulesViewModel.isRestricted()) {
            false
        } else {
            appRulesViewModel.hasFreeAppWithoutSystemApp || appRulesViewModel.hasDisabledApp
        }
    }

    private fun showList(data: Pair<List<App>, List<AppGroup>>) {

//        tvAppsItemWhatIsGroup?.visibleOrInvisible(!appRulesViewModel.hasAddedGroup)
//
//        if (data.first.isEmpty() || data.second.isNotEmpty()) {
//            llAppsAddGroup.gone()
//        } else if (data.second.isEmpty()) {
//            llAppsAddGroup.visible()
//        }

        Flowable.just(data)
                .map { compostData(data) }
                .computation2UI()
                .autoDispose(Lifecycle.Event.ON_DESTROY)
                .subscribeIgnoreError {
                    adapter.replaceAll(it)
                    refreshCompleted()
                }
    }

    private fun compostData(data: Pair<List<App>, List<AppGroup>>): MutableList<Any> {
        val list = mutableListOf<Any>()

        list.add(HeaderData(!appRulesViewModel.hasAddedGroup, data.second.isEmpty()))

        val groups = data.second.map {
            GroupWrapper(it).apply {
                groupIcons = generateAppGroupIconList(appGroup.soft_list)
                groupDetails = generateAppGroupName(appGroup.soft_list)
                groupUsedTime = appGuardResource.generateAppUsedTimeTextFromSecond(appGroup.group_used_time_perday, appGroup.used_time)
            }
        }

        val dividerColor = colorFromId(R.color.gray_level5)
        val singleApps = data.first.map {
            AppWrapper(it).apply {
                appUsedTime = appGuardResource.generateAppUsedTimeTextFromSecond(app.used_time_perday, app.used_time)
                appNameWithType = SpanUtils()
                        .append(app.soft_name?:"")
                        .append("  |  ")
                        .setForegroundColor(dividerColor)
                        .append(app.checkedTypeName())
                        .setFontSize(12, true)
                        .create()
            }
        }

        if (groups.isNotEmpty()) {
            list.add(LimitedTypeHeader(LIMITED_ADD_GROUP_ITEM_BINDER_TYPE, groups.isEmpty(), singleApps.isNotEmpty()))
            list.addAll(groups)
        }

        list.add(LimitedTypeHeader(LIMITED_ADD_APP_ITEM_BINDER_TYPE, singleApps.isEmpty(), hasElseAppCanBeSet()))
        list.addAll(singleApps)

        return list
    }

}