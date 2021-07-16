package com.gwchina.parent.main.presentation.device

import android.arch.lifecycle.Observer
import com.android.base.app.fragment.BaseFragment
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.utils.buildFlag
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.ErrorHandler
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog

internal class DeviceUnbindProcessor(
        private val baseFragment: BaseFragment,
        private val boundDeviceViewModel: DeviceViewModel,
        private val errorHandler: ErrorHandler
) {

    var phoneNumber = ""

    private var mUnbindDialog: UnbindDialog? = null

    private var unbindingChildId = ""
    private var unbindingDeviceId = ""

    init {
        subscribeViewModel()
    }

    fun showAskUnbindDeviceDialog(childId: String, device: Device, deviceName: String) {

        unbindingChildId = childId
        unbindingDeviceId = device.device_id

        if(isMemberGuardExpired(device.status)) {
            with(baseFragment) {
                showConfirmDialog {
                    message = getString(R.string.unbind_current_device, deviceName)
                    positiveListener = {
                        boundDeviceViewModel.unbindDevice(childUserId = unbindingChildId, childDeviceId = unbindingDeviceId)
                    }
                }
            }
        } else {
            boundDeviceViewModel.sendSmsCode(phoneNumber)
        }
    }

    private fun subscribeViewModel() = with(baseFragment) {
        //发送sms
        boundDeviceViewModel.sendSmsCode
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isLoading -> showLoadingDialog(false)
                        it.isError -> {
                            dismissLoadingDialog()
                            errorHandler.handleError(it.error())
                        }
                        it.isSuccess -> {
                            dismissLoadingDialog()
                            processSendUnbindSmsSuccess()
                        }
                    }
                })

        //解绑
        boundDeviceViewModel.unBindChild
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isLoading -> {
                            showLoadingDialog(false)
                        }
                        it.isError -> {
                            dismissLoadingDialog()
                            mUnbindDialog?.unbindError(it.error().message)
                        }
                        it.isSuccess -> {
                            StatisticalManager.onEvent(UMEvent.ClickEvent.BINDDEVICE_BTN_UNTYING_CONFIRM)
                            dismissLoadingDialog()
                            showMessage(R.string.unbind_child_success)
                            mUnbindDialog?.dismiss()
                            /**解绑后权限弹窗重置*/
                            AppContext.storageManager().stableStorage().putBoolean(buildFlag(deviceId = unbindingDeviceId,isPermissionLose = false), false)
                            AppContext.storageManager().stableStorage().putBoolean(buildFlag(deviceId = unbindingDeviceId,isPermissionLose = true), false)
                        }
                    }
                })
    }

    private fun processSendUnbindSmsSuccess() {
        if (mUnbindDialog == null) {
            val safeContext = baseFragment.context ?: return
            mUnbindDialog = UnbindDialog(safeContext) { code ->
                boundDeviceViewModel.unbindDevice(code, unbindingChildId, unbindingDeviceId)
            }
        }
        mUnbindDialog?.setSentPhoneNumber(phoneNumber)
        mUnbindDialog?.show()
    }


}