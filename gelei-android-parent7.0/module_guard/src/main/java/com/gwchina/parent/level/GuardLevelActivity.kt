package com.gwchina.parent.level

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.level.presentation.SetGuardLevelFragment
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.data.models.findChildByDeviceId
import com.gwchina.sdk.base.router.ChildDeviceInfo
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent

/**
 *守护等级模块，包括两个功能：
 *
 * - 为设备设置守护等级
 * - 选择守护等级
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-12-28 18:50
 */
@Route(path = RouterPath.GuardLevel.PATH)
class GuardLevelActivity : InjectorAppBaseActivity() {

    @JvmField
    @Autowired(name = RouterPath.PAGE_KEY)
    var pageKey: Int = RouterPath.GuardLevel.ACTION_SETTING_LEVEL

    @JvmField
    @Autowired(name = RouterPath.GuardLevel.DEVICE_ID_KEY)
    var childDeviceId: String? = null

    @JvmField
    @Autowired(name = RouterPath.GuardLevel.CHILD_USER_ID_KEY)
    var childUserId: String? = null

    @JvmField
    @Autowired(name = RouterPath.GuardLevel.CHILD_DEVICE_INFO_KEY)
    var childDeviceInfo: ChildDeviceInfo? = null

    @JvmField
    @Autowired(name = RouterPath.GuardLevel.IS_FROM_MIGRATION)
    var isFromMigration: Boolean = false


    override fun layout() = R.layout.app_base_activity

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)

        if (pageKey == RouterPath.GuardLevel.ACTION_SETTING_LEVEL) {
            val user = AppContext.appDataSource().user()
            if (childDeviceId.isNullOrEmpty()) {
                childDeviceId = user.currentDevice?.device_id ?: ""
            }
            if (childUserId.isNullOrEmpty()) {
                childUserId = user.findChildByDeviceId(childDeviceId ?: "")?.child_user_id ?: ""
            }
        }
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_GROWTLEVEV)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        savedInstanceState.ifNull {
            inFragmentTransaction { addWithDefaultContainer(SetGuardLevelFragment.instance(isFromMigration)) }
        }
    }

}