package com.gwchina.sdk.base.app

import android.app.Dialog
import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import com.android.base.app.BaseKit
import com.android.sdk.net.errorhandler.ErrorMessageFactory
import com.app.base.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.ApiHelper
import com.gwchina.sdk.base.data.api.isLoginExpired
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.widget.dialog.TipsManager
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog
import java.lang.ref.WeakReference

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-08 16:02
 */
interface ErrorHandler {

    /** 根据异常，生成一个合理的错误提示 */
    fun createMessage(throwable: Throwable): CharSequence

    /** 直接处理异常，比如根据 [createMessage] 方法生成的消息弹出一个 toast。 */
    fun handleError(throwable: Throwable)

    /** 直接处理异常，自定义消息处理*/
    fun handleError(throwable: Throwable, processor: ((CharSequence) -> Unit))

    /**处理全局异常，此方法仅由数据层调用，用于统一处理全局异常*/
    fun handleGlobalError(code: Int)

}

internal class AppErrorHandler : ErrorHandler {

    private var showingDialog: WeakReference<Dialog>? = null

    override fun createMessage(throwable: Throwable): CharSequence {
        return ErrorMessageFactory.createMessage(throwable)
    }

    override fun handleError(throwable: Throwable) {
        handleError(throwable) {
            TipsManager.showMessage(it)
        }
    }

    override fun handleError(throwable: Throwable, processor: (CharSequence) -> Unit) {
        if (!throwable.isLoginExpired()) {
            processor(createMessage(throwable))
        }
    }

    override fun handleGlobalError(code: Int) {
        if (AppContext.appDataSource().userLogined()) {
            AppContext.schedulerProvider().ui().scheduleDirect {
                showReLoginDialog(code)
            }
        }
    }

    private fun showReLoginDialog(code: Int): Boolean {
        val currentActivity = BaseKit.get().topActivity ?: return false

        val dialog = showingDialog?.get()

        if (dialog != null) {
            return true
        }

        val showConfirmDialog = currentActivity.showConfirmDialog {
            messageId = getExpiredMessage(code)
            noNegative()
            negativeListener = { showingDialog = null }
            positiveListener = {
                showingDialog = null
                AppContext.appRouter().build(RouterPath.Main.PATH)
                        .withInt(RouterPath.Main.ACTION_KEY, RouterPath.Main.ACTION_RE_LOGIN)
                        .navigation()
            }
        }

        showConfirmDialog.setCancelable(false)
        showConfirmDialog.setOnDismissListener {
            showingDialog = null
        }

        showingDialog = WeakReference(showConfirmDialog)

        /*activity maybe finish by programming*/
        if (currentActivity is LifecycleOwner) {
            currentActivity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onDestroy(owner: LifecycleOwner) {
                    showingDialog?.get()?.dismiss()
                    showingDialog = null
                }
            })
        }

        return true
    }

    private fun getExpiredMessage(code: Int): Int {
        return if (ApiHelper.isLoginExpired(code)) {
            R.string.login_expired_re_login_tips
        } else {
            R.string.sso_re_login_tips
        }
    }

}