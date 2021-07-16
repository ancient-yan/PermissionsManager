package com.gwchina.parent.account.data

import com.android.base.app.dagger.ActivityScope
import com.android.sdk.net.kit.resultExtractor
import com.gwchina.sdk.base.data.api.HttpResult
import com.gwchina.sdk.base.data.api.SMS_LOGIN
import com.gwchina.sdk.base.data.api.WECHAT_BIND
import com.gwchina.sdk.base.data.api.isYes
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.LoginResponse
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-13 14:30
 */
@ActivityScope
class AccountRepository @Inject constructor(
        private val accountApi: AccountApi,
        private val appDataSource: AppDataSource
) {

    fun sendLoginSmsCode(cellphone: String): Completable {
        return appDataSource.sendSmsCode(SMS_LOGIN, cellphone)
    }

    fun sendWechatBindingSmsCode(cellphone: String): Completable {
        return appDataSource.sendSmsCode(WECHAT_BIND, cellphone)
    }

    fun bindWechat(openid: String, cellphone: String, code: String): Observable<LoginResponse> {
        return accountApi.bindWechat(openid, cellphone, code).processTokenResponse()
    }

    fun smsLogin(cellphone: String, code: String) = accountApi.smsLogin(cellphone, code).processTokenResponse()

    private fun Observable<HttpResult<LoginResponse>>.processTokenResponse(): Observable<LoginResponse> =
            resultExtractor()
                    .doOnNext {
                        appDataSource.saveUser(it.login_info, it.app_token, it.expire_time)
                    }

    fun wechatLogin(openid: String): Observable<LoginResponse> {
        return accountApi.wechatLogin(openid)
                .resultExtractor()
                .doOnNext {
                    if (!isYes(it.register_flag)) {
                        appDataSource.saveUser(it.login_info, it.app_token, it.expire_time)
                    }
                }
    }

}