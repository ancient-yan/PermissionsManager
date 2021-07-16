package com.gwchina.sdk.base.widget.member

import com.android.sdk.net.kit.resultChecker
import com.gwchina.sdk.base.data.api.EXPIRE_RETAIN_FLAG_SET
import com.gwchina.sdk.base.data.app.AppDataSource
import io.reactivex.Completable

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-28 14:36
 */
internal class MemberRepository(
        private val memberApi: MemberApi,
        private val appDataSource: AppDataSource
) {

    fun setRetainedDevice(deviceId: String): Completable {
        return memberApi.setRetainDevice(deviceId)
                .resultChecker()
                .ignoreElements()
                .doOnComplete {
                    val member = appDataSource.user().member_info
                    if (member != null) {
                        val copy = member.copy(expire_setting_retain_flag = EXPIRE_RETAIN_FLAG_SET)
                        appDataSource.updateMember(copy)
                    }
                    appDataSource.syncUser()
                }
    }

}

