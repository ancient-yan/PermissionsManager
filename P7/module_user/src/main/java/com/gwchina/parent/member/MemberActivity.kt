package com.gwchina.parent.member

import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.gwchina.lssw.parent.user.R
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject

/**
 * 会员中心
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 14:50
 */
@Route(path = RouterPath.MemberCenter.PATH)
class MemberActivity : InjectorAppBaseActivity() {
    @Inject
    lateinit var memberNavigator: MemberNavigator

    override fun layout(): Int = R.layout.app_base_activity

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        ResultProcessor(this)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        processIntent(intent)
    }

    internal fun processIntent(intent: Intent) {
        if (!requireLogined()) {
            return
        }
        switchPage()
    }

    private fun switchPage() = postRun {
        memberNavigator.openMemberCenter()
    }

    private fun postRun(delaMillis: Long = 0, action: () -> Unit) {
        if (delaMillis == 0L) {
            window.decorView.post(action)
        } else {
            window.decorView.postDelayed(action, delaMillis)
        }
    }

    private fun requireLogined(): Boolean {
        val user = com.gwchina.sdk.base.AppContext.appDataSource().user()
        val mAppRouter = com.gwchina.sdk.base.AppContext.appRouter()
        //是否登录
        if (!user.logined()) {
            mAppRouter.build(RouterPath.Account.PATH).navigation(this, RouterPath.MemberCenter.REQUEST_CODE)
            return false
        }
        return true
    }
}