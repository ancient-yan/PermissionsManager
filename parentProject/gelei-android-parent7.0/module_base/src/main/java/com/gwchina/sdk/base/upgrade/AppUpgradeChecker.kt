package com.gwchina.sdk.base.upgrade

import android.app.Activity
import android.app.Dialog
import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.android.base.app.BaseKit
import com.android.base.app.ui.LoadingView
import com.android.base.rx.addTo
import com.android.base.rx.subscribeIgnoreError
import com.android.base.utils.android.ResourceUtils
import com.android.base.utils.android.XAppUtils
import com.android.base.utils.android.compat.AndroidVersion
import com.app.base.R
import com.blankj.utilcode.util.AppUtils
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.config.appFileProviderAuthorities
import com.gwchina.sdk.base.data.api.isFlagPositive
import com.gwchina.sdk.base.widget.dialog.AppLoadingView
import com.gwchina.sdk.base.widget.dialog.TipsManager
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import com.yanzhenjie.permission.AndPermission
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.io.File
import java.util.*

/**shared status*/
private var isChecking: Boolean = false

abstract class AppUpgradeChecker {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var appStatusDisposable: Disposable? = null

    protected var isChecked: Boolean = false
        private set

    private var resultReceiver: ResultBroadcastReceiver? = null

    private val upgradeRepository: UpgradeRepository = UpgradeRepository()

    internal val isUpgradeServiceRunning: Boolean
        get() = XAppUtils.isServiceRunning(AppContext.getContext(), UpgradeService::class.java.name)

    private val flag = UUID.randomUUID().toString()

    private var confirmUpdateDialog: Dialog? = null
    private var loadingDialog: AppLoadingView? = null

    protected abstract val activity: Activity?

    private lateinit var appUpgradeInfo: UpgradeData

    abstract fun shouldCheckAppUpgrade(): Boolean
    abstract fun shouldAskUpdateDialog(forced: Boolean, isIgnored: Boolean, upgradeData: UpgradeData): Boolean

    open fun showLatestVersion() {}
    open fun showCheckFail() {}
    open fun onCheckEnd(success: Boolean) {}

    fun checkAppUpgrade() {
        if (shouldCheckAppUpgrade()) {
            realCheck()
        }
    }

    private fun realCheck() {
        isChecking = true

        val appVersionName = AppUtils.getAppVersionName()

        upgradeRepository.appUpdateInfo(appVersionName)
                .doOnTerminate { isChecking = false }
                .subscribe(
                        { updateData ->
                            dismissLoadingDialog()
                            isChecked = true
                            if (updateData.isPresent) {
                                processUpdateInfo(updateData.get())
                            } else {
                                showCheckFail()
                                onCheckEnd(false)
                            }
                        },
                        {
                            dismissLoadingDialog()
                            showCheckFail()
                            onCheckEnd(false)
                        }
                ).addTo(compositeDisposable)
    }

    private fun processUpdateInfo(response: UpgradeResponse) {
        val upgradeData = response.app_version

        if (upgradeData != null && upgradeData.version.isNotEmpty() && upgradeData.update_url.isNotEmpty()) {

            appUpgradeInfo = upgradeData

            val forced = isFlagPositive(upgradeData.is_force)
            if (shouldAskUpdateDialog(forced, upgradeRepository.isIgnored(upgradeData.version), upgradeData)) {
                showUpdateDialog(upgradeData)
            } else {
                onCheckEnd(true)
            }

        } else {
            showLatestVersion()
            onCheckEnd(true)
        }
    }

    private fun showUpdateDialog(upgradeData: UpgradeData) {
        val safeContext = activity

        if (safeContext == null) {
            onCheckEnd(false)
            return
        }

        confirmUpdateDialog?.setOnCancelListener(null)
        confirmUpdateDialog?.dismiss()

        val updateDialog = UpgradeTipsDialog(safeContext)
        confirmUpdateDialog = updateDialog

        val forced = isFlagPositive(upgradeData.is_force)

        updateDialog.setContent(upgradeData.update_title, upgradeData.update_desc, forced) { doUpgrade ->
            if (doUpgrade) {
                doUpdate(upgradeData.update_url, upgradeData.version, forced)
            } else {
                //only happen when not forcing upgrade
                upgradeRepository.saveIgnored(upgradeData.version)
                onCheckEnd(true)
            }
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    private fun doUpdate(loadUrl: String, versionName: String, forced: Boolean) {
        val safeContext = activity ?: return/*never happen*/

        if (forced) {
            initLoadingDialog()?.showLoadingDialog(R.string.check_version_downloading_apk, false)
        }

        var receiver = resultReceiver
        if (receiver == null) {
            receiver = ResultBroadcastReceiver()
            resultReceiver = receiver
            LocalBroadcastManager.getInstance(AppContext.getContext()).registerReceiver(receiver, IntentFilter(UpgradeService.DOWNLOAD_APK_RESULT_ACTION))
        }
        UpgradeService.start(safeContext, flag, loadUrl, versionName, forced)
    }

    protected fun initLoadingDialog(): LoadingView? {
        val safeContext = activity ?: return null
        if (loadingDialog == null) {
            loadingDialog = AppLoadingView(safeContext)
        }
        return loadingDialog
    }

    protected fun dismissLoadingDialog() {
        loadingDialog?.dismissLoadingDialog()
    }

    fun destroy() {
        compositeDisposable.dispose()
        val receiver = resultReceiver
        if (receiver != null) {
            LocalBroadcastManager.getInstance(AppContext.getContext()).unregisterReceiver(receiver)
        }
        dismissLoadingDialog()
        onCheckEnd(true)
    }


    private inner class ResultBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (flag != intent.getStringExtra(UpgradeService.FLAG_KEY) || intent.action != UpgradeService.DOWNLOAD_APK_RESULT_ACTION) {
                return
            }
            if (intent.getBooleanExtra(UpgradeService.IS_SUCCESS, false)) {
                processOnDownloadingFileSuccessful(intent)
            } else {
                processOnDownloadingFileFailed(intent)
            }
        }
    }

