package com.gwchina.parent.main.presentation.home

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import com.android.base.app.BaseKit
import com.android.base.imageloader.DisplayConfig
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.onDismiss
import com.android.base.rx.observeOnUI
import com.blankj.utilcode.util.ActivityUtils
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.AD_TYPE_DIALOG
import com.gwchina.sdk.base.data.app.AdvertisingFilter
import com.gwchina.sdk.base.data.models.Advertising
import com.gwchina.sdk.base.linker.SchemeJumper
import com.gwchina.sdk.base.utils.SequentialUITaskExecutor
import com.gwchina.sdk.base.utils.UITask
import com.gwchina.sdk.base.utils.getWeeOfToday
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.home_dialog_adversiting.*
import timber.log.Timber

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-09-03 11:10
 */
class MainAdvertisingDialogTask : UITask(
        MAIN_ADVERTISING_DIALOG_TASK_ID,
        SequentialUITaskExecutor.SCHEDULE_POLICY_TILL_SUCCESS) {

    companion object {
        private const val MAIN_ADVERTISING_DIALOG_TASK_ID = "main_advertising_dialog_task_id"
        private const val MAIN_ADVERTISING_DIALOG_SHOW_FLAG = "main_advertising_dialog_is_showed_flag"
    }

    private val advertisingFilter = object : AdvertisingFilter {
        override fun filter(ad: Advertising): Boolean {
            return ad.ad_type == AD_TYPE_DIALOG
        }
    }

    private var advertising: Advertising? = null

    override fun run() {
        val stableStorage = AppContext.storageManager().stableStorage()
        if (stableStorage.getBoolean(buildFlag(), false)) {
            finished(true)
            return
        }

        val safeAdvertising = advertising
        if (safeAdvertising != null) {

            return
        }

        AppContext.appDataSource().advertisingList(advertisingFilter, true)
                .observeOnUI()
                .subscribe(
                        {
                            processResult(it)
                        },
                        {
                            finished(false)
                        }
                )
    }

    private fun processResult(optionalData: Optional<List<Advertising>>) {
        if (optionalData.orElse(null).isNullOrEmpty()) {
            finished(true)
            return
        }

        val advertising = optionalData.get()[0]
        this.advertising = advertising
        showAd(advertising)
    }

    private fun showAd(advertising: Advertising) {
        val topActivity = BaseKit.get().topActivity
        if (topActivity == null) {
            finished(false)
            return
        }
        val mainAdvertisingDialog = MainAdvertisingDialog(topActivity)
        mainAdvertisingDialog.showAdvertising(advertising)
        AppContext.storageManager().stableStorage().putBoolean(buildFlag(), true)

        mainAdvertisingDialog.onDismiss {
            finished(true)
        }

        if (topActivity is LifecycleOwner) {
            topActivity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    finished(true)
                }
            })
        }

    }

    private fun buildFlag(): String {
        return "${MAIN_ADVERTISING_DIALOG_SHOW_FLAG}${getWeeOfToday()}"
    }

}

private class MainAdvertisingDialog(context: Context) : BaseDialog(context) {

    init {
        setContentView(R.layout.home_dialog_adversiting)
    }

    fun showAdvertising(advertising: Advertising) {
        show()
        setCanceledOnTouchOutside(false)

        tvHomeAdvertising.setOnClickListener {
            SchemeJumper.handleSchemeJump(context, advertising.jump_target
                    ?: "", advertising.jump_args)
            dismiss()
        }

        ivHomeDialogClose.setOnClickListener {
            dismiss()
        }

        Timber.d("tvHomeAdvertising ${tvHomeAdvertising.measuredWidth},${tvHomeAdvertising.measuredHeight}")

        tvHomeAdvertising.postDelayed({
            Timber.d("tvHomeAdvertising ${tvHomeAdvertising.measuredWidth},${tvHomeAdvertising.measuredHeight}")
            /**
             * fixed:
             * java.lang.IllegalArgumentException: You cannot start a load for a destroyed activity
            at com.bumptech.glide.manager.RequestManagerRetriever.assertNotDestroyed(RequestManagerRetriever.java:323)
             */
            if (!ActivityUtils.getActivityByContext(tvHomeAdvertising.context).isFinishing)
                ImageLoaderFactory.getImageLoader().display(tvHomeAdvertising, advertising.ad_photo_url, DisplayConfig.create().scaleType(DisplayConfig.SCALE_CENTER_CROP))
        }, 1000)

    }

}