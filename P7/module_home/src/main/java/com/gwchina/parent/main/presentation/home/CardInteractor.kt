package com.gwchina.parent.main.presentation.home

import com.android.base.app.fragment.BaseFragment
import com.gwchina.parent.main.MainNavigator
import com.gwchina.parent.main.data.PhoneApprovalInfo
import com.gwchina.parent.main.data.Soft
import com.gwchina.parent.main.presentation.home.ApprovalInfoVO.SoftWrapper
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.User

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-29 15:05
 */
class CardInteractor(
        val host: BaseFragment,
        val navigator: MainNavigator,
        val cardDataProvider: CardDataProvider
) {

    val usingUser: User
        get() = cardDataProvider.usingUser

    val usingData: HomeVO?
        get() = cardDataProvider.usingData

    fun showMessage(msg: String) {
        host.showMessage(msg)
    }

}

interface CardDataProvider {

    val usingUser: User
    val usingData: HomeVO?

    fun observeHomeData(listener: (HomeVO?) -> Unit) {
        observeHomeData(object : HomeDataListener {
            override fun onHasChildNoDevice(data: HomeVO?) {

            }

            override fun onSuccess(data: HomeVO?) {
                listener(data)
            }

            override fun onError() = Unit
            override fun onLoading() = Unit
        })
    }

    fun observeHomeData(listener: HomeDataListener)
    fun observeUser(listener: UserStatusListener)

    fun setTempUsable(tempUsableMinutes: Int)
    fun closeCloseTempUsable(tempUsableId: String)
    fun forbidNewApp(app: SoftWrapper)
    fun switchChild(child: Child, device: Device? = null)
    fun installSoft(soft: Soft)
    fun approvalNewNumber(phoneApprovalInfo: PhoneApprovalInfo, allow: Boolean)

}