    private fun processOnDownloadingFileSuccessful(intent: Intent) {
        dismissLoadingDialog()
        val apkFile = intent.getSerializableExtra(UpgradeService.APK_FILE_KEY) as File
        val isForce = intent.getBooleanExtra(UpgradeService.IS_FORCE, false)

        val safeContext = activity

        if (safeContext != null) {
            showInstallingDialog(apkFile, safeContext, isForce)
        } else {
            appStatusDisposable = BaseKit.get().appState().subscribeIgnoreError { isForeground ->
                if (isForeground) {
                    BaseKit.get().topActivity?.let {
                        showInstallingDialog(apkFile, it, isForce)
                    }
                }
            }
        }
    }

    private fun showInstallingDialog(apkFile: File, context: Context, isForce: Boolean) {
        showConfirmDialog(context) {
            messageId = R.string.check_version_installing_app_tips
            positiveListener = { installApk(AppContext.getContext(), apkFile) }
            negativeListener = {
                appStatusDisposable?.dispose()
                it.dismiss()
                isChecked = true
                onCheckEnd(true)
                upgradeRepository.saveIgnored(appUpgradeInfo.version)
            }
            autoDismiss = false
            if (isForce) {
                noNegative()
            }
        }.setCancelable(false)
    }

    private fun processOnDownloadingFileFailed(intent: Intent) {
        dismissLoadingDialog()
        val isForce = intent.getBooleanExtra(UpgradeService.IS_FORCE, false)
        val safeContext = activity
        isChecked = false

        if (BaseKit.get().isForeground && safeContext != null) {

            showConfirmDialog(safeContext) {
                messageId = R.string.check_version_download_app_fail_retry
                positiveListener = {
                    doUpdate(appUpgradeInfo.update_url, appUpgradeInfo.version, isForce)
                }
                if (isForce) {
                    noNegative()
                } else {
                    negativeListener = {
                        upgradeRepository.saveIgnored(appUpgradeInfo.version)
                        isChecked = true
                        onCheckEnd(true)
                    }
                }
            }.setCancelable(false)

        } else {
            if (!isForce) {
                TipsManager.showMessage(ResourceUtils.getString(R.string.check_version_download_app_fail))
                onCheckEnd(false)
            }
        }
    }

    private fun installApk(context: Context, apkPath: File) {
        Timber.d("installApk")
        if (AndroidVersion.atLeast(26)) {
            //Android8.0未知来源应用安装权限方案
            AndPermission.with(context)
                    .install()
                    .file(apkPath)
                    .onDenied {
                        Timber.d("installApk onDenied")
                    }
                    .onGranted {
                        Timber.d("installApk onGranted")
                    }.start()
        } else {
            //正常安装
            XAppUtils.installApp(context, apkPath, appFileProviderAuthorities())
        }
    }

}

/**静默检查 App 更新*/
open class AppUpgradeSilentChecker : AppUpgradeChecker() {

    override val activity: Activity?
        get() = BaseKit.get().topActivity

    override fun shouldCheckAppUpgrade(): Boolean {
        /*正在检查过*/
        if (isChecking) {
            return false
        }
        /*已经检查过了*/
        if (isChecked) {
            return false
        }
        /*正在下载*/
        if (isUpgradeServiceRunning) {
            return false
        }
        return true
    }

    override fun shouldAskUpdateDialog(forced: Boolean, isIgnored: Boolean, upgradeData: UpgradeData): Boolean {
        return forced || !isIgnored
    }

}

/**显式地检查 App 更新*/
class AppUpgradeObviousChecker(override val activity: Activity, lifecycleOwner: LifecycleOwner) : AppUpgradeChecker() {

    init {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                destroy()
            }
        })
    }

    override fun shouldCheckAppUpgrade(): Boolean {
        if (isChecking) {
            initLoadingDialog()?.showLoadingDialog(R.string.dialog_loading, false)
            return false
        }
        if (isUpgradeServiceRunning) {
            TipsManager.showMessage(activity.getString(R.string.check_version_downloading_apk))
            return false
        }
        initLoadingDialog()?.showLoadingDialog(R.string.dialog_loading, false)
        return true
    }

    override fun shouldAskUpdateDialog(forced: Boolean, isIgnored: Boolean, upgradeData: UpgradeData) = true

    override fun showLatestVersion() {
        TipsManager.showMessage(activity.getString(R.string.check_version_is_least))
    }

    override fun showCheckFail() {
        TipsManager.showMessage(activity.getString(R.string.check_version_update_failed))
    }

}