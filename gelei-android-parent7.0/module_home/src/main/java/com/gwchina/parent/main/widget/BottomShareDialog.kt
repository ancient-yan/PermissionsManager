package com.gwchina.parent.main.widget

import android.support.v7.app.AppCompatDialog
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.android.base.kotlin.dip
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.presentation.location.ChildLocationFragment
import com.gwchina.sdk.base.data.api.SEX_FEMALE
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.third.share.SharePlatform
import com.gwchina.sdk.base.third.share.ShareURL
import com.gwchina.sdk.base.third.share.SocialShareUtils
import com.gwchina.sdk.base.widget.dialog.TipsManager
import kotlinx.android.synthetic.main.home_share_dialog_layout.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-30 14:04
 */

@Suppress("IMPLICIT_CAST_TO_ANY")
class BottomShareDialog(val host: ChildLocationFragment, lat: Double, lon: Double, address: String?, child: Child?) : AppCompatDialog(host.requireContext(), R.style.Theme_Dialog_Common_Transparent) {

    //https://lbs.amap.com/api/uri-api/guide/mobile-web/point
    val path = "https://uri.amap.com/marker?position=$lon,$lat&src=mypage&coordinate=gaode&callnative=1"

    private val shareListener = ShareListenerImp()

    private val thumbData by lazy {
        if (!child?.head_photo_path.isNullOrEmpty()) child?.head_photo_path else {
            if (child?.sex == SEX_FEMALE) {
                R.drawable.img_head_girl_50
            } else {
                R.drawable.img_head_boy_50
            }
        }
    }

    init {
        setContentView(R.layout.home_share_dialog_layout)
        setCancelable(false)
        setCanceledOnTouchOutside(true)
        fixDialogHeight()
        cancel.setOnClickListener { dismiss() }
        wechat.setOnClickListener {
            val shareURL = ShareURL(SharePlatform.WeChat, path, "“${child?.nick_name}”的位置", address
                    ?: "", thumbData)
            SocialShareUtils.share(host.activity, shareURL, shareListener)
            dismiss()
        }

        qq.setOnClickListener {
            //title不能为空，否则报错
            val shareURL = ShareURL(SharePlatform.QQ, path, "“${child?.nick_name}”的位置", address
                    ?: "", thumbData)
            SocialShareUtils.share(host.activity, shareURL, shareListener)
            dismiss()
        }
    }

    private fun fixDialogHeight() {

        window?.let {
            // 在底部，宽度撑满
            val params = it.attributes
            params.windowAnimations = com.app.base.R.style.Style_Anim_Bottom_In
            params.gravity = Gravity.BOTTOM
            params.height = dip(178)
            params.width = ScreenUtils.getScreenWidth()
            it.attributes = params
        }
    }

    override fun show() {
        //https://stackoverflow.com/questions/23520892/unable-to-hide-navigation-bar-during-alertdialog-logindialog
        fixSystemNavigator()
        //real show
        super.show()
    }

    private fun fixSystemNavigator() {
        window?.run {
            setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        }
    }


    inner class ShareListenerImp : SocialShareUtils.ShareListener {

        override fun onStart() {
            host.showLoadingDialog()
        }

        override fun onSuccess() {
//            TipsManager.showMessage("分享成功")
            host.dismissLoadingDialog()
        }

        override fun onFailed() {
            TipsManager.showMessage("分享失败")
            host.dismissLoadingDialog()
        }

        override fun onCancel() {
//            TipsManager.showMessage("分享取消")
            host.dismissLoadingDialog()
        }

    }
}