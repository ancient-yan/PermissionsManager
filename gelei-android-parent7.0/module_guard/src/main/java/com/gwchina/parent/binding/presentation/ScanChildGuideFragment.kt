package com.gwchina.parent.binding.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.android.base.app.dagger.Injectable
import com.android.base.kotlin.dip
import com.android.base.permission.AutoPermissionRequester
import com.android.sdk.qrcode.zxing.QRCodeEncoder
import com.blankj.utilcode.util.ToastUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.binding.BindingNavigator
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.DataContext
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.web.WebUtils
import com.yanzhenjie.permission.runtime.Permission
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.binding_fragment_scan_guide.*
import javax.inject.Inject


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-26 14:31
 */
class ScanChildGuideFragment : InjectorBaseFragment(), Injectable {

    companion object {
        /*分享到孩子的链接,下载孩子端链接*/
        private const val DOWNLOAD_CHILD_APP_PATH = "/gelei-guard-h5/share/downloads.html#/children-app-install-guide"

        private val DOWNLOAD_CHILD_APP_URL = getUrl(DOWNLOAD_CHILD_APP_PATH)

        private fun getUrl(path: String): String {
            return if (path.startsWith("/")) {
                WebUtils.removePath(DataContext.getInstance().baseWebUrl()) + path
            } else {
                DataContext.getInstance().baseWebUrl() + path
            }
        }
    }

    @Inject
    lateinit var bindingNavigator: BindingNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.binding_fragment_scan_guide

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_BIND)
        btnBindingStart.setOnClickListener {
            checkPermission()
        }

        tvBindingSupportedDeviceType.setOnClickListener {
            bindingNavigator.openSupportedDevices()
            StatisticalManager.onEvent(UMEvent.ClickEvent.PAGE_BIND_BTN_LIST)
        }

        tvShare.setOnClickListener {
            val shareTitle = getString(R.string.click_download_child)
            val shareDescription = getString(R.string.child_guard_grow_up)
            val sharePicture = R.drawable.ic_child_launcher
            BottomShareDialog(this@ScanChildGuideFragment, shareTitle, shareDescription, DOWNLOAD_CHILD_APP_URL, sharePicture).show()
        }
        createQRCode()
    }

    /**
     * 生成二维码
     */
    private fun createQRCode() {
        Observable.create<Bitmap> {
            val result = QRCodeEncoder.syncEncodeQRCode(DOWNLOAD_CHILD_APP_URL, dip(150), Color.BLACK, BitmapFactory.decodeResource(resources, R.drawable.ic_child_launcher))
            it.onNext(result)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDispose()
                .subscribe({
                    ivBindingChildAppIcon.setImageBitmap(it)
                }, {
                    ToastUtils.showLong("二维码生成失败！")
                }, {
                })
    }

    private fun checkPermission() {
        AutoPermissionRequester.with(this)
                .permission(Permission.CAMERA)
                .customUI(ScanPermissionDialogProvider())
                .onGranted {
                    bindingNavigator.openScannerPage()
                    StatisticalManager.onEvent(UMEvent.ClickEvent.PAGE_BIND_BTN_SCAN)
                }
                .request()
    }

    override fun onResume() {
        super.onResume()
        dismissLoadingDialog()
    }

    private fun subscribeViewModel() {

    }

}