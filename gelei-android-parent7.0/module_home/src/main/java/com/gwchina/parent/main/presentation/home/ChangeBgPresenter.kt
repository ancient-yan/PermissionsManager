package com.gwchina.parent.main.presentation.home

import android.content.Intent
import android.support.v4.app.Fragment
import android.widget.TextView
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.android.base.permission.AutoPermissionRequester
import com.android.base.rx.observeOnUI
import com.android.sdk.mediaselector.CropOptions
import com.android.sdk.mediaselector.MediaSelector
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import com.gwchina.sdk.base.widget.dialog.showBottomSheetListDialog
import com.yanzhenjie.permission.runtime.Permission
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-10-14 11:28
 *      更换首页背景图
 */
class ChangeBgPresenter(val host: Fragment, private val tvChangeBg: TextView?) {

    private val ms = MediaSelector(host, object : MediaSelector.Callback {

        override fun onTakePictureSuccess(picture: String?) {
            picture?.let { uploadImage(it) }
        }
    })

    fun showBottomDialog() {
        host.showBottomSheetListDialog {
            spaceViewGone = true
            items = listOf(host.getString(R.string.home_change_bg))
            actionTextId = R.string.cancel_
            itemSelectedListener = { position, _ ->
                if (position == 0) {
                    addPic()
                }
            }
            actionListener = {
                StatisticalManager.onEvent(UMEvent.ClickEvent.HOMEPAGE_BTN_CHANGEPHOTO_NO)
            }
        }
    }

    private fun addPic() {
        AutoPermissionRequester.with(host)
                .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted {
                    val cropOptions = CropOptions().setAspectX(375).setAspectY(240).setOutputX(ScreenUtils.getScreenWidth()).setOutputY((ScreenUtils.getScreenWidth() * (240.0f / 375)).toInt())
                    ms.takeSinglePictureWithCrop(false, cropOptions)
                }.request()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ms.onActivityResult(requestCode, resultCode, data)
    }

    fun uploadImage(imgPath: String) {
        if (host is HomeFragment) {
            changeBgInterval()
            //上传到文件服务器
            host.viewModel.mainRepository.serviceManager.newFileUploadService().uploadFiles(listOf(imgPath)).observeOnUI().subscribe(
                    { fileInfoList ->
                        host.viewModel.mainRepository.upHomeTopImg(AppContext.appDataSource().user().currentChild?.child_user_id
                                ?: "", fileInfoList[0].url).observeOnUI().subscribe({
                            host.viewModel.refreshHomePage()
                            disposable()
                        }, {
                            host.showMessage(it.message)
                            disposable()
                        })
                    },
                    {
                        host.showMessage("上传失败")
                        disposable()
                    }
            )
        }
    }

    var disposable: Disposable? = null

    private fun changeBgInterval() {
        tvChangeBg?.visible()
        disposable = Observable.interval(0, 300, TimeUnit.MILLISECONDS).observeOnUI()
                .subscribe {
                    Timber.e(it.toString())
                    when (it.rem(3)) {
                        0L -> {
                            tvChangeBg?.text = "更换中."
                        }
                        1L -> {
                            tvChangeBg?.text = "更换中.."
                        }
                        2L -> {
                            tvChangeBg?.text = "更换中..."
                        }
                    }
                }
    }

    private fun disposable() {
        disposable?.dispose()
        disposable = null
        tvChangeBg?.gone()
    }

}