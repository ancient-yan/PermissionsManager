package com.gwchina.parent.main

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import com.android.base.app.activity.ActivityDelegate
import com.android.base.rx.bindLifecycle
import com.android.base.rx.observeOnUI
import com.android.base.rx.subscribeIgnoreError
import com.gwchina.sdk.base.data.api.isYes
import com.gwchina.sdk.base.data.app.AppDataSource

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-07 15:32
 */
class SettingLevelListener : ActivityDelegate<MainActivity> {

    private lateinit var mainActivity: MainActivity
    private lateinit var appDataSource: AppDataSource

    override fun onAttachedToActivity(activity: MainActivity) {
        mainActivity = activity
    }

    override fun onCreateAfterSetContentView(savedInstanceState: Bundle?) {
        appDataSource = mainActivity.appDataSource
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        mainActivity.appDataSource.observableUser()
                .observeOnUI()
                .bindLifecycle(mainActivity, Lifecycle.Event.ON_DESTROY)
                .subscribeIgnoreError {
                    val currentDevice = it.currentDevice
                    if (currentDevice != null && !currentDevice.hasSetLevel()) {
                        mainActivity.mainNavigator.openGuardLevelForDevice(currentDevice.device_id)
                        mainActivity.isShowGuardGuidePop = true
                        //定制机在设置守护等级之后，需要显示弹框
                        if (isYes(currentDevice.custom_device_flag)) {
                            mainActivity.isShowCustomDialog = true
                        }
                    }
                }
    }

}