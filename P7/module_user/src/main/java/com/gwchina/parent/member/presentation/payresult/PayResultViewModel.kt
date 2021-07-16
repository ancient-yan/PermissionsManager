package com.gwchina.parent.member.presentation.payresult

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.SchedulerProvider
import com.gwchina.parent.member.data.MemberRepository
import javax.inject.Inject

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-28 14:14
 */
class PayResultViewModel @Inject constructor(
        private val memberRepository: MemberRepository,
        private val schedulerProvider: SchedulerProvider,
        private val payDataMapper: PayDataMapper
) : ArchViewModel() {
    private val _orderData = MutableLiveData<Resource<PayResultVO>>()

    val orderData: LiveData<Resource<PayResultVO>>
        get() = _orderData

    /**
     * 查询订单
     */
    fun searchOrder(orderNo: String) {
        _orderData.postValue(Resource.loading())
        memberRepository.searchOrder(orderNo)
                .subscribeOn(schedulerProvider.io())
                .map { payDataMapper.transform(it.get()) }
                .subscribe(
                        {
                            _orderData.postValue(Resource.success(it))

                        },
                        {
                            _orderData.postValue(Resource.error(it))
                        }
                )
    }
}