package com.gwchina.parent.binding.presentation

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.kotlin.gone
import com.android.base.kotlin.onDismiss
import com.android.base.kotlin.visible
import com.android.sdk.net.exception.ApiErrorException
import com.android.sdk.qrcode.QRCodeView
import com.android.sdk.qrcode.zxing.ZXingView
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.binding.BindingNavigator
import com.gwchina.sdk.base.app.InjectorBaseStateFragment
import com.gwchina.sdk.base.data.exception.DeviceAlreadyBoundException
import com.gwchina.sdk.base.data.exception.DeviceNotSupportException
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showTipsDialog
import kotlinx.android.synthetic.main.binding_bind_error.*
import kotlinx.android.synthetic.main.binding_fragment_scanner.*
import javax.inject.Inject


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.co
 *      Date : 2018-11-26 14:58
 */
class ScannerFragment : InjectorBaseStateFragment() {

    @Inject
    lateinit var bindingNavigator: BindingNavigator

    private val bindingViewModel by lazy {
        getViewModelFromActivity<BindingViewModel>(viewModelFactory)
    }

    private lateinit var zXingView: ZXingView

    private var isDeviceAlreadyBoundError = false

    private lateinit var scannerHelper: ScannerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.binding_fragment_scanner

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)

        scannerHelper = ScannerHelper(this)

        zXingView = scannerZXingView

        zXingView.setDelegate(object : QRCodeView.Delegate {
            override fun onScanQRCodeSuccess(result: String) {
                scannerHelper.parseResult(result)
            }

            override fun onScanQRCodeOpenCameraError(error: Exception) {
                scannerHelper.showQrCodeError()
            }
        })
    }

    override fun onStart() {
        super.onStart()
        zXingView.startSpotAndShowRect()
    }

    override fun onStop() {
        super.onStop()
        zXingView.stopCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        zXingView.onDestroy()
    }

    private fun subscribeViewModel() {
        bindingViewModel.bindingResult
                .observe(this, Observer {
                    it ?: return@Observer
                    when {
                        it.isLoading -> {
                            showLoadingDialog(R.string.device_binding_loading_tips, true)
                        }
                        it.isError -> {
                            dismissLoadingDialog()
                            if (it.error() is DeviceAlreadyBoundException) {
                                showErrorLayout()
                                setErrorStatusDeviceAlreadyBound(R.string.device_already_bound_tips)
                            } else if (it.error() is DeviceNotSupportException) {
                                processErrorWithStatus(it.error())
                                setErrorStatusNormal(errorHandler.createMessage(it.error()), R.string.page_error_retry_scan)
                            } else if (it.error() is ApiErrorException) {
                                val code = (it.error() as ApiErrorException).code
                                processErrorWithStatus(it.error())
                                if (code == -1) {
                                    setErrorStatusDeviceAlreadyBound(R.string.no_identify_qr_code_tips)
                                } else {
                                    setErrorStatusNormal(errorHandler.createMessage(it.error()), R.string.page_error_retry_action_tips)
                                }
                            } else {
                                processErrorWithStatus(it.error())
                                setErrorStatusNormal(errorHandler.createMessage(it.error()), R.string.page_error_retry_action_tips)
                            }
                        }
                        it.isSuccess -> {
                            dismissLoadingDialog()
                            showBindingSuccess(it.data())
                        }
                    }
                })
    }

    private fun setErrorStatusNormal(errorMessage: CharSequence, errorAction: Int) {
        stateLayoutConfig
                .setStateMessage(ERROR, errorMessage)
                .setStateAction(ERROR, getString(errorAction))
        btnBindingRetry?.gone()
        isDeviceAlreadyBoundError = false
    }

    private fun setErrorStatusDeviceAlreadyBound(message: Int) {
        stateLayoutConfig
                .setStateMessage(ERROR, getString(message))
                .setStateAction(ERROR, getString(R.string.i_got_it))

        isDeviceAlreadyBoundError = true
        btnBindingRetry.visible()
        btnBindingRetry.setOnClickListener {
            showContentLayout()
            startScan()
        }
    }

    override fun onRetry(state: Int) {
        if (isDeviceAlreadyBoundError) {
            exitFragment()
        } else {
            showContentLayout()
            startScan()
        }
    }


    private fun showBindingSuccess(bindingResult: BindingResult) {
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_BIND_SUCCESS)
        showTipsDialog {
            messageId = R.string.binding_device_success_tips
            type = success()
        }.onDismiss {
            exitAndReturnInfo(bindingResult)
        }
    }

    private fun exitAndReturnInfo(bindingResult: BindingResult) {
        val intent = Intent()
        intent.putExtra(RouterPath.Binding.DEVICE_ID_KEY, bindingResult.deviceSn)
        activity?.setResult(Activity.RESULT_OK, intent)
        activity?.supportFinishAfterTransition()
    }

    fun stopScan() {
        zXingView.stopSpot()
    }

    fun startScan() {
        if (isResumed) {
            zXingView.startSpotAndShowRect()
        }
    }

    fun startBind(childDeviceInfo: DeviceInfo) {
        bindingViewModel.bindDevice(childDeviceInfo)
    }

}