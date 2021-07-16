package com.gwchina.parent.main.presentation.location

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.findFragmentByTag
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNonNull
import com.android.base.kotlin.otherwise
import com.android.base.utils.android.compat.SystemBarCompat
import com.android.sdk.social.qq.QQManager
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.data.ChildLocation
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.router.RouterPath
import com.tencent.connect.common.Constants
import timber.log.Timber

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-09-19 14:08
 */
@Route(path = RouterPath.Main.LOCATION)
class ChildLocationActivity : InjectorAppBaseActivity() {

    @JvmField
    @Autowired(name = RouterPath.Main.LOCATION_KEY)
    var childLocation: ChildLocation? = null

    override fun layout(): Any? {
        return R.layout.app_base_activity
    }

    private fun hideSystemUi() {
        try {
            SystemBarCompat.setTranslucentSystemUi(this, true, true)
            val systemUiVisibility = window.decorView.systemUiVisibility
            window.decorView.systemUiVisibility = systemUiVisibility or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()
    }

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        //设置 MainFragment
        supportFragmentManager.findFragmentByTag(ChildLocationFragment::class).ifNonNull {
            this@ifNonNull
        }.otherwise {
            val childLocationFragment = ChildLocationFragment.newInstance(childLocation)
            inFragmentTransaction {
                addWithDefaultContainer(childLocationFragment)
            }
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