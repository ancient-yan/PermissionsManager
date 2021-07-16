package com.gwchina.parent.main.presentation.weekly

import android.arch.lifecycle.Lifecycle
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.app.ui.processListErrorWithStatus
import com.android.base.app.ui.processListResultWithStatus
import com.android.base.app.ui.showLoadingIfEmpty
import com.android.base.interfaces.OnItemClickListener
import com.android.base.kotlin.dip
import com.android.base.rx.bindLifecycle
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.MainNavigator
import com.gwchina.parent.main.data.ReportInfo
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorBaseListFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.models.isIOS
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import kotlinx.android.synthetic.main.week_fragment_list.*
import java.util.*
import javax.inject.Inject

/**
 * 周报列表
 *@author hujie
 *      Email: hujie1991@126.com
 *      Date : 2019-08-10 15:34
 */
class WeeklyListFragment : InjectorBaseListFragment<WeeklyVO>() {

    companion object {
        const val SHOW_WEEKLIST_TIPS = "showWeekListTips"
    }

    private val weeklyListAdapter by lazy { WeeklyListAdapter(this) }

    private val viewModel by lazy {
        getViewModel<WeeklyListViewModel>(viewModelFactory)
    }

    @Inject
    lateinit var mainNavigator: MainNavigator

    override fun provideLayout() = R.layout.week_fragment_list

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        canShowTips()
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_GUARDREPORT)
        weeklyListAdapter.onDetailListener = object : OnItemClickListener<ReportInfo>() {
            override fun onClick(view: View, t: ReportInfo) {
                //获取上一周是第几周
                val lastCalendar = Calendar.getInstance()
                lastCalendar.time = Date(System.currentTimeMillis() - 1000 * 3600 * 24 * 7)
                val lastWeek = lastCalendar.get(Calendar.WEEK_OF_YEAR)

                //获取周报是第几周
                lastCalendar.time = Date(t.create_time - 1000 * 3600 * 24 * 7)
                val weeklyWeek = lastCalendar.get(Calendar.WEEK_OF_YEAR)

                if (/*lastWeek == weeklyWeek &&*/ !t.user_id.isNullOrEmpty()) {
                    mainNavigator.openGuardReportForChild(t.record_time,t.user_id)
                }
//                else if (!t.url.isNullOrEmpty()) {
//                    mainNavigator.openWeeklyDetail(t.url)
//                }
            }
        }

        setDataManager(weeklyListAdapter)
        rvWeeklyList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val topPadding = if (parent.getChildAdapterPosition(view) == 0) dip(20) else 0
                outRect.set(0, topPadding, 0, dip(20))
            }
        })

        rvWeeklyList.adapter = weeklyListAdapter
        autoRefresh()
    }


    override fun onRefresh() {
        viewModel.loadWeeklyListData()
                .doOnSubscribe { showLoadingIfEmpty() }
                .bindLifecycle(this, Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    processListResultWithStatus(it)
                }, {
                    processListErrorWithStatus(it)
                })
    }

    private fun canShowTips() {
        var hasIPhone = false
        run outside@{
            AppContext.appDataSource().user().childList?.forEach {
                it.device_list?.forEach { device ->
                    if (device.isIOS()) {
                        hasIPhone = true
                        return@outside
                    }
                }
            }
        }
        if (hasIPhone && !AppSettings.settingsStorage().getBoolean(SHOW_WEEKLIST_TIPS, false)) {
            showTips()
        }
    }

    private fun showTips() {
        showConfirmDialog {
            this.messageId = R.string.guarding_report_tips
            positiveText = context.resources.getString(R.string.i_got_it)
            checkBoxText = context.resources.getString(R.string.do_not_tips_again)
            checkBoxChecked = true
            negativeText = null
            positiveListener2 = { _, isChecked ->
                AppSettings.settingsStorage().putBoolean(SHOW_WEEKLIST_TIPS, isChecked)

            }
        }?.setCanceledOnTouchOutside(false)
    }
}

