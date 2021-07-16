package com.gwchina.parent.member.presentation.purchase

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.SchedulerProvider
import com.android.sdk.net.exception.ServerErrorException
import com.android.sdk.social.wechat.PayInfo
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.parent.member.data.AliPayInfo
import com.gwchina.parent.member.data.MemberRepository
import com.gwchina.parent.member.data.PurchaseInfo
import com.gwchina.sdk.base.data.api.AD_POSITION_MEMBER
import com.gwchina.sdk.base.data.app.AdvertisingFilter
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Advertising
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import javax.inject.Inject

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 15:06
 */
class PurchaseMemberViewModel @Inject constructor(
        private val memberRepository: MemberRepository,
        private val schedulerProvider: SchedulerProvider,
        private val purchaseDataMapper: PurchaseDataMapper,
        private val appDataSource: AppDataSource
) : ArchViewModel() {

    private val _purchaseData = MutableLiveData<Resource<PurchaseDataVO>>()
    private val _aliPayData = MutableLiveData<Resource<AliPayInfo>>()
    private val _wxPayData = MutableLiveData<Resource<PayInfo>>()
    private val _orderNoData = MutableLiveData<String>()

    val purchaseData: LiveData<Resource<PurchaseDataVO>>
        get() = _purchaseData
    val aliPayData: LiveData<Resource<AliPayInfo>>
        get() = _aliPayData
    val wxPayData: LiveData<Resource<PayInfo>>
        get() = _wxPayData
    val orderNoData: LiveData<String>
        get() = _orderNoData

    private val advertisingFilter = object : AdvertisingFilter {
        override fun filter(ad: Advertising): Boolean {
            return ad.ad_position == AD_POSITION_MEMBER
        }
    }

    @SuppressLint("CheckResult")
    fun loadPurchaseData() {
        _purchaseData.postValue(Resource.loading())
        val advertisingList = appDataSource.advertisingList(advertisingFilter).toObservable()
        val loadPurchaseData = memberRepository.loadPurchaseData()
        Observable.zip(loadPurchaseData, advertisingList,
                BiFunction<PurchaseInfo, Optional<List<Advertising>>, PurchaseDataVO> { purchaseInfo, adOptional ->
                    purchaseDataMapper.transform(purchaseInfo, adOptional.orElse(null), appDataSource.user())!!
                }).subscribe(
                {
                    _purchaseData.postValue(Resource.success(it))
                }
                ,
                {
                    _purchaseData.postValue(Resource.error(it))
                }
        )
    }

    fun createAliPayBuyInfo(planId: String) {
        _orderNoData.postValue(null)
        _aliPayData.postValue(Resource.loading())
        memberRepository.createAliPayBuyInfo(planId)
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                        {
                            if (!it.isPresent) {
                                _aliPayData.postValue(Resource.error(ServerErrorException(ServerErrorException.SERVER_DATA_ERROR)))
                            } else{
                                _orderNoData.postValue(it.get().order_no)
                                _aliPayData.postValue(Resource.success(it.get()))
                            }
                        },
                        {
                            _aliPayData.postValue(Resource.error(it))
                        }
                )
    }

    @SuppressLint("CheckResult")
    fun createWXPayBuyInfo(planId: String) {
        _orderNoData.postValue(null)
        _wxPayData.postValue(Resource.loading())
        memberRepository.createWXPayBuyInfo(planId)
                .map {
                    if (it.isPresent) {
                        _orderNoData.postValue(it.get().order_no)
                        purchaseDataMapper.transformWxPayInfo(it.get())
                    } else {
                        null
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                        {
                            if (it == null) {
                                _wxPayData.postValue(Resource.error(ServerErrorException(ServerErrorException.SERVER_DATA_ERROR)))
                            } else _wxPayData.postValue(Resource.success(it))
                        },
                        {
                            _wxPayData.postValue(Resource.error(it))
                        }
                )
    }
}