package com.gwchina.parent.profile

import android.os.Bundle
import android.support.v4.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.profile.presentation.child.ChildInfoFragment
import com.gwchina.parent.profile.presentation.patriarch.PatriarchInfoFragment
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.router.RouterPath

/**
 * 个人信息
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-03-18 15:25
 */
@Route(path = RouterPath.Profile.PATH)
class ProfileActivity : InjectorAppBaseActivity() {

    @JvmField
    @Autowired(name = RouterPath.PAGE_KEY)
    var pageKey: Int = -1
    @JvmField
    @Autowired(name = RouterPath.Profile.CHILD_USER_ID_KEY)
    var childUserId: String? = null
    /**孩子信息页面，点击开启临时锁屏是否需要判断权限弹窗*/
    @JvmField
    @Autowired(name = RouterPath.Profile.CHILD_NEED_SHOW_PERMISSION_KEY)
    var needShowPermissionDialog: Boolean = false

    override fun layout() = R.layout.app_base_activity

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        savedInstanceState.ifNull {
            showTargetPage()
        }
    }

    private fun showTargetPage() {
        val fragment: Fragment = when (pageKey) {
            /**家长信息*/
            RouterPath.Profile.PAGE_PATRIARCH_INFO -> PatriarchInfoFragment()
            /**孩子信息*/
            RouterPath.Profile.PAGE_CHILD_INFO -> ChildInfoFragment.getFragment(needShowPermissionDialog)
            else -> throw IllegalArgumentException("invalidate action key")
        }
        inFragmentTransaction {
            addWithDefaultContainer(fragment)
        }
    }

}