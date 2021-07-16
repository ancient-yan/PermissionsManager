package com.gwchina.parent.migration

import android.arch.lifecycle.ViewModelProvider
import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.app.mvvm.getViewModel
import com.android.base.kotlin.ifNull
import com.android.base.utils.android.compat.SystemBarCompat
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.migration.presentation.*
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.data.api.MIGRATING_ALL_UNSUPPORTED_CHILD_NOT_UPGRADE
import com.gwchina.sdk.base.data.api.MIGRATING_ALL_UNSUPPORTED_CHILD_UPGRADED
import com.gwchina.sdk.base.router.MigrationInfo
import com.gwchina.sdk.base.router.RouterManager
import com.gwchina.sdk.base.router.RouterPath
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 14:02
 */
@Route(path = RouterPath.Migration.PATH)
class MigrationActivity : InjectorAppBaseActivity() {

    override fun layout() = R.layout.app_base_activity

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val migrationViewModel by lazy {
        getViewModel<MigrationViewModel>(viewModelFactory)
    }

    @JvmField
    @Autowired(name = RouterPath.Migration.MIGRATION_INFO_KEY)
    var migrationInfo: MigrationInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SystemBarCompat.setTranslucentSystemUi(this, true, false)

        savedInstanceState.ifNull {
            val info = migrationInfo
            if (info == null) {
                continueMigrating()
            } else {
                startMigrating(info)
            }
        }

    }

    private fun startMigrating(info: MigrationInfo) {
        setMigrationStarted()
        //全部支持，强制升级的7.0
        when {
            //3
            info.greed_box_init_setting == MIGRATING_ALL_UNSUPPORTED_CHILD_NOT_UPGRADE -> inFragmentTransaction {
                addWithDefaultContainer(MigrationGuideFragment())
            }
            //4
            info.greed_box_init_setting == MIGRATING_ALL_UNSUPPORTED_CHILD_UPGRADED -> inFragmentTransaction {
                addWithDefaultContainer(ChildDeviceStateRefreshFragment())
            }
            else -> //部分支持或全部不支持，跳转到确认升级界面
                inFragmentTransaction {
                    addWithDefaultContainer(MigrationConfirmingFragment())
                }
        }
    }

    private fun continueMigrating() {
        inFragmentTransaction {
            when {
                //4
                migrationViewModel.currentMigrationStep == MigratingData.MIGRATING_STEP_CONFIRMING -> addWithDefaultContainer(MigrationConfirmingFragment())
                //5
                migrationViewModel.currentMigrationStep == MigratingData.MIGRATING_STEP_DEVICE_STATE -> addWithDefaultContainer(ChildDeviceStateRefreshFragment())
                else -> addWithDefaultContainer(MigrationGuideFragment())
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        RouterManager.dispatchActivityResult(supportFragmentManager, requestCode, resultCode, data)
    }

}