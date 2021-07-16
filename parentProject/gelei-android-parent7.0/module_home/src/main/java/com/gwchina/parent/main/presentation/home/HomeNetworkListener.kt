package com.gwchina.parent.main.presentation.home

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import android.view.ViewGroup
import com.android.base.app.BaseKit
import com.android.base.kotlin.visible
import com.android.base.rx.bindLifecycle
import com.android.base.rx.subscribeIgnoreError
import com.android.base.utils.android.ViewUtils
import com.android.base.utils.android.compat.AndroidVersion
import com.android.base.utils.android.compat.SystemBarCompat
import com.blankj.utilcode.util.NetworkUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import kotlinx.android.synthetic.main.home_fragment.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.min

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-29 20:10
 */
internal fun newNetworkListener(fragment: HomeFragment, onNetRecover: () -> Unit): NetworkListener {
    return if (AndroidVersion.atLeast(19)) {
        NetworkListener19Impl(fragment, onNetRecover)
    } else {
        throw IllegalStateException()
    }
}

internal abstract class NetworkListener(private val host: HomeFragment, private val onNetRecover: () -> Unit) {

    private var isNetErrorShow = false
    protected lateinit var netErrorView: View
    protected lateinit var contentView: SwipeRefreshLayout
    protected lateinit var statusView: View

    private val netStatus = MutableLiveData<Boolean>()

    fun start() {
        //初始化view
        if (initViews()) {
            //监听LiveData
            listenerLiveData()
            //监听状态
            listenNetStatus()
        }
    }

    private fun initViews(): Boolean {
        val netErrorViewNullable: View? = host.flHomeNetError
        val contentViewNullable: SwipeRefreshLayout? = host.view?.findViewById(R.id.base_refresh_layout)
        val statusViewNullable: View? = host.clHomeTopChildInfo

        if (netErrorViewNullable == null || contentViewNullable == null || statusViewNullable == null) {
            return false
        }

        netErrorView = netErrorViewNullable
        contentView = contentViewNullable
        statusView = statusViewNullable

        //先隐藏
        if (netErrorView.measuredHeight == 0) {
            ViewUtils.measureWithScreenSize(netErrorView)
        }

        netErrorView.setTopMargin(-netErrorView.measuredHeight)
        netErrorView.visible()

        return true
    }

    @SuppressLint("MissingPermission")
    private fun listenerLiveData() {
        netStatus.observe(host, Observer { connected ->
            connected ?: return@Observer
            Timber.d("isNetErrorShow = $isNetErrorShow connected = $connected")
            if (!isNetErrorShow && !connected && !NetworkUtils.isConnected()) {
                setNetErrorTipsVisible(true)
                isNetErrorShow = true
            } else if (isNetErrorShow && connected) {
                onNetRecover()
                setNetErrorTipsVisible(false)
                isNetErrorShow = false
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun listenNetStatus() {
        BaseKit.get().networkState()
                .throttleLast(2000, TimeUnit.MILLISECONDS)
                .bindLifecycle(host, Lifecycle.Event.ON_DESTROY)
                .subscribeIgnoreError {
                    netStatus.postValue(it.isConnected)
                }
    }

    private fun setNetErrorTipsVisible(visible: Boolean) {
        if (visible) {
            show()
        } else {
            hide()
        }
    }

    abstract fun show()
    abstract fun hide()
}

private fun View.setTopMargin(topMargin: Int) {
    val params = this.layoutParams as ViewGroup.MarginLayoutParams
    params.topMargin = topMargin
    layoutParams = params
}

private class NetworkListener19Impl(host: HomeFragment, onNetRecover: () -> Unit) : NetworkListener(host, onNetRecover) {

    private val preScrollY = SystemBarCompat.getStatusBarHeight(AppContext.getContext())

    private var animator: ValueAnimator? = null

    override fun show() {
        animator?.cancel()
        val height = netErrorView.measuredHeight
        val valueAnimator = ValueAnimator.ofInt(-height, 0)
        animator = valueAnimator

        valueAnimator.setDuration(500)
                .addUpdateListener {
                    val offset = it.animatedValue as Int
                    netErrorView.setTopMargin(offset)
                    val absOffset = height - abs(offset)
                    if (absOffset >= preScrollY) {
                        contentView.setTopMargin(absOffset - preScrollY)
                        statusView.setTopMargin(absOffset - preScrollY)
                    }
                }

        valueAnimator.start()
    }

    override fun hide() {
        animator?.cancel()
        val height = netErrorView.measuredHeight
        val contentScroll = height - preScrollY
        val valueAnimator = ValueAnimator.ofInt(0, -height)
        animator = valueAnimator

        valueAnimator.setDuration(500)
                .addUpdateListener {
                    val offset = it.animatedValue as Int
                    netErrorView.setTopMargin(offset)
                    val absOffset = min(abs(offset), contentScroll)
                    contentView.setTopMargin(contentScroll - absOffset)
                    statusView.setTopMargin(contentScroll - absOffset)
                }

        valueAnimator.start()
    }

}