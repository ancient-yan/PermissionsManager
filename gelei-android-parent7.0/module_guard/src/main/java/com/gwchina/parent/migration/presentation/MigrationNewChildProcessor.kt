package com.gwchina.parent.migration.presentation

import android.arch.lifecycle.ViewModelProvider
import android.util.Log
import com.android.base.app.fragment.findFragmentByTag
import com.android.base.app.fragment.popBackTo
import com.android.base.app.mvvm.getViewModel
import com.gwchina.parent.binding.common.NewChildProcessor
import com.gwchina.parent.binding.presentation.ChildInfo
import com.gwchina.parent.migration.MigrationActivity
import com.gwchina.parent.migration.MigrationNavigator
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.utils.calculateAgeByBirthday
import com.gwchina.sdk.base.utils.splitBirthday
import java.util.*
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-29 15:39
 */
class MigrationNewChildProcessor @Inject constructor(
        viewModelFactory: ViewModelProvider.Factory,
        private val migrationActivity: MigrationActivity,
        private val migrationNavigator: MigrationNavigator
) : NewChildProcessor {

    private val migrationViewModel by lazy {
        migrationActivity.getViewModel<MigrationViewModel>(viewModelFactory)
    }

    override fun newNewChildInfoCollected(childInfo: ChildInfo) {
        migrationViewModel.addChild(childInfo)
        //.如果当前家长仅绑定了一台有效设备（属于7.0绑定清单的），则点击提交后，直接跳转至20.4；--->选择守护等级
        val migrationDevice = migrationViewModel.migrationDevice
        if (migrationDevice != null) {
            val id = UUID.randomUUID().toString()
            val (year, month, day) = splitBirthday(migrationViewModel.uploadingChild.birthday)
            migrationNavigator.openGuardLevelForGetLevelInfo(id, migrationDevice.device_type?:"", calculateAgeByBirthday(year, month, day))
        } else {
            when {
                migrationViewModel.currentMigrationStep == MigratingData.MIGRATING_STEP_ADDING -> showAddingChildPage()
                migrationViewModel.currentMigrationStep == MigratingData.MIGRATING_STEP_BELONGING -> showBelongingPage()
                migrationViewModel.currentMigrationStep == MigratingData.MIGRATING_STEP_GUIDE -> showAddingChildPage()
            }
        }

    }

    private fun showAddingChildPage() {
        val fragment = migrationActivity.supportFragmentManager.findFragmentByTag(AddingChildFragment::class)
        if (fragment != null) {
            migrationActivity.supportFragmentManager.popBackTo(AddingChildFragment::class.java.name)
        } else {
            migrationActivity.supportFragmentManager.popBackStackImmediate()
            migrationNavigator.openAddingChildPage()
        }
    }

    private fun showBelongingPage() {
        val fragment = migrationActivity.supportFragmentManager.findFragmentByTag(BelongingDeviceFragment::class)
        if (fragment != null) {
            migrationActivity.supportFragmentManager.popBackTo(BelongingDeviceFragment::class.java.name)
        } else {
            migrationActivity.supportFragmentManager.popBackStackImmediate()
            migrationNavigator.openBelongingDevicePage()
        }
    }

}