package com.gwchina.parent.apps.presentation.group

import android.arch.lifecycle.Observer
import android.arch.lifecycle.Transformations.map
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.*
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.common.*
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.data.AppGroup
import com.gwchina.parent.apps.presentation.rules.AppRulesViewModel
import com.gwchina.parent.apps.widget.showSelectAppsDialog
import com.gwchina.parent.times.common.NOT_SET_ENABLE_TIME
import com.gwchina.parent.times.common.TimeMapper
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.TimePeriod
import com.gwchina.sdk.base.utils.textStr
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.apps_fragment_add_edit_group.*
import javax.inject.Inject

/**
 * App分组编辑与添加界面
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-25 11:15
 */
class AddEditGroupFragment : InjectorBaseFragment() {

    companion object {

        internal const val GROUP_SPAN_COUNT = 5
        private const val APP_GROUP_KEY = "app_group_key"

        fun newInstance(group: AppGroup?): AddEditGroupFragment {
            return AddEditGroupFragment().apply {
                group.ifNonNull {
                    arguments = Bundle().apply {
                        putParcelable(APP_GROUP_KEY, group)
                    }
                }
            }
        }
    }

    @Inject
    lateinit var timeMapper: TimeMapper
    @Inject
    lateinit var appEventCenter: AppEventCenter
    @Inject
    lateinit var appGuardResource: AppGuardResource

    private val appRulesViewModel by lazy {
        getViewModelFromActivity<AppRulesViewModel>(viewModelFactory)
    }

    private lateinit var allSelectableAppList: MutableList<App>
    private lateinit var appGroup: AppGroup

    private val groupAdapter by lazy {
        GroupAppsAdapter(this)
    }

    private var usableDurationEnable: Boolean
        get() = clAppsAppUsableDuration?.visibility == View.VISIBLE
        set(value) {
            clAppsAppUsableDuration?.visibleOrGone(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appGroup = with(arguments?.getParcelable<AppGroup>(APP_GROUP_KEY)) {
            this?.copy() ?: AppGroup(group_used_time_perday = NOT_SET_ENABLE_TIME)
        }
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.apps_fragment_add_edit_group

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        /*应用列表*/
        setupAppList()

        /*设置可用时段和可用时长*/
        setupUsableTime()

        //如果是编辑群
        fillDataIfEditingGroup()

        //分组名
        cetAppsGroupName.editText.textWatcher {
            afterTextChanged { checkCompleteEnable() }
        }

        //退出检查
        gtlAppsSetFlowTitle.setOnNavigationOnClickListener {
            activity?.onBackPressed()
        }

        //完成
        btnAppsAddGroupComplete.setOnClickListener {
            addGroup()
        }

        usableDurationEnable = appGuardResource.isUsableDurationEnable
    }

    private fun setupAppList() {
        rvAppsGroupAppContent.layoutManager = GridLayoutManager(context, GROUP_SPAN_COUNT)
        rvAppsGroupAppContent.addItemDecoration(MarginDecoration(dip(10), 0, dip(10), dip(15)))
        with(groupAdapter) {
            onAddAppListener = { selectApps() }
            onAddDeleteListener = { checkCompleteEnable() }
            rvAppsGroupAppContent.adapter = this
        }
    }

    private fun setupUsableTime() {
        //设置可用时长
        clAppsAppUsableDuration.setOnClickListener {
            selectGuardDuration()
        }
        tsvAppsTimeTable.onTimePeriodChangedListener = {
            checkCompleteEnable()
        }
    }

    private fun selectGuardDuration() {
        showSelectAppGuardDurationDialog(requireContext(), appGroup.group_used_time_perday, appGuardResource.hasTimeGuard) {
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_GROUP_BTN_CHANGESHICHANG)
            appGroup.group_used_time_perday = it
            tvAppsAppUsableDurationValue.setTextColor(colorFromId(R.color.gray_level1))
            tvAppsAppUsableDurationValue.text = generateDetailTimePerDayTextFromSecond(appGroup.group_used_time_perday)
            checkCompleteEnable()
        }
    }

    private fun fillDataIfEditingGroup() {
        if (appGroup.soft_group_id != null) {
            //名称
            cetAppsGroupName.editText.setText(appGroup.soft_group_name)
            //时长
            tvAppsAppUsableDurationValue.setTextColor(colorFromId(R.color.gray_level1))
            tvAppsAppUsableDurationValue.text = generateDetailTimePerDayTextFromSecond(appGroup.group_used_time_perday)
            //时段
            tsvAppsTimeTable.setData(timeMapper.toTimePeriods(appGroup.group_fragment))
            //列表
            groupAdapter.replaceAll(appGroup.soft_list)
            //删除
            gtlAppsSetFlowTitle.menu.add(R.string.delete)
                    .setIcon(R.drawable.icon_delete_black)
                    .alwaysShow()
                    .onMenuItemClick {
                        showConfirmDialog {
                            message = getString(R.string.confirm_delete_the_group_mask, appGroup.soft_group_name)
                            positiveListener = {
                                StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_GROUP_BTN_DETELEGROUP)
                                deleteGroup()
                            }
                        }
                    }
        }
    }

    private fun addGroup() {
        appGroup.soft_list = groupAdapter.items
        appGroup.soft_group_name = cetAppsGroupName.editText.textStr()
        val result = tsvAppsTimeTable.getData()
        removeTimePeriodWithType(result)
        appGroup.group_fragment = timeMapper.toTimeParts(result)

        val checkedUsableTime = checkUsableDurationWhenSettingAppGroup(appGroup.group_used_time_perday, result, usableDurationEnable)

        appGroup.group_used_time_perday = checkedUsableTime

        if (appGroup.soft_group_id == null) {
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_BTN_SADDGROUP_FINISH)
            appRulesViewModel.addAppGroup(appGroup)
        } else {
            appRulesViewModel.updateAppGroup(appGroup)
        }
                .observe(this, Observer {
                    it?.onSuccess {
                        appEventCenter.notifyAppListNeedRefresh()
                        exitFragment()
                    }?.onError { error ->
                        dismissLoadingDialog()
                        errorHandler.handleError(error)
                    }?.onLoading {
                        showLoadingDialog()
                    }
                })
    }

    private fun deleteGroup() {
        appRulesViewModel.deleteAppGroup(appGroup)
                .observe(this, Observer {
                    it?.onSuccess {
                        appEventCenter.notifyAppListNeedRefresh()
                        exitFragment()
                    }?.onError { error ->
                        dismissLoadingDialog()
                        errorHandler.handleError(error)
                    }?.onLoading {
                        showLoadingDialog()
                    }
                })
    }

    private fun selectApps() {
        if (!::allSelectableAppList.isInitialized) {
            return
        }

        if (allSelectableAppList.isEmpty()) {
            showMessage(R.string.no_more_app_tips)
            return
        }

        val selectedItems = groupAdapter.items

        val selectable = if (selectedItems.isNullOrEmpty()) {
            arrayListOf<App>().apply { addAll(allSelectableAppList) }
        } else {
            allSelectableAppList.filter {
                !selectedItems.contains(it)
            }
        }

        if (selectable.isEmpty()) {
            showMessage(R.string.no_more_app_tips)
            return
        }

        showSelectAppsDialog(selectable, onSelectedAppList = {
            groupAdapter.addItems(it)
            checkCompleteEnable()
        })
    }

    private fun checkCompleteEnable() {
        btnAppsAddGroupComplete.isEnabled = cetAppsGroupName.editText.textStr().isNotEmpty()
                && groupAdapter.dataSize > 0
                && (((usableDurationEnable && appGroup.group_used_time_perday != NOT_SET_ENABLE_TIME) || tsvAppsTimeTable.getData().isNotEmpty()))
    }

    override fun handleBackPress(): Boolean {
        if (btnAppsAddGroupComplete.isEnabled) {
            showConfirmDialog {
                messageId = R.string.edit_exit_tips
                positiveListener = {
                    exitFragment()
                }
            }
            return true
        }
        return false
    }

    private fun subscribeViewModel() {
        map(appRulesViewModel.limitedList) {
            //排序分为两大类：未设置时长（先）、已设置时长（后），大类内排序=原列表排序；
            it.first.toMutableList().sortedBy { app -> -app.used_time_perday }
        }.observe(this, Observer {
            allSelectableAppList = it?.toMutableList() ?: ArrayList(0)
        })
    }


    /**
     * 移除掉TimePeriod type为1的
     */
    private fun removeTimePeriodWithType(plan: MutableList<TimePeriod>) {
        plan.remove(plan.find { timePeriod -> timePeriod.type == 1 })
    }
}