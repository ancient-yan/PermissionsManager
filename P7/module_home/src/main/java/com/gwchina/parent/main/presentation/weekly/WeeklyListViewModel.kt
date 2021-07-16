package com.gwchina.parent.main.presentation.weekly

import android.annotation.SuppressLint
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.rx.SchedulerProvider
import com.gwchina.parent.main.data.MainRepository
import com.gwchina.parent.main.data.WeeklyInfo
import io.reactivex.Observable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-22 14:05
 */
class WeeklyListViewModel @Inject constructor(
        private val mainRepository: MainRepository,
        private val schedulerProvider: SchedulerProvider
        ) : ArchViewModel() {


    @SuppressLint("CheckResult")
    fun loadWeeklyListData() : Observable<List<WeeklyVO>> {
        return mainRepository.loadWeeklyListData()
                .subscribeOn(schedulerProvider.io())
                .map {
                    mapData(it.orElse(null))
                }
                .observeOn(schedulerProvider.ui())
    }

    private fun mapData(response: List<WeeklyInfo>?): List<WeeklyVO> {
        val weeklyVos = mutableListOf<WeeklyVO>()
        response?.forEach { it ->
//            var month = ""
//            if (!it.month.isNullOrEmpty()) {
//                val data = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(it.month)
//                month = SimpleDateFormat("yyyy年MM月", Locale.getDefault()).format(data)
//            }
            it.report_list?.forEachIndexed { index, reportInfo ->
                weeklyVos.add(WeeklyVO(month = if (index == 0) it.month else "", report_list = reportInfo))
            }
        }
        return weeklyVos
    }
}