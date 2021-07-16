package com.gwchina.sdk.base.data.api

import com.android.sdk.net.exception.ApiErrorException

/**登录过期，token 过期或者该账号在其他设备登录了*/
fun Throwable.isLoginExpired(): Boolean {
    return this is ApiErrorException && (ApiHelper.isLoginExpired(this.code) || ApiHelper.isSSOLoginExpired(this.code))
}

/**登录过期，token 过期或者该账号在其他设备登录了*/
fun Throwable.ifNotLoginExpired(action: () -> Unit) {
    if (!(this is ApiErrorException && (ApiHelper.isLoginExpired(this.code) || ApiHelper.isSSOLoginExpired(this.code)))) {
        action()
    }
}