package com.gwchina.parent.launcher

import android.arch.lifecycle.Lifecycle
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.imageloader.DisplayConfig
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.imageloader.Source
import com.android.base.permission.AutoPermissionRequester
import com.android.base.rx.observeOnUI
import com.android.base.rx.subscribed
import com.android.base.utils.android.DevicesUtils
import com.android.base.utils.android.compat.SystemBarCompat
import com.blankj.utilcode.util.AppUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.AppBaseActivity
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.config.AppSettings.NEED_MIGRATION_FLAG
import com.gwchina.sdk.base.data.api.AD_TYPE_LAUNCH
import com.gwchina.sdk.base.data.app.AdvertisingFilter
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Advertising
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.router.RouterPath.Launcher.AD_KEY
import com.gwchina.sdk.base.utils.GwDevices
import com.yanzhenjie.permission.runtime.Permission
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.launcher_activity.*
import timber.log.Timber

/**
 * 启动页
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-22 16:03
 */
@Route(path = RouterPath.Launcher.PATH)
class LauncherActivity : AppBaseActivity() {

    lateinit var appDataSource: AppDataSource

    companion object {
        private const val LAUNCHER_MIN_SHOW_TIME = 1000

        private const val LAUNCHER_FIRST_LAUNCHING = "app_is_first_launching_in_launcher"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        hideSystemUi()
        super.onCreate(savedInstanceState)
    }

    private val handler = Handler()

    private val openMainRunnable = Runnable {
        openMain()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    private val advertisingFilter = object : AdvertisingFilter {
        override fun filter(ad: Advertising): Boolean {
            return ad.ad_type == AD_TYPE_LAUNCH
        }
    }

    private fun loadAdvertising() {
        appDataSource.advertisingList(advertisingFilter, true)
                .observeOnUI()
                .autoDispose(Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    val list = it.orElse(null)
                    if (!list.isNullOrEmpty()) {
                        openAd(list[0])
                    } else {
                        openMainPage()
                    }
                }, {
                    openMainPage()
                })
    }


    private fun hideSystemUi() {
        try {
            SystemBarCompat.setTranslucentSystemUi(this, true, true)
            val systemUiVisibility = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = systemUiVisibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun layout() = R.layout.launcher_activity

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        ImageLoaderFactory.getImageLoader().display(ivLauncher, Source.create(R.drawable.launcher_img_bg), DisplayConfig.create().scaleType(DisplayConfig.SCALE_CENTER_CROP))
        requestNecessaryPermission()
    }

    private fun requestNecessaryPermission() {
        AutoPermissionRequester.with(this)
                .permission(Permission.READ_PHONE_STATE, Permission.WRITE_EXTERNAL_STORAGE)
                .onDenied { start() }
                .onGranted { start() }
                .request()
    }

    private fun start() {
        appDataSource = AppContext.appDataSource()
        if (appDataSource.deviceId().isEmpty()) {
            Log.e("hujl", "upload device_sn");
            Observable.create<String> {
                val deviceSN = GwDevices.getGwDeviceSN()
                it.onNext(deviceSN)
            }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                appDataSource.uploadDevice(
                        it,
                        DevicesUtils.getModel(),
                        AppUtils.getAppVersionName())
                        .subscribed()
                handler.postDelayed(openMainRunnable, LAUNCHER_MIN_SHOW_TIME.toLong())
            }, {
                handler.postDelayed(openMainRunnable, LAUNCHER_MIN_SHOW_TIME.toLong())
            });
        } else {
            Log.e("hujl", "缓存中有不用上传" + appDataSource.deviceId());
            handler.postDelayed(openMainRunnable, LAUNCHER_MIN_SHOW_TIME.toLong())
        }
    }

    private fun openMain() {
        when {
            AppSettings.settingsStorage().getBoolean(NEED_MIGRATION_FLAG, false) -> {
                AppContext.appRouter().build(RouterPath.Migration.PATH).navigation()
                supportFinishAfterTransition()
            }
            AppSettings.isFirst(LAUNCHER_FIRST_LAUNCHING) -> {
                startActivity(Intent(this, GuideActivity::class.java))
                supportFinishAfterTransition()
            }
            else -> {
                loadAdvertising()
            }
        }
    }

    private fun openAd(ad: Advertising) {
        AppContext.appRouter().build(RouterPath.Launcher.AD_PATH).withParcelable(AD_KEY, ad).navigation()
        supportFinishAfterTransition()
    }

    private fun openMainPage() {
        AppContext.appRouter().build(RouterPath.Main.PATH).navigation()
        supportFinishAfterTransition()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

}