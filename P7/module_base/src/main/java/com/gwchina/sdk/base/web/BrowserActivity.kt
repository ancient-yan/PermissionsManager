package com.gwchina.sdk.base.web

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.webkit.URLUtil
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.utils.android.compat.SystemBarCompat
import com.app.base.R
import com.gwchina.sdk.base.app.AppBaseActivity
import com.gwchina.sdk.base.data.DataContext
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import timber.log.Timber

/**
 * 应用内浏览器
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-01 11:29
 */
@Route(path = RouterPath.Browser.PATH)
class BrowserActivity : AppBaseActivity() {

    @JvmField
    @Autowired(name = RouterPath.Browser.FRAGMENT_KEY)
    var fragmentClass: String? = null

    @JvmField
    @Autowired(name = RouterPath.Browser.SHOW_HEADER_KEY)
    var showHeader: Boolean = true

    @JvmField
    @Autowired(name = RouterPath.Browser.URL_KEY)
    var targetUrl: String? = null

    @JvmField
    @Autowired(name = RouterPath.Browser.ARGUMENTS_KEY)
    var bundle: Bundle? = null

    @JvmField
    @Autowired(name = RouterPath.Browser.JS_CALL_INTERCEPTOR_CLASS_KEY)
    var customJsCallInterceptor: String? = null

    @JvmField
    @Autowired(name = RouterPath.Browser.CACHE_ENABLE)
    var cacheEnable: Boolean = false

    @JvmField
    @Autowired(name = RouterPath.Browser.LOAD_NO_CACHE_ENABLE)
    var loadNoCacheEnable: Boolean = false

    @JvmField
    @Autowired(name = RouterPath.Browser.WEB_TITLE)
    var customeTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("fragmentClass = $fragmentClass")
        Timber.d("showHeader = $showHeader")
        Timber.d("targetUrl = $targetUrl")
        Timber.d("bundle = $bundle")
        Timber.d("customJsCallInterceptor = $customJsCallInterceptor")
        Timber.d("cacheEnable = $cacheEnable")
        Timber.d("loadNoCacheEnable = $loadNoCacheEnable")
        Timber.d("customeTitle = $customeTitle")

        var urlStr = targetUrl ?: ""
        //成长树
        if (urlStr == RouterPath.Browser.GROWING_TREE) {
            StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_TREE)
        } else if (urlStr == RouterPath.Browser.GUARD_STATISTICS) {//数据统计
            StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_DATA)
        }
        if (!URLUtil.isValidUrl(urlStr)) {
            urlStr = if (urlStr.startsWith("/")) {
                WebUtils.removePath(DataContext.getInstance().baseWebUrl()) + urlStr
            } else {
                DataContext.getInstance().baseWebUrl() + urlStr
            }
        }

        Timber.d("final = $urlStr")

        val argument = Bundle().apply {
            putString(RouterPath.Browser.URL_KEY, urlStr)
            putString(RouterPath.Browser.JS_CALL_INTERCEPTOR_CLASS_KEY, customJsCallInterceptor
                    ?: "")
            putBoolean(RouterPath.Browser.SHOW_HEADER_KEY, showHeader)
            putBoolean(RouterPath.Browser.CACHE_ENABLE, cacheEnable)
            putBoolean(RouterPath.Browser.LOAD_NO_CACHE_ENABLE, loadNoCacheEnable)
            putString(RouterPath.Browser.WEB_TITLE, customeTitle)
            bundle?.let { putBundle(RouterPath.Browser.ARGUMENTS_KEY, it) }
        }

        if (savedInstanceState == null) {
            if (!TextUtils.isEmpty(fragmentClass)) {
                showCustomFragment(argument)
            } else {
                showInnerFragment(argument)
            }
        }
    }

    private fun showInnerFragment(argument: Bundle) {
        BaseWebFragment().apply {
            arguments = argument
        }.let {
            inFragmentTransaction { addWithDefaultContainer(it) }
        }
    }

    private fun showCustomFragment(argument: Bundle) {
        val instantiate = Fragment.instantiate(this, fragmentClass, argument)
        inFragmentTransaction { addWithDefaultContainer(instantiate) }
    }

    override fun layout() = R.layout.app_base_web_activity

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        SystemBarCompat.setTranslucentSystemUi(this, true, false)
    }

    override fun tintStatusBar(): Boolean {
        return false
    }

}