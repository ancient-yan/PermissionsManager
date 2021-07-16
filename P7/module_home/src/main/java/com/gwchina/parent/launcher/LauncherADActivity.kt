package com.gwchina.parent.launcher

import android.arch.lifecycle.Lifecycle
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.imageloader.DisplayConfig
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.visible
import com.android.base.rx.observeOnUI
import com.android.base.utils.android.compat.SystemBarCompat
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.AppBaseActivity
import com.gwchina.sdk.base.data.models.Advertising
import com.gwchina.sdk.base.linker.SchemeJumper
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.launcher_ad_activity.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * 启动页广告
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-22 16:03
 */
@Route(path = RouterPath.Launcher.AD_PATH)
class LauncherADActivity : AppBaseActivity() {

    @JvmField
    @Autowired(name = RouterPath.Launcher.AD_KEY)
    var ad: Advertising? = null

    private var disposable: Disposable? = null

    override fun onResume() {
        super.onResume()
        hideSystemUi()
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

    override fun layout() = R.layout.launcher_ad_activity

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        ad?.let { showAdvertising(it) }
    }

    private fun showAdvertising(ad: Advertising) {
        intervalTime()
        ImageLoaderFactory.getImageLoader().display(ivLauncher, ad.ad_photo_url, DisplayConfig.create().scaleType(DisplayConfig.SCALE_CENTER_CROP))
        ivLauncher.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.BTN_CLICKAD)
            openMain()
            SchemeJumper.handleSchemeJump(this, ad.jump_target?:"", ad.jump_args)
        }
        tvLauncherSkip.visible()
        tvLauncherSkip.setOnClickListener {
            StatisticalManager.onEvent(UMEvent.ClickEvent.BTN_SKIPAD)
            openMain()
        }
    }

    private fun openMain() {
        AppContext.appRouter().build(RouterPath.Main.PATH).navigation()
        supportFinishAfterTransition()
    }

    private fun intervalTime() {
        val count = 3L
        disposable = Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(count + 1)
                .map {
                    count - it
                }.observeOnUI()
                .autoDispose(Lifecycle.Event.ON_DESTROY)
                .subscribe({
                    tvLauncherSkip.text = String.format(getString(R.string.launcher_skip_mask, it))
                }, {}, {
                    openMain()
                })
    }
}