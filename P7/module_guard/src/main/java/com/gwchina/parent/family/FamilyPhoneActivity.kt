package com.gwchina.parent.family

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.presentation.home.FamilyIphonePageFragment
import com.gwchina.parent.family.presentation.home.FamilyPhoneFragment
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.findDevice
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject


@Route(path = RouterPath.FamilyPhone.PATH)
class FamilyPhoneActivity : InjectorAppBaseActivity() {

    @Inject
    lateinit var appDataSource: AppDataSource

    @JvmField
    @Autowired(name = RouterPath.FamilyPhone.DEVICE_ID_KEY)
    var childDeviceId: String? = null

    @JvmField
    @Autowired(name = RouterPath.FamilyPhone.CHILD_USER_ID_KEY)
    var childUserId: String? = null

    @JvmField
    @Autowired(name = RouterPath.FamilyPhone.PHONE_PLAN_HAS_BE_SET)
    var phonePlanHasBeSet: Boolean = false

    override fun layout(): Any? = R.layout.app_base_activity

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

        savedInstanceState.ifNull {
            val childDevice = AppContext.appDataSource().user().findDevice(childDeviceId ?: "")
            if (childDevice != null && currentDeviceIsIphone(childDevice)) {
                inFragmentTransaction {
                    addWithDefaultContainer(FamilyIphonePageFragment())
                }
            } else {
                inFragmentTransaction {
                    addWithDefaultContainer(FamilyPhoneFragment.getFragment(phonePlanHasBeSet))
                }
            }
        }
    }

    private fun currentDeviceIsIphone(childDevice: Device): Boolean {
        return (childDevice.ios_supervised_flag != null || childDevice.ios_supervised_flag != null)
    }

}