package com.gwchina.parent.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.clearBackStack
import com.android.base.app.fragment.findFragmentByTag
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNonNull
import com.android.base.kotlin.ignoreCrash
import com.android.base.kotlin.otherwise
import com.android.base.rx.subscribed
import com.android.base.utils.android.compat.SystemBarCompat
import com.google.gson.reflect.TypeToken
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.presentation.home.MainAdvertisingDialogTask
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.config.AppSettings
import com.gwchina.sdk.base.data.api.HttpResult
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.VipRule
import com.gwchina.sdk.base.data.utils.JsonUtils
import com.gwchina.sdk.base.data.utils.JsonUtils.fromType
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.upgrade.AppUpgradeTask
import com.gwchina.sdk.base.utils.SequentialUITaskExecutor
import com.gwchina.sdk.base.widget.dialog.TipsManager
import com.gwchina.sdk.base.widget.member.MemberExpiringTipsTask
//import com.umeng.analytics.MobclickAgent
import org.json.JSONObject
import javax.inject.Inject


/**
 *主界面
 *
 *@author Ztiany
 *      Date : 2018-09-06 14:42
 */
@Route(path = RouterPath.Main.PATH)
class MainActivity : InjectorAppBaseActivity() {

    companion object {
        private const val IS_FIRST_LAUNCHING = "app_is_first_launching_in_main"
    }

    @Inject
    lateinit var appRouter: AppRouter
    @Inject
    lateinit var appDataSource: AppDataSource
    @Inject
    internal lateinit var mainNavigator: MainNavigator

    private lateinit var mainFragment: MainFragment

    private var clickToExit = false

    override fun layout() = R.layout.main_activity

    override fun tintStatusBar() = false

    var isShowCustomDialog = false
    var isShowGuardGuidePop = false

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        addDelegate(ResultProcessor())
        addDelegate(SettingLevelListener())
        window.decorView.post {
            addUITask()
        }
    }

    private fun addUITask() {
        SequentialUITaskExecutor.execute(UserPrivacyAgreementTask())
        SequentialUITaskExecutor.execute(AppUpgradeTask())
//        SequentialUITaskExecutor.execute(MemberExpiredForceChooseTask.getInstance())
//        SequentialUITaskExecutor.execute(MemberExpiringTipsTask.getInstance())
        //非第一次启动
        if (!AppSettings.isFirst(IS_FIRST_LAUNCHING)) {
            SequentialUITaskExecutor.execute(MainAdvertisingDialogTask())
        }
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        /*如果登录了就同步用户数据*/
        if (appDataSource.userLogined()) {
            appDataSource.syncUser().subscribed()
        }

        //设置 TranslucentStatus
        SystemBarCompat.setTranslucentSystemUi(this, true, false)

        //设置 MainFragment
        supportFragmentManager.findFragmentByTag(MainFragment::class).ifNonNull {
            mainFragment = this@ifNonNull
        }.otherwise {
            mainFragment = MainFragment()
            inFragmentTransaction {
                addWithDefaultContainer(mainFragment)
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        ignoreCrash { processIntent(intent) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        window.decorView.post {
            ignoreCrash { processIntent(intent) }
        }
    }

    /*run it in post action*/
    private fun processIntent(intent: Intent) {
        //切换页面
        if (intent.hasExtra(RouterPath.PAGE_KEY)) {
            val page = intent.getIntExtra(RouterPath.PAGE_KEY, 0)
            switchPage(page)
        }

        //其他行为
        if (intent.hasExtra(RouterPath.Main.ACTION_KEY)) {
            val action = intent.getIntExtra(RouterPath.Main.ACTION_KEY, 0)
            //用户需要重新登录，登录之前需要清空用户之前的数据
            if (action == RouterPath.Main.ACTION_RE_LOGIN) {
                //back to main
                supportFragmentManager.clearBackStack()
                postRun {
                    switchPage(RouterPath.Main.PAGE_HOME)
                    //clear data
                    appDataSource.logout()
                    //jump to login activity
                    mainNavigator.openLoginPage()
                }
            } else if (action == RouterPath.Main.ACTION_MEMBER_EXPIRING_TIPS) {
                MemberExpiringTipsTask.showMemberExpiringTimeTipsDirectly(this)
            }
        }
    }

    private fun switchPage(page: Int) = postRun {
        when (page) {
            RouterPath.Main.PAGE_APP_APPROVAL -> mainNavigator.openAppApprovalListPage()
            else -> {
                supportFragmentManager.clearBackStack()
                mainFragment.selectTabAtPosition(page)
            }
        }
    }

    override fun superOnBackPressed() {
        if (!mainFragment.isVisible) {
            super.superOnBackPressed()
            return
        }
        if (clickToExit) {
//            MobclickAgent.onKillProcess(this)
            supportFinishAfterTransition()
        }
        if (!clickToExit) {
            clickToExit = true
            TipsManager.showMessage(getString(R.string.main_exit_tips))
            postRun(1000) {
                clickToExit = false
            }
        }
    }

    private fun postRun(delaMillis: Long = 0, action: () -> Unit) {
        if (delaMillis == 0L) {
            window.decorView.post(action)
        } else {
            window.decorView.postDelayed(action, delaMillis)
        }
    }

    /**模拟解析vip规则数据*/
    override fun onResume() {
        super.onResume()
        val inputStream = assets.open("vip_rule.json")
        val result = inputStream.readBytes()
        val json = String(result)
        val jsonObject = JSONObject(json)
        val vipRuleJson = jsonObject.getJSONObject("data").getJSONObject("vip_rule")
        val vipRule = JsonUtils.fromJson(vipRuleJson.toString(), VipRule::class.java)
        appDataSource.syncUserVipRule(vipRule)
    }
}