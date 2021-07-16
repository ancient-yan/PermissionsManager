package com.gwchina.parent.main.presentation.home

import com.android.base.data.Resource
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.data.utils.JsonUtils
import com.gwchina.sdk.base.debug.ifOpenDebug
import timber.log.Timber


interface HomeDataListener {
    fun onSuccess(data: HomeVO?)
    //有孩子没有绑定设备
    fun onHasChildNoDevice(data: HomeVO?)
    fun onError()
    fun onLoading()
}

typealias UserStatusListener = (User) -> Unit

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-15 15:11
 */
class CardDataDispatcher {

    private val userListeners by lazy { mutableListOf<UserStatusListener>() }
    private val homePageResponseListeners by lazy { mutableListOf<HomeDataListener>() }

    private var homeDataStatus: Resource<HomeVO>? = null

    var showingHomeData: HomeVO? = null
        private set

    var showingUser: User = User.NOT_LOGIN
        private set

    private var showingLocationDetail: LocationDetail? = null

    fun dispatchHomeData(value: Resource<HomeVO>) {
        ifOpenDebug {
            Timber.d("dispatchHomeData is Error = ${value.isError}, data = ${JsonUtils.toJson(value.get())}")
        }
        homeDataStatus = value
        when {
            value.isSuccess ->
            {
                innerHomeDataListener.onSuccess(value.orElse(null))
                innerHomeDataListener.onHasChildNoDevice(value.orElse(null))
            }
            value.isLoading -> innerHomeDataListener.onLoading()
            value.isError -> innerHomeDataListener.onError()
        }
    }

    fun dispatchUser(user: User) {
        ifOpenDebug {
            Timber.d("dispatchUser ${JsonUtils.toJson(user)}")
        }
        innerUserListener(user)
    }

    internal fun registerUserListener(listener: UserStatusListener) {
        if (!userListeners.contains(listener)) {
            userListeners.add(listener)
            //initiative notify
            listener(showingUser)
        }
    }

    internal fun registerHomePageDataListener(listener: HomeDataListener) {
        if (!homePageResponseListeners.contains(listener)) {
            homePageResponseListeners.add(listener)
            //initiative notify
            val value = homeDataStatus ?: return
            when {
                value.isSuccess -> {
                    listener.onSuccess(value.orElse(null))
                    listener.onHasChildNoDevice(value.orElse(null))
                }
                value.isLoading -> listener.onLoading()
                value.isError -> listener.onError()
            }
        }
    }

    private val innerHomeDataListener = object : HomeDataListener {

        override fun onHasChildNoDevice(data: HomeVO?) {
            showingHomeData = data
            if (data != null && showingUser.currentDevice == null) {
                homePageResponseListeners.forEach {
                    it.onHasChildNoDevice(data) }
            }
        }

        override fun onSuccess(data: HomeVO?) {
            showingHomeData = data
            if (data != null && showingUser.currentDevice != null) {
                homePageResponseListeners.forEach { it.onSuccess(data) }
            } else {
                userListeners.forEach { it(showingUser) }
            }
        }

        override fun onLoading() {
            if (showingHomeData == null) {
                Timber.d("notify the loading status")
                homePageResponseListeners.forEach { it.onLoading() }
            } else {
                Timber.d("we have a showing data, we ignore the loading status")
            }
        }

        override fun onError() {
            if (showingHomeData == null) {
                Timber.d("notify the error status")
                homePageResponseListeners.forEach { it.onError() }
            } else {
                Timber.d("we have a showing data, we ignore the loading status")
            }
        }
    }

    private val innerUserListener = object : UserStatusListener {

        override fun invoke(user: User) {
            when {
                user.currentDevice?.hasSetLevel() == true -> userLoginedHasGuardLevel(user)
                user.currentChild?.boundDevice() == true -> userLoginedNoGuardLevel(user)
                user.logined() -> userLoginedNoDevice(user)
                else -> userLogout(user)
            }
        }

        fun userLogout(user: User) {
            showingUser = user
            showingHomeData = null
            showingLocationDetail = null
            userListeners.forEach { it(user) }
        }

        fun userLoginedNoDevice(user: User) {
            showingUser = user
            showingHomeData = null
            showingLocationDetail = null
            userListeners.forEach { it(user) }
        }

        fun userLoginedNoGuardLevel(user: User) {
            showingUser = user
            showingLocationDetail = null
            userListeners.forEach { it(user) }
        }

        fun userLoginedHasGuardLevel(user: User) {
            showingUser = user
            val showingHomeData = showingHomeData
            if (showingHomeData == null || showingUser.currentDevice?.device_id != showingHomeData.deviceInfo?.device_id) {
                this@CardDataDispatcher.showingHomeData = null
                userListeners.forEach { it(user) }
                homePageResponseListeners.forEach { it.onSuccess(null) }
            } else {
                userListeners.forEach { it(user) }
                homePageResponseListeners.forEach { it.onSuccess(showingHomeData) }
            }
        }
    }


}