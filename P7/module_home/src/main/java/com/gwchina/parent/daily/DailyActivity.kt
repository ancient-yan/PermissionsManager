package com.gwchina.parent.daily

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.findFragmentByTag
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNonNull
import com.android.base.kotlin.javaClassName
import com.android.base.kotlin.otherwise
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import timber.log.Timber
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-26 17:11
 */
@Route(path = RouterPath.Diary.PATH)
class DailyActivity : InjectorAppBaseActivity() {

    @Inject
    lateinit var appRouter: AppRouter
    @Inject
    lateinit var appDataSource: AppDataSource

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)

        //设置 MainFragment
        if (intent.hasExtra(RouterPath.PAGE_KEY)) {
            val page = intent.getIntExtra(RouterPath.PAGE_KEY, 0)
            if (page == RouterPath.Diary.PAGE_LIST) {
                supportFragmentManager.findFragmentByTag(DailyStreamFragment::class).ifNonNull {
                    return@ifNonNull
                }.otherwise {
                    inFragmentTransaction {
                        addWithDefaultContainer(DailyStreamFragment())
                    }
                }
            } else if (page == RouterPath.Diary.PAGE_PUBLISH) {
                openPublishDailyPage()
            }
        }
    }

    private fun openPublishDailyPage() {
        val childBoundList = AppContext.appDataSource().user().childList?.filter {
            it.boundDevice()
        }
        //没有绑定的孩子设备->跳到绑定说明页面
        if (childBoundList == null || childBoundList.isEmpty()) {
            appRouter.build(RouterPath.Binding.PATH).navigation(this, RouterPath.Binding.REQUEST_CODE)
        } else {
            if (supportFragmentManager?.findFragmentByTag(DailyPublishFragment::class) != null) {
                return
            }
            inFragmentTransaction {
                addWithDefaultContainer(DailyPublishFragment.newInstance(true))
            }
        }
    }


    override fun layout() = R.layout.app_base_activity

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.fragments.forEach {
            Timber.e(it.javaClassName())
            if (it.javaClassName() == "com.gwchina.parent.daily.DailyStreamFragment") {
                callFragmentResult(it, requestCode, resultCode, data)
            }
        }
    }

    /**
     * 解决Fragment中不回调onActivityResult
     */
    private fun callFragmentResult(fragment: Fragment, requestCode: Int, resultCode: Int, data: Intent?) {
        fragment.onActivityResult(requestCode, resultCode, data)
        val childFragment = fragment.childFragmentManager.fragments
        childFragment.forEach {
            callFragmentResult(it, requestCode, resultCode, data)
        }
    }
}