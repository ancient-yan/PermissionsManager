package com.gwchina.parent.account

import android.app.Activity
import android.content.Intent
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.fragment.inFragmentTransaction
import com.gwchina.parent.account.presentation.binding.BindWechatFragment
import com.gwchina.sdk.base.data.models.LoginAttachment
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.MigrationInfo
import com.gwchina.sdk.base.router.RouterPath
import timber.log.Timber
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-14 18:31
 */
@ActivityScope
class AccountNavigator @Inject constructor(
        private val accountActivity: AccountActivity,
        private val appRouter: AppRouter
) {

    /**
     *[loginType] 参考 [RouterPath.Account]。
     */
    fun exitAccountSuccessfully(loginType: Int, memberPresent: String?) {
        val intent = Intent()
        intent.putExtra(RouterPath.Account.LOGIN_TYPE_KEY, loginType)
        if (!memberPresent.isNullOrEmpty()) {
            intent.putExtra(RouterPath.Account.VIP_PRESENT_DAYS_KEY, memberPresent)
        }
        accountActivity.setResult(Activity.RESULT_OK, intent)
        accountActivity.supportFinishAfterTransition()
    }

    fun openOldUserMigrationPage(loginAttachment: LoginAttachment) {
        appRouter.build(RouterPath.Migration.PATH)
                .withParcelable(RouterPath.Migration.MIGRATION_INFO_KEY, loginAttachment.greed_box_init_setting?.let { MigrationInfo(it, loginAttachment.not_upgrade_device_list ?: emptyList()) })
                .navigation()
        accountActivity.supportFinishAfterTransition()
    }

    fun openBindWechatPage(wechatId: String) {
        accountActivity.inFragmentTransaction {
            replaceWithStack(fragment = BindWechatFragment.newInstance(wechatId))
        }
    }

    fun openPrivacyPage() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.PRIVACY)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    fun openAgreementPage() {
        appRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.AGREEMENT)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

}