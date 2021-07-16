package com.gwchina.parent.migration.presentation

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.util.Log
import android.view.View
import com.android.base.app.dagger.Injectable
import com.android.base.app.mvvm.getViewModelFromActivity
import com.android.base.app.ui.processErrorWithStatus
import com.android.base.data.onError
import com.android.base.data.onLoading
import com.android.base.data.onSuccess
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.migration.MigrationNavigator
import com.gwchina.parent.migration.data.MigratingDevice
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.utils.calculateAgeByBirthday
import com.gwchina.sdk.base.utils.splitBirthday
import kotlinx.android.synthetic.main.migration_fragment_guide.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * 迁移引导
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 14:59
 */
class MigrationGuideFragment : InjectorBaseFragment(), Injectable {

    @Inject
    lateinit var migrationNavigator: MigrationNavigator

    var isLoadSuccess = false
    var migrationDevice: MigratingDevice? = null

    private val migrationViewModel by lazy {
        getViewModelFromActivity<MigrationViewModel>(viewModelFactory)
    }

    override fun provideLayout() = R.layout.migration_fragment_guide

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        migrationViewModel.loadMigrationDeviceList()
        observeMigrationData()
        btnMigrationNextStep.setOnClickListener {
            view.post {
                Timber.d("migrationViewModel.currentMigrationStep =${migrationViewModel.currentMigrationStep}")
                when (migrationViewModel.currentMigrationStep) {
                    MigratingData.MIGRATING_STEP_ADDING -> migrationNavigator.openAddingChildPage()
                    MigratingData.MIGRATING_STEP_BELONGING -> migrationNavigator.openBelongingDevicePage()
                    else -> {
                        if (isLoadSuccess) {
                            if (migrationViewModel.hasChild) {
                                // 如果当前家长仅绑定了一台有效设备（属于7.0绑定清单的），则点击提交后，直接跳转至选择守护等级
                                if (migrationDevice != null) {
                                    val id = UUID.randomUUID().toString()
                                    val (year, month, day) = splitBirthday(migrationViewModel.uploadingChild.birthday)
                                    migrationNavigator.openGuardLevelForGetLevelInfo(id, migrationDevice!!.device_type
                                            ?: "", calculateAgeByBirthday(year, month, day))
                                } else {
                                    migrationNavigator.openAddingChildPage()
                                }
                            } else {
                                migrationNavigator.openChildInfoCollectingPage()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun observeMigrationData() {
        migrationViewModel.supportedDevice.observe(this, Observer {
            it?.onSuccess { list ->
                //如果当前家长仅绑定了一台有效设备（属于7.0绑定清单的），则点击提交后，直接跳转至选择守护等级
                if (list != null && list.size == 1) {
                    migrationDevice = list[0]
                }
                isLoadSuccess = true
            }?.onLoading {
            }?.onError { err ->
                isLoadSuccess = false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val migrationStep = migrationViewModel.currentMigrationStep
        if (migrationStep == MigratingData.MIGRATING_STEP_NONE || migrationStep == MigratingData.MIGRATING_STEP_DEVICE_STATE) {
            migrationViewModel.updateMigrationStep(MigratingData.MIGRATING_STEP_GUIDE)
        }
    }

    override fun handleBackPress() = true

}