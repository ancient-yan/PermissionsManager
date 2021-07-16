package com.gwchina.parent.times.presentation.make

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.android.base.kotlin.dip
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.times.TimeGuardNavigator
import com.gwchina.parent.times.common.TimeEventCenter
import com.gwchina.parent.times.common.TimeGuardDailyPlanVO
import com.gwchina.parent.times.widget.TimeGuardPlanView
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.models.timePlanHasBeSet
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.textStr
import com.gwchina.sdk.base.widget.SimpleDividerItemDecoration
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.times_fragment_make_plans2.*
import me.drakeet.multitype.register
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-11-28 15:28
 */
class MakeTimeGuardPlanFragment2 : InjectorBaseFragment() {

    /**
     * 保存添加的计划的数据
     */
    private val mTimeGuardPlanDataList = mutableListOf<Any?>()

    companion object {

        private const val MAKING_INFO_KEY = "making_info_key"

        fun newInstance(makingPlanInfo: MakingPlanInfo) = MakeTimeGuardPlanFragment2().apply {
            arguments = Bundle().apply {
                putParcelable(MAKING_INFO_KEY, makingPlanInfo)
            }
        }
    }

    @Inject
    lateinit var timeGuardNavigator: TimeGuardNavigator
    @Inject
    lateinit var eventCenter: TimeEventCenter

    internal lateinit var makingPlanInfo: MakingPlanInfo

    private var optionalNamesBinder: OptionalNamesBinder? = null
    private lateinit var timeGuardBinder: TimeGuardBinder
    internal lateinit var recyclerView: RecyclerView

    internal val viewModel by lazy {
        getViewModel<MakeTimeGuardPlanViewModel>(viewModelFactory)
    }

    private val adapter by lazy {
        MultiTypeAdapter(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makingPlanInfo = arguments?.getParcelable(MAKING_INFO_KEY) ?: throw NullPointerException()
        subscribeToViewModel()
    }

    override fun provideLayout() = R.layout.times_fragment_make_plans2

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        showOperationTipsIfNeed()
        setupListeners()
        recyclerView = rvTimeGuard
        timeGuardBinder = TimeGuardBinder(this)
        timeGuardBinder.isTypeSpare = makingPlanInfo.makingType == MakingPlanInfo.TYPE_SPARE
        rvTimeGuard.adapter = adapter
        if (makingPlanInfo.makingType == MakingPlanInfo.TYPE_SPARE) {
            optionalNamesBinder = OptionalNamesBinder(requireContext())
            adapter.register(optionalNamesBinder!!)
            mTimeGuardPlanDataList.add(makingPlanInfo)
            optionalNamesBinder!!.checkPlansStatus = {
                checkPlansStatus()
            }
        }
        adapter.register(timeGuardBinder)
        addFirstPlanView()

        rvTimeGuard.addItemDecoration(SimpleDividerItemDecoration(context, dip(12)))
        rvTimeGuard.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter.items = mTimeGuardPlanDataList

        timeGuardBinder.onGuardDataChangedListener = {
            checkPlansStatus()
        }

        timeGuardBinder.onUsableDurationClickListener = {
            timeGuardNavigator.openMemberCenter()
        }

        with(makingPlanInfo.notOptionalDays) {
            timeGuardBinder.notOptionalDays = this
        }

        with(makingPlanInfo.selectedDays) {
            if (!this.isNullOrEmpty()) {
                mTimeGuardPlanDataList.filterIsInstance<TimeGuardPlanData>().first().selectedGuardDays = this
            }
        }
        checkPlansStatus()
    }

    private fun addFirstPlanView() {
        val timeGuardPlanData = TimeGuardPlanData(TimeGuardDailyPlanVO(), state = TimeGuardPlanView.EXPAND)
        mTimeGuardPlanDataList.add(timeGuardPlanData)
    }

    private fun setupListeners() {
        gwlTimesMakePlan.setOnNavigationOnClickListener {
            activity?.onBackPressed()
        }

        ivTimeAddPlan.setOnClickListener {
            mTimeGuardPlanDataList.filterIsInstance<TimeGuardPlanData>().forEach {
                it.state = TimeGuardPlanView.COLLAPSE
            }
            addFirstPlanView()
            adapter.setDataSource(mTimeGuardPlanDataList, true)
            StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_START_BTN_ADD_NEW_PLAN)
            checkPlansStatus()
        }

        btnTimeComplete.setOnClickListener {
            setTimeGuardPlans()
            if (makingPlanInfo.makingType == MakingPlanInfo.TYPE_NORMAL) {
                StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_START_BTN_FINISH)
            } else {
                StatisticalManager.onEvent(UMEvent.ClickEvent.TIMEVIEW_BTN_SPAREPLAN_FINISH)
            }
        }
    }


    private fun checkPlansStatus() {
        val isAllPlansCompleted = mTimeGuardPlanDataList.filterIsInstance<TimeGuardPlanData>().all { isCompleted(it) }
        val isAnyPlansCompleted = mTimeGuardPlanDataList.filterIsInstance<TimeGuardPlanData>().any { isCompleted(it) }
        ivTimeAddPlan.isEnabled = isAllPlansCompleted && !isAllGuardDaySelected()
        btnTimeComplete.isEnabled = if (makingPlanInfo.makingType == MakingPlanInfo.TYPE_NORMAL) {
            isAnyPlansCompleted
        } else {
            isAnyPlansCompleted && optionalNamesBinder?.cetTimesPlanName?.editText?.textStr()?.isNotEmpty() ?: true
        }
    }

    private fun showOperationTipsIfNeed() {
        if (viewModel.deviceCount != 1) {
            return
        }
        if (AppSettings.settingsStorage().getBoolean(AppSettings.TIME_OPERATION_TIPS_SHOWED_FLAG, false)) {
            return
        }
    }

    override fun handleBackPress(): Boolean {
        if (firstMakePlanAskExit()) {
            return true
        }
        if (btnTimeComplete.isEnabled) {
            askExit()
            return true
        }
        return false
    }

    /**
     * 首次制定时间计划时，有内容设置提示弹窗
     * 1、已选择守护日
     * 2、已设置可用时长
     * 3、已设置可用时段
     * 4、计划名称不能空
     */
    private fun firstMakePlanAskExit(): Boolean {
        if (selectedDateHasChange() ||
                mTimeGuardPlanDataList.filterIsInstance<TimeGuardPlanData>().first().hasSetTimeDuration ||
                mTimeGuardPlanDataList.filterIsInstance<TimeGuardPlanData>().first().timeGuardDailyPlanVO.timePeriodList.any { it.type != 1 }
                || (optionalNamesBinder?.isPlanNameNotEmpty() == true)
        ) {
            askExit()
            return true
        }
        return false
    }

    private fun selectedDateHasChange(): Boolean {
        val selectedList = mTimeGuardPlanDataList.filterIsInstance<TimeGuardPlanData>().first().selectedGuardDays
        return if (!makingPlanInfo.selectedDays.isNullOrEmpty()) {
            makingPlanInfo.selectedDays.toString() != this.toString()
        } else {
            selectedList.isNotEmpty()
        }
    }

    private fun askExit() {
        showConfirmDialog {
            messageId = R.string.save_edits_tips
            positiveListener = { exitFragment() }
        }
    }

    private fun setTimeGuardPlans() {
        if (makingPlanInfo.makingType == MakingPlanInfo.TYPE_SPARE && makingPlanInfo.usedName?.contains(optionalNamesBinder?.cetTimesPlanName?.editText?.textStr()) == true) {
            optionalNamesBinder?.tvTimesPlanNameDuplicateTips?.visible()
            optionalNamesBinder?.rvTimesPlanNameSuggestions?.gone()
            return
        }
        mTimeGuardPlanDataList.filterIsInstance<TimeGuardPlanData>().filter {
            isCompleted(it)
        }.map {
            val selectedDays = it.selectedGuardDays
            val timePlan = it.timeGuardDailyPlanVO
            //如果没有设置值，默认显示02小时00分钟的值；
            //            if (!it.hasSetTimeDuration) {
            //                timePlan.enabledTime = 3600 * 2
            //            }
            selectedDays.map { day ->
                timePlan.copy(dayOfWeek = day)
            }
        }.fold(mutableListOf<TimeGuardDailyPlanVO>()) { acc, list ->
            acc.addAll(list)
            acc
        }.let {
            if (makingPlanInfo.makingType == MakingPlanInfo.TYPE_NORMAL) {
                viewModel.addTimeGuardPlans(it)
            } else {
                optionalNamesBinder?.cetTimesPlanName?.editText?.textStr()?.let { it1 -> viewModel.addSparePlans(it1, it) }
            }
        }
    }

    private fun subscribeToViewModel() {
        viewModel.addPlan
                .observe(this, Observer { resource ->
                    resource?.onLoading {
                        showLoadingDialog(false)
                    }?.onError {
                        dismissLoadingDialog()
                        errorHandler.handleError(it)
                    }?.onSuccess {
                        dismissLoadingDialog()
                        processAddPlanSuccessfully()
                    }
                })

        viewModel.user.observe(this, Observer {
                    timeGuardBinder.isSVip = it?.vipRule?.time_defend_available_time_enabled == FLAG_POSITIVE_ACTION
                    adapter.notifyDataSetChanged()
                })
    }

    private fun processAddPlanSuccessfully() {
        if (makingPlanInfo.makingType == MakingPlanInfo.TYPE_NORMAL) {
            showMessage(R.string.time_table_update_successfully_tips)
            eventCenter.notifyPlansChanged()
            timeGuardNavigator.openGuardTimePlanTablePage()
            saveFirstSetTimeGuardDate()
        } else {
            eventCenter.notifySparePlansChanged()
            timeGuardNavigator.openSparePlanListPage()
        }
    }

    private var haveSetTimeGuardKey = "haveSetTimeGuard"

    @SuppressLint("SimpleDateFormat")
    @Synchronized
    private fun saveFirstSetTimeGuardDate() {
        if (AppContext.appDataSource().user().currentDevice.timePlanHasBeSet()) {
            return
        }
        if (AppSettings.settingsStorage().getString(haveSetTimeGuardKey).isNullOrEmpty()) {
            //保存的是明天0点0分0秒的时间
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, 1)
            val time = cal.time
            AppSettings.settingsStorage().putString(haveSetTimeGuardKey, SimpleDateFormat("yyyy-MM-dd 00:00:00").format(time))
        }
    }

    /**
     * 当前计划是否填写完整
     */
    private fun isCompleted(timeGuardPlanData: TimeGuardPlanData): Boolean {
        return timeGuardPlanData.selectedGuardDays.isNotEmpty() && (timeGuardPlanData.timeGuardDailyPlanVO.timePeriodList.any { it.type == 0 } || timeGuardPlanData.timeGuardDailyPlanVO.enabledTime>0)
    }

    private fun isAllGuardDaySelected(): Boolean {
        //1-7
        return timeGuardBinder.selectedDays().sum() == (1..7).sum()
    }
}