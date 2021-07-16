package com.gwchina.parent.net

import android.os.Bundle
import android.support.v4.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.net.presentation.home.fragment.NetGuardFragment
import com.gwchina.parent.net.presentation.home.fragment.NetGuardGuideFragment
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.data.api.isYes
import com.gwchina.sdk.base.data.models.findChildByDeviceId
import com.gwchina.sdk.base.router.RouterPath

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-15 10:17
 */
@Route(path = RouterPath.NetGuard.PATH)
class NetGuardActivity : InjectorAppBaseActivity() {

    @JvmField
    @Autowired(name = RouterPath.AppsGuard.DEVICE_ID_KEY)
    var childDeviceId: String? = null

    @JvmField
    @Autowired(name = RouterPath.AppsGuard.CHILD_USER_ID_KEY)
    var childUserId: String? = null

    override fun layout() = R.layout.app_base_activity

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        if (childDeviceId.isNullOrEmpty() || childUserId.isNullOrEmpty()) {
            val user = AppContext.appDataSource().user()
            childUserId = user.currentChild?.child_user_id ?: ""
            childDeviceId = user.currentDevice?.device_id ?: ""
        }
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)


        //判断当前孩子的所有设备中是否有开启上网守护的设备，如果有则认为该孩子开启过守护上网，不需要进入引导页
        val child = AppContext.appDataSource().user().findChildByDeviceId(childDeviceId ?: "")
        val find = child?.device_list?.find {
            isYes(it.setting_url_pattern_flag)
        }

        val fragment: Fragment = if (find != null) {
            NetGuardFragment()
        } else {
            NetGuardGuideFragment()
        }

        inFragmentTransaction {
            addWithDefaultContainer(fragment)
        }
    }

}