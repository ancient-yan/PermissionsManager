package com.gwchina.sdk.debug

import android.os.Bundle
import com.android.base.app.activity.BaseActivity
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.android.base.permission.AutoPermissionRequester
import com.app.base.R
import com.gwchina.sdk.base.widget.dialog.TipsManager
import com.yanzhenjie.permission.runtime.Permission
import timber.log.Timber

/**
 * 仅用于调试版本
 *
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-07-26 18:49
 */
class DebugActivity : BaseActivity() {

    override fun layout() = R.layout.app_base_activity

    override fun setupView(savedInstanceState: Bundle?) {
        Timber.d("savedInstanceState = $savedInstanceState")
        savedInstanceState.ifNull {
            inFragmentTransaction {
                addWithDefaultContainer(DebugFragment())
            }
        }
        requestPermission()
    }

    fun switchHost() {
        inFragmentTransaction {
            replaceWithStack(fragment = EnvironmentConfigFragment.newInstance(false))
        }
    }

    private fun requestPermission() {
        AutoPermissionRequester.with(this)
                .permission(Permission.READ_PHONE_STATE, Permission.WRITE_EXTERNAL_STORAGE)
                .onDenied {
                    TipsManager.showMessage("没有权限")
                    supportFinishAfterTransition()
                }.request()
    }

    fun showFileUploadTest() {
        inFragmentTransaction {
            replaceWithStack(fragment = FileUploadFragment())
        }
    }

}