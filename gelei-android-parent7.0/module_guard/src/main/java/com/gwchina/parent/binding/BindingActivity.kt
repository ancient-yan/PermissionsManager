package com.gwchina.parent.binding

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.android.sdk.social.qq.QQManager
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.binding.common.BindingProcessStatusKeeper
import com.gwchina.parent.binding.presentation.ChildInfoCollectFragment
import com.gwchina.parent.binding.presentation.ScanChildGuideFragment
import com.gwchina.parent.binding.presentation.SelectChildFragment
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.router.RouterPath
import com.tencent.connect.common.Constants
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-23 09:55
 */
@Route(path = RouterPath.Binding.PATH)
class BindingActivity : InjectorAppBaseActivity() {

    @JvmField @Autowired(name = RouterPath.Binding.CHILD_USER_ID_KEY) var childUserId: String? = null
    @JvmField @Autowired(name = RouterPath.Binding.SELECT_CHILD_KEY) var selectChild: Boolean = false

    @Inject lateinit var appDataSource: AppDataSource
    @Inject lateinit var bindingNavigator: BindingNavigator

    internal val bindingProcessStatusKeeper = BindingProcessStatusKeeper()

    override fun layout() = R.layout.app_base_activity

    override fun initialize(savedInstanceState: Bundle?) {
        super.initialize(savedInstanceState)
        bindingProcessStatusKeeper.restore(savedInstanceState)
        val userId = childUserId
        if (!userId.isNullOrEmpty()) {
            bindingProcessStatusKeeper.setAddingDeviceForExistChild(userId)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        bindingProcessStatusKeeper.save(outState)
        super.onSaveInstanceState(outState)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        savedInstanceState.ifNull { showFragment() }
    }

    private fun showFragment() {
        val target: Fragment
        when {
            selectChild -> target = SelectChildFragment()

            childUserId.isNullOrEmpty() -> {
                val childList = appDataSource.user().childList

                target = if (childList.isNullOrEmpty()) {
                    ChildInfoCollectFragment()
                } else if (childList.size == 1 && childList[0].device_list.isNullOrEmpty()) {
                    bindingProcessStatusKeeper.setAddingDeviceForExistChild(childList[0].child_user_id)
                    ScanChildGuideFragment()
                } else {
                    SelectChildFragment()
                }
            }

            else -> target = ScanChildGuideFragment()
        }

        inFragmentTransaction {
            addWithDefaultContainer(target)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_QQ_SHARE || requestCode == Constants.REQUEST_QZONE_SHARE) {
            var intent = data
            if (resultCode == 0 && data == null) {//未登录QQ的情况下
                intent = Intent()
            }
            QQManager.onActivityResult(requestCode, resultCode, intent)
        }
    }

}