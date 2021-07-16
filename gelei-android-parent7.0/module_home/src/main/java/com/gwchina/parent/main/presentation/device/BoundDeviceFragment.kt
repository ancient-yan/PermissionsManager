package com.gwchina.parent.main.presentation.device

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModel
import com.android.base.kotlin.dip
import com.android.base.kotlin.visibleOrGone
import com.android.base.widget.recyclerview.MarginDecoration
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.MainNavigator
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.config.CHILD_USER_ID_KEY
import com.gwchina.sdk.base.data.api.FLAG_POSITIVE_ACTION
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.data.models.deviceCount
import com.gwchina.sdk.base.data.models.guardDeviceCount
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.utils.mapChildAvatarSmall
import com.gwchina.sdk.base.widget.member.OpenMemberDialog
import kotlinx.android.synthetic.main.bound_device_fragment.*
import javax.inject.Inject

/**
 * 已绑设备
 *
 *@author Wangwb
 *        Date : 2018/12/24 9:01 PM
 */
class BoundDeviceFragment : InjectorBaseFragment() {

    companion object {

        fun newInstance(childUserId: String): BoundDeviceFragment {
            return BoundDeviceFragment().apply {
                arguments = Bundle().apply {
                    putString(CHILD_USER_ID_KEY, childUserId)
                }
            }
        }

    }

    private val childUserId by lazy {
        arguments?.getString(CHILD_USER_ID_KEY) ?: throw NullPointerException("no child id")
    }

    private val boundDeviceViewModel by lazy {
        getViewModel<DeviceViewModel>(viewModelFactory)
    }

    private val deviceUnbindProcessor by lazy {
        DeviceUnbindProcessor(this, boundDeviceViewModel, errorHandler)
    }

    private val deviceAdapter by lazy {
        BoundDeviceAdapter(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_BINDDEVICE)
    }

    @Inject
    lateinit var mainNavigator: MainNavigator

    override fun provideLayout() = R.layout.bound_device_fragment

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        val vipRule = AppContext.appDataSource().user().vipRule
        rvDeviceList.layoutManager = LinearLayoutManager(context)
        rvDeviceList.addItemDecoration(MarginDecoration(0, dip(15), 0, 0))
        rvDeviceList.adapter = deviceAdapter

        deviceAdapter.onUnbindListener = {
            deviceUnbindProcessor.showAskUnbindDeviceDialog(childUserId, it, createPendingUnbindDeviceName(it))
        }

        deviceAdapter.onSwitchGuardLevelListener = {
            StatisticalManager.onEvent(UMEvent.ClickEvent.BINDDEVICE_BTN_GROWTLEVEV)
            //会员过期
            if (isMemberGuardExpired(it.status)) {
                OpenMemberDialog.show(requireContext()) {
                    message = getString(R.string.multi_device_guard_requirement_member_tips_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
                    messageDesc= getString(R.string.open_member_recovery_guard_function_tips_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
                    positiveText = getString(R.string.open_vip_to_experience_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
                }
            } else {
                appRouter.build(RouterPath.GuardLevel.PATH)
                        .withString(RouterPath.GuardLevel.CHILD_USER_ID_KEY, childUserId)
                        .withString(RouterPath.GuardLevel.DEVICE_ID_KEY, it.device_id)
                        .navigation(requireActivity(), RouterPath.GuardLevel.REQUEST_CODE)
            }
        }

        tvDeviceItemAddDevice.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.BINDDEVICE_BTN_ADDDEVICE)
            val user = boundDeviceViewModel.user.value ?: return@setOnClickListener
            if (vipRule?.home_mine_add_device_enabled== FLAG_POSITIVE_ACTION || user.guardDeviceCount() == 0) {
                mainNavigator.openAddDevicePage(childUserId)
            } else {
                OpenMemberDialog.show(requireContext()) {
                    message = getString(R.string.multi_device_guard_requirement_member_tips_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
                    messageDescId = R.string.multi_device_guard_requirement_member_desc
                    positiveText = getString(R.string.open_vip_to_experience_mask, vipRule?.home_mine_add_device_enabled_minimum_level)
                }
            }
        }
    }

    private fun subscribeViewModel() {
        //设备信息
        boundDeviceViewModel.user
                .observe(this, Observer {
                    it ?: return@Observer
                    deviceUnbindProcessor.phoneNumber = it.patriarch.phone ?: ""
                    showDevice(it)
                })
    }

    private fun showDevice(user: User) {
        user.childList?.find {
            childUserId == it.child_user_id
        }?.let {
            ivDeviceChildAvatar.setImageResource(mapChildAvatarSmall(it.sex))
            ivDeviceItemChildName.text = getString(R.string.devices_of_x_mask, it.nick_name)
            val deviceList = it.device_list ?: emptyList()
            deviceAdapter.setDataSource(deviceList, true)
            tvDeviceItemAddDevice.visibleOrGone(!it.reachMaxDeviceCount() && deviceList.all { device -> !isMemberGuardExpired(device.status) })
        }

        if (deviceAdapter.isEmpty) {
            exitFragment()
            if (user.deviceCount() == 0) {
                AppSettings.settingsStorage().putBoolean(AppSettings.TIME_OPERATION_TIPS_SHOWED_FLAG, false)
            }
        }
    }

}