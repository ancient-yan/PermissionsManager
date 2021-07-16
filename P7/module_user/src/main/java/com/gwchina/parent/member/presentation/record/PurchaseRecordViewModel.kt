package com.gwchina.parent.member.presentation.record

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.rx.SchedulerProvider
import com.gwchina.parent.member.data.MemberRepository
import com.gwchina.parent.member.data.PurchaseRecord
import javax.inject.Inject

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 15:11
 */
class PurchaseRecordViewModel @Inject constructor(
        private val memberRepository: MemberRepository,
        private val schedulerProvider: SchedulerProvider
) : ArchViewModel() {
    private val _recordData = MutableLiveData<RecordListStatus>()

    val recordData: LiveData<RecordListStatus>
        get() = _recordData

    fun loadPurchaseRecord(position: Int) {
        memberRepository.loadPurchaseRecord(position)
                .observeOn(schedulerProvider.io())
                .subscribe(
                        {
                            if (position == 0) {
                                if (!it.isPresent) {
                                    _recordData.postValue(RecordListStatus())
                                } else {
                                    _recordData.postValue(RecordListStatus(recordList = it.get(), lastSize = it.get().size))
                                }
                            } else {
                                if (it.isPresent){
                                    val list = mutableListOf<PurchaseRecord>()
                                    _recordData.value?.recordList?.let { old -> list.addAll(old) }
                                    list.addAll(it.get())
                                    _recordData.postValue(RecordListStatus(recordList = list, lastSize = it.get().size))
                                }else{
                                    _recordData.postValue(_recordData.value?.copy(loadError = false)
                                            ?: RecordListStatus(loadError = false))
                                }
                            }
                        },
                        {
                            if (position == 0) {
                                _recordData.postValue(RecordListStatus(loadError = true))
                            } else {
                                _recordData.postValue(_recordData.value?.copy(loadError = true)
                                        ?: RecordListStatus(loadError = true))
                            }
                        }
                )

    }
}

data class RecordListStatus(
        val loadError: Boolean = false,
        val recordList: List<PurchaseRecord>? = null,
        val lastSize: Int = 0
)