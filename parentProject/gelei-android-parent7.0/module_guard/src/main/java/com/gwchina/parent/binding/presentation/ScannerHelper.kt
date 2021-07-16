package com.gwchina.parent.binding.presentation

import android.arch.lifecycle.Lifecycle
import android.support.annotation.StringRes
import com.android.base.app.BaseKit
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.android.base.receiver.NetworkState
import com.android.base.rx.bindLifecycle
import com.android.base.rx.subscribeIgnoreError
import com.gwchina.lssw.parent.guard.R
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.binding_fragment_scanner.*
import timber.log.Timber

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-28 11:45
 */
class ScannerHelper(private val host: ScannerFragment) {

    init {
        BaseKit.get().networkState()
                .observeOn(AndroidSchedulers.mainThread())
                .bindLifecycle(host, Lifecycle.Event.ON_DESTROY)
                .subscribeIgnoreError {
                    processNetworkState(it)
                }
    }

    fun parseResult(result: String) {
        Timber.d("scan result result = $result")

        val childDeviceInfo = DeviceInfo.fromUrl(result)

        if (childDeviceInfo == null) {
            Timber.d("showQrCodeError")
            showQrCodeError()
            //扫描到错误二维码，重新开始
            host.startScan()
        } else {
            Timber.d("hideQrCodeError")
            //扫描正确的二维码，开始绑定，隐藏错误提示
            host.startBind(childDeviceInfo)
            hideTips()
        }
    }

    fun showQrCodeError() {
        showTips(R.string.qrcode_error)
    }

    private fun showTips(@StringRes tisRes: Int) {
        host.scanTvErrorTips?.visible()
        host.scanTvErrorTips?.setText(tisRes)
    }

    private fun hideTips() {
        host.scanTvErrorTips?.invisible()
        host.scanTvErrorTips?.text = ""
    }

    private fun processNetworkState(it: NetworkState) {
        if (it.isConnected) {
            host.startScan()
            hideTips()
        } else {
            host.stopScan()
            showTips(R.string.page_error_no_network)
        }
    }

}