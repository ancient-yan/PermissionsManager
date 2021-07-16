package com.gwchina.sdk.base.app

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.NetworkUtils
import javax.inject.Inject
import javax.inject.Singleton

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-03-22 15:39
 */
@Singleton
class AndroidKit @Inject constructor() {

    fun isConnected() = NetworkUtils.isConnected()

    fun getAppVersionName(): String =AppUtils.getAppVersionName()

}