package com.gwchina.parent.member

import com.android.base.app.fragment.clearBackStack
import com.android.base.app.fragment.findFragmentByTag
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.app.fragment.popBackTo
import com.gwchina.parent.member.presentation.center.MemberCenterFragment
import com.gwchina.parent.member.presentation.payresult.PayResultFragment
import com.gwchina.parent.member.presentation.purchase.PurchaseMemberFragment
import com.gwchina.parent.member.presentation.record.PurchaseRecordFragment
import com.gwchina.sdk.base.router.AppRouter
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-16 19:48
 */
class MemberNavigator @Inject constructor(
        private val mMemberActivity: MemberActivity,
        private val mAppRouter: AppRouter
) {
    /**
     * 打开购买会员页面
     */
    /*fun openPurchaseMemberPage(isReplace: Boolean) {
        val fragment = mMemberActivity.supportFragmentManager.findFragmentByTag(PurchaseMemberFragment::class)
        if (fragment != null) {
            mMemberActivity.supportFragmentManager.popBackTo(PurchaseMemberFragment::class.java.name)
            return
        }
        mMemberActivity.inFragmentTransaction {
            if (isReplace) {
                replaceWithDefaultContainer(fragment = PurchaseMemberFragment())
            } else {
                addWithStack(fragment = PurchaseMemberFragment())
            }
        }
    }*/

    /**
     * 打开会员中心页面
     */
    fun openMemberCenter() {
        val fragment = mMemberActivity.supportFragmentManager.findFragmentByTag(MemberCenterFragment::class)
        if (fragment != null) {
            mMemberActivity.supportFragmentManager.popBackTo(MemberCenterFragment::class.java.name)
            return
        }
        mMemberActivity.inFragmentTransaction {
            replaceWithDefaultContainer(fragment = MemberCenterFragment())
        }
    }

    /**
     * 回到会员中心页面
     */
    fun backToMemberCenter() {
        mMemberActivity.supportFragmentManager.clearBackStack()
        mMemberActivity.inFragmentTransaction {
            replaceWithDefaultContainer(fragment = MemberCenterFragment.newInstance(true))
        }
    }

    /**
     * 打开购买记录
     */
    fun openHistory() {
        mMemberActivity.inFragmentTransaction {
            addWithStack(fragment = PurchaseRecordFragment())
        }
    }

    /**
     * 打开会员协议页面
     */
    fun openAgreement() {
        mAppRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.VIP_PROTOCOL)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }

    /**
     * 支付结果页面
     */
    fun paySuccess(orderNo: String) {
        mMemberActivity.inFragmentTransaction {
            replaceWithStack(fragment = PayResultFragment.newInstance(orderNo))
        }
    }

    /**
     * 打开客服页面
     */
    fun openCustomerPage() {
        mAppRouter.build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, RouterPath.Browser.CUSTOMER)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    }
}