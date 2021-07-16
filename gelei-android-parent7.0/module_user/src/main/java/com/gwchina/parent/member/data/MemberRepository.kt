package com.gwchina.parent.member.data

import com.android.base.app.dagger.ActivityScope
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultExtractor
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.app.AndroidKit
import com.gwchina.sdk.base.config.DEFAULT_PAGE_SIZE
import com.gwchina.sdk.base.utils.GwDevices
import io.reactivex.Observable
import javax.inject.Inject

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 14:33
 */
@ActivityScope
class MemberRepository @Inject constructor(
        private val memberApi: MemberApi,
        private val androidKit: AndroidKit
) {

    companion object {
        private const val WX_PAY = "01"
        private const val ALI_PAY = "02"
    }

    /**
     * 加载会员中心首页数据
     */
    fun loadMemberPageData(): Observable<Optional<MemberInfo>> {
        return memberApi.loadMemberPageData()
                .optionalExtractor()
    }

    /**
     * 加载购买会员信息
     */
    fun loadPurchaseData(): Observable<PurchaseInfo> {
        return memberApi.loadPurchaseData(
                androidKit.getAppVersionName(),
                GwDevices.DEVICE_TYPE
        ).resultExtractor()
    }

    /**
     * 购买会员-获取支付宝支付报文
     */
    fun createAliPayBuyInfo(plan_id: String): Observable<Optional<AliPayInfo>> {
        return memberApi.createAliPayBuyInfo(ALI_PAY, plan_id)
                .optionalExtractor()
    }

    /**
     * 购买会员-获取微信支付报文
     */
    fun createWXPayBuyInfo(plan_id: String): Observable<Optional<WXPayInfo>> {
        return memberApi.createWXPayBuyInfo(WX_PAY, plan_id)
                .optionalExtractor()
    }

    /**
     * 加载购买记录
     */
    fun loadPurchaseRecord(position: Int, limit: Int = DEFAULT_PAGE_SIZE): Observable<Optional<List<PurchaseRecord>>> {
        return memberApi.loadPurchaseRecord(position, limit).optionalExtractor()

    }

    /**
     * 查询订单
     */
    fun searchOrder(orderNo: String): Observable<Optional<OrderInfo>> {
        return memberApi.searchOrder(orderNo).optionalExtractor()
    }

}