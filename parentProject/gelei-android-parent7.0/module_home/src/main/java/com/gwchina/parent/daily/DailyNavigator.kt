package com.gwchina.parent.daily

import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.findFragmentByTag
import com.android.base.app.fragment.inFragmentTransaction
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject

/**
 *@author hujl
 *@Email: hujlin@163.com
 *@Date : 2019-08-08 18:20
 */
@ActivityScope
class DailyNavigator @Inject constructor(
        private val dailyActivity: DailyActivity,
        private val appRouter: AppRouter
) {

    private fun requireLogined(): Boolean {
        val user = dailyActivity.appDataSource.user()
        //是否登录
        if (!user.logined()) {
            openLoginPage()
            return false
        }
        return true
    }

    private fun openLoginPage() {
        appRouter.build(RouterPath.Account.PATH).navigation(dailyActivity, RouterPath.Account.REQUEST_CODE)
    }

    fun openPicPrePage(position: Int, picPathList: ArrayList<String>) {
        if (dailyActivity.supportFragmentManager.findFragmentByTag(PicPreFragment::class) == null) {
            dailyActivity.inFragmentTransaction {
                addWithStack(fragment = PicPreFragment.newInstance(position, picPathList))
            }
        }
    }

    fun openMessageListPage() {
        if (dailyActivity.supportFragmentManager?.findFragmentByTag(DailyMessageListFragment::class) != null) {
            return
        }
        dailyActivity.inFragmentTransaction {
            addWithStack(fragment = DailyMessageListFragment())
        }
    }

    fun openPublishDailyPage() {
        if (!requireLogined()) {
            return
        }

        val childBoundList = AppContext.appDataSource().user().childList?.filter {
            it.boundDevice()
        }
        //没有绑定的孩子设备->跳到绑定说明页面
        if (childBoundList == null || childBoundList.isEmpty()) {
            appRouter.build(RouterPath.Binding.PATH).navigation(dailyActivity, RouterPath.Binding.REQUEST_CODE)
        } else {
            if (dailyActivity.supportFragmentManager?.findFragmentByTag(DailyPublishFragment::class) != null) {
                return
            }
            dailyActivity.inFragmentTransaction {
                addWithStack(fragment = DailyPublishFragment.newInstance())
            }
        }
    }


    fun replacePublishDailyPage() {
        if (dailyActivity.supportFragmentManager?.findFragmentByTag(DailyStreamFragment::class) != null) {
            return
        }
        dailyActivity.inFragmentTransaction {
            replaceWithDefaultContainer(fragment = DailyStreamFragment())
        }
    }

    fun openExamplePage() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.DIARY_EXAMPLE)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }
}