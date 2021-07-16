package com.gwchina.parent.member.data

import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 14:33
 */
interface MemberApi {

    /**加载会员中心首页数据*/
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/member/home")
    fun loadMemberPageData(): Observable<HttpResult<MemberInfo>>

    /**
     *查询购买记录
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/member/buy/flow")
    @FormUrlEncoded
    fun loadPurchaseRecord(
            @Field("pos") pos: Int/*开始位置*/,
            @Field("limit") limit: Int/*数量*/
    ): Observable<HttpResult<List<PurchaseRecord>>>

    /**
     * 加载购买会员-选项/特权
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/member/buy/index")
    @FormUrlEncoded
    fun loadPurchaseData(
            @Field("app_version") app_version: String/*开始位置*/,
            @Field("device_type") device_type: String/*03-Android*/
    ): Observable<HttpResult<PurchaseInfo>>

    /**
     * 购买会员-获取支付宝支付报文
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/member/buy/pay/create")
    @FormUrlEncoded
    fun createAliPayBuyInfo(@Field("pay_type") pay_type: String, @Field("plan_id") plan_id: String): Observable<HttpResult<AliPayInfo>>

    /**
     * 购买会员-获取微信支付报文
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/member/buy/pay/create")
    @FormUrlEncoded
    fun createWXPayBuyInfo(@Field("pay_type") pay_type: String, @Field("plan_id") plan_id: String): Observable<HttpResult<WXPayInfo>>

    /**
     * 购买会员-订单信息查询
     */
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("patriarch/order/search")
    @FormUrlEncoded
    fun searchOrder(@Field("order_no") orderNo: String): Observable<HttpResult<OrderInfo>>
}