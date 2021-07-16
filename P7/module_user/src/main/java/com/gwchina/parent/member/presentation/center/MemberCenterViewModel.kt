package com.gwchina.parent.member.presentation.center

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.kotlin.toArrayList
import com.android.base.rx.SchedulerProvider
import com.android.base.rx.subscribeIgnoreError
import com.android.base.utils.android.ResourceUtils.getString
import com.android.sdk.net.exception.ServerErrorException
import com.android.sdk.social.wechat.PayInfo
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.data.AliPayInfo
import com.gwchina.parent.member.data.MemberItem
import com.gwchina.parent.member.data.MemberRepository
import com.gwchina.parent.member.presentation.purchase.PurchaseDataMapper
import com.gwchina.sdk.base.data.app.AppDataSource
import javax.inject.Inject

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 10:32
 */
class MemberCenterViewModel @Inject constructor(
        private val memberRepository: MemberRepository,
        private val schedulerProvider: SchedulerProvider,
        private val purchaseDataMapper: PurchaseDataMapper,
        private val appDataSource: AppDataSource
) : ArchViewModel() {

    private val _memberCenterVOData = MutableLiveData<Resource<MemberCenterVO>>()
    val memberCenterVOData: LiveData<Resource<MemberCenterVO>>
        get() = _memberCenterVOData

    private val _aliPayData = MutableLiveData<Resource<AliPayInfo>>()
    private val _wxPayData = MutableLiveData<Resource<PayInfo>>()
    private val _orderNoData = MutableLiveData<String>()

    val aliPayData: LiveData<Resource<AliPayInfo>>
        get() = _aliPayData
    val wxPayData: LiveData<Resource<PayInfo>>
        get() = _wxPayData
    val orderNoData: LiveData<String>
        get() = _orderNoData


    init {
        appDataSource.observableUser()
                .autoDispose()
                .subscribeIgnoreError {
                    loadMemberPageData(false)
                }
    }

    /**加载会员中心首页数据*/
    @SuppressLint("CheckResult")
    fun loadMemberPageData(isNeedSyncUserData: Boolean) {
        if (isNeedSyncUserData) {
            syncUserData()
            return
        }
        memberRepository.loadMemberPageData()
                .map {
                    if (it.isPresent) {
                        val toArrayList = it.get().member_item_list.toArrayList()
                        toArrayList.add(MemberItem(member_item_name = getString(R.string.more_functions), member_item_desc = getString(R.string.coming_soon)))
                        MemberCenterVO(
                                user = appDataSource.user(),
                                memberItemList = toArrayList,
                                endTime = it.get().end_time,
                                memberServiceItemList = it.get().member_service_list
                        )
                    } else {
                        null
                    }
                }
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                        {
                            _memberCenterVOData.postValue(Resource.success(it))
                        },
                        {
                            _memberCenterVOData.postValue(Resource.error(it))
                        }
                )

    }

    private fun syncUserData() {
        appDataSource.syncUser().subscribeOn(schedulerProvider.io()).subscribe()
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