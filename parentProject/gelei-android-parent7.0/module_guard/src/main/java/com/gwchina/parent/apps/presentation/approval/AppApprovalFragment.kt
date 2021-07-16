package com.gwchina.parent.apps.presentation.approval

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.android.base.app.dagger.Injectable
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.visibleOrGone
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.common.*
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.apps.widget.AppGuardMemberDialog
import com.gwchina.parent.times.common.NOT_SET_ENABLE_TIME
import com.gwchina.parent.times.common.TimeMapper
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.TimePeriod
import com.gwchina.sdk.base.utils.displayAppIcon
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.apps_fragment_app_approval.*
import javax.inject.Inject

/**
 * App 审批设置页
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-29 16:49
 */
class AppApprovalFragment : InjectorBaseFragment(), Injectable {

    companion object {

        private const val IS_APPROVAL_KEY = "is_approval_key"
        private const val IS_FORBID_KEY = "is_forbid_key"
        private const val APP_KEY = "app_key"

        fun newInstance(app: App, forAppApproval: Boolean = false, isForbid: Boolean = false): AppApprovalFragment {
            return AppApprovalFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(APP_KEY, app)
                    putBoolean(IS_APPROVAL_KEY, forAppApproval)
                    putBoolean(IS_FORBID_KEY, isForbid)
                }
            }
        }

    }

    @Inject
    lateinit var appEventCenter: AppEventCenter
    @Inject
    lateinit var timeMapper: TimeMapper
    @Inject
    lateinit var appGuardResource: AppGuardResource

    private val appApprovalViewModel by lazy {
        getViewModel<AppApprovalViewModel>(viewModelFactory)
    }

    private lateinit var app: App

    private var forAppApproval = false
    //是否禁止添加限时可用/禁止可用
    private var isForbid = false
    private var selectedUsableDurationSecond = 0
    private var mIsRiskType: Boolean = false
    private lateinit var selectionAdapter: RuleTypeAdapter

    private var usableDurationEnable: Boolean
        get() = clAppsAppUsableDuration?.visibility == View.VISIBLE
        set(value) {
            clAppsAppUsableDuration?.visibleOrGone(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            //it will change app's properties in process, so we need to copy out a backup.
            app = with(getParcelable<App>(APP_KEY)) {
                this?.copy() ?: throw NullPointerException("the app is null!")
            }
            forAppApproval = getBoolean(IS_APPROVAL_KEY)
            isForbid = getBoolean(IS_FORBID_KEY)
            selectedUsableDurationSecond = app.used_time_perday
        }

        //if the rule type is high risk, set it to free type by default in the process of approving.
        if (app.rule_type.isHighRisk()) {
            mIsRiskType = true
            app.rule_type = RULE_TYPE_LIMITED
        }

        if (!app.rule_type.isLimitedUsable()) {
            selectedUsableDurationSecond = NOT_SET_ENABLE_TIME
            app.used_time_perday = selectedUsableDurationSecond
        }
    }

    override fun provideLayout() = R.layout.apps_fragment_app_approval

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        //title
        gtlAppsRulesTitle.setOnNavigationOnClickListener {
            activity?.onBackPressed()
        }

        //app properties
        ImageLoaderFactory.getImageLoader().displayAppIcon(this, ivAppsAppIcon, app.soft_icon)
        ivAppsAppName.text = app.soft_name

        //type selection
        with(RuleTypeAdapter(requireContext(), buildSelections(), app.rule_type)) {
            onRuleTypeChanged = { processOnRuleTypeChanged(it) }
            selectionAdapter = this
            rvAppsRuleTypeSelection.adapter = this
        }

        //usable duration
        if (app.rule_type.isLimitedUsable() && !mIsRiskType) {
            tsvAppsTimeTable.setData(timeMapper.toTimePeriods(app.soft_fragments))
            setUsableDurationText()
        }
        processOnRuleTypeChanged(app.rule_type)
        clAppsAppUsableDuration.setOnClickListener {
            selectGuardDuration()
        }

        //complete btn
        if (forAppApproval) {
            btnAppsPermissionComplete.isEnabled = false
        }
        btnAppsPermissionComplete.setOnClickListener {
            val selectedRuleType = selectionAdapter.selectedRuleType
            if(isForbid && (selectedRuleType.isDisabled() || selectedRuleType.isLimitedUsable())) {
                AppGuardMemberDialog.showTips(requireContext(), getString(R.string.app_guard_limit_tips2, appApprovalViewModel.maxCount), R.string.i_got_it)
            } else if (isSame() && !mIsRiskType) {
                exitFragment()
            } else {
                doUpdateDataAppRule()
            }
        }

        usableDurationEnable = appGuardResource.isUsableDurationEnable
    }

    private fun setUsableDurationText() {
        tvAppsAppUsableDurationValue.setTextColor(colorFromId(R.color.gray_level1))
        tvAppsAppUsableDurationValue.text = generateDetailTimePerDayTextFromSecond(selectedUsableDurationSecond)
    }

    private fun selectGuardDuration() {
        showSelectAppGuardDurationDialog(requireContext(), selectedUsableDurationSecond, appGuardResource.hasTimeGuard) {
            StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_EDITOR_BTN_CHANGESHICHANG)
            selectedUsableDurationSecond = it
            setUsableDurationText()
        }
    }

    private fun isSame(): Boolean {
        if (app.rule_type != selectionAdapter.selectedRuleType) {
            return false
        } else if (app.rule_type == RULE_TYPE_LIMITED) {

            if (app.used_time_perday != selectedUsableDurationSecond) {
                return false
            }

            val softFragments = app.soft_fragments
            val selectedPeriodList = tsvAppsTimeTable.getData()
            removeTimePeriodWithType(selectedPeriodList)
            if (softFragments.isNullOrEmpty() && selectedPeriodList.isNotEmpty()) {
                return false
            }

            return selectedPeriodList == timeMapper.toTimePeriods(softFragments)

        }
        return true
    }

    private fun buildSelections(): List<Selection> {
        return mutableListOf<Selection>().apply {
            if (!forAppApproval && !mIsRiskType) {
                add(Selection(R.drawable.app_icon_type_disable, appGuardResource.getRuleTypeName(RULE_TYPE_DISABLE), RULE_TYPE_DISABLE))
            }
            add(Selection(R.drawable.app_icon_type_free, appGuardResource.getRuleTypeName(RULE_TYPE_FREE), RULE_TYPE_FREE))
            add(Selection(R.drawable.app_icon_type_limited, appGuardResource.getRuleTypeName(RULE_TYPE_LIMITED), RULE_TYPE_LIMITED))
        }
    }

    private fun processOnRuleTypeChanged(ruleType: Int) {
        if (forAppApproval && (ruleType.isLimitedUsable() || ruleType.isFreeUsable())) {
            btnAppsPermissionComplete.isEnabled = true
        }
        llAppsTimeSetting.visibleOrGone(ruleType.isLimitedUsable())
    }

    private fun doUpdateDataAppRule() {
        val selectedRuleType = selectionAdapter.selectedRuleType
        //友盟埋点统计
        when {
            selectedRuleType.isDisabled() -> StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_EDITOR_BTN_PROHIBIT)
            selectedRuleType.isFreeUsable() -> StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_EDITOR_BTN_FREE)
            selectedRuleType.isLimitedUsable() -> StatisticalManager.onEvent(UMEvent.ClickEvent.SOFTVIEW_EDITOR_BTN_TIMELIMIT)
        }
        val result = tsvAppsTimeTable.getData()
        removeTimePeriodWithType(result)
        val (usableDuration, timeParts) = if (selectedRuleType == RULE_TYPE_LIMITED) {
            if (mIsRiskType&&tvAppsAppUsableDurationValue.text.toString()=="未设置"){
                //time单位为秒
                Pair(24*60*60, timeMapper.toTimeParts(result))
            }else{
                Pair(selectedUsableDurationSecond, timeMapper.toTimeParts(result))
            }
        } else {
            Pair(0, emptyList())
        }
        val duration = checkUsableDurationWhenApprovalApp(usableDuration, result, usableDurationEnable).let {
            if (it == NOT_SET_ENABLE_TIME) {
                ""
            } else {
                it.toString()
            }
        }

        appApprovalViewModel.updateAppRule(app.rule_id, selectedRuleType.toString(), duration, timeParts, forAppApproval)
                .observe(this, Observer { resource ->
                    resource?.onSuccess {
                        dismissLoadingDialog()
                        notifyUpdateSuccessAndExit()
                    }?.onLoading {
                        showLoadingDialog()
                    }?.onError {
                        dismissLoadingDialog()
                        errorHandler.handleError(it)
                    }
                })
    }


    /**
     * 移除掉TimePeriod type为1的
     */
    private fun removeTimePeriodWithType(plan: MutableList<TimePeriod>) {
        plan.remove(plan.find { timePeriod -> timePeriod.type == 1 })
    }

    private fun notifyUpdateSuccessAndExit() {
        val selectedRuleType = selectionAdapter.selectedRuleType

        //show tips
        if (forAppApproval) {
            showMessage(getString(R.string.x_add_to_y_list_mask, app.soft_name, appGuardResource.getRuleTypeName(selectedRuleType)))
        } else {
            if (app.rule_type == selectionAdapter.selectedRuleType && selectionAdapter.selectedRuleType == RULE_TYPE_LIMITED) {
                showMessage(R.string.modify_success)
            } else {
                showMessage(getString(R.string.already_transform_to_mask, appGuardResource.getRuleTypeName(selectedRuleType)))
            }
        }

        //notify other page refresh
        appEventCenter.notifyAppHasBeApproved(app.copy(rule_type = selectedRuleType))
        appEventCenter.notifyAppListNeedRefresh()

        //exit
        exitFragment()
    }

    override fun handleBackPress(): Boolean {
        if (!isSame()) {
            askSaveEdits()
            return true
        }
        return false
    }

    private fun askSaveEdits() {
        showConfirmDialog {
            messageId = R.string.edit_exit_tips
            positiveListener = { exitFragment() }
        }
    }

}