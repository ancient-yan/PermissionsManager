package com.gwchina.parent.migration.presentation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.SchedulerProvider
import com.gwchina.parent.binding.presentation.ChildInfo
import com.gwchina.parent.migration.data.MigratingDevice
import com.gwchina.parent.migration.data.MigrationRepository
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 15:26
 */
class MigrationViewModel @Inject constructor(
        private val migrationRepository: MigrationRepository,
        private val schedulerProvider: SchedulerProvider
) : ArchViewModel() {

    private val migratingData: MigratingData = migrationRepository.migratingData()
            ?: MigratingData()

    val childList: LiveData<List<UploadingChild>>
        get() = _childList
    private val _childList = MutableLiveData<List<UploadingChild>>()

    private val _supportedDevice = MutableLiveData<Resource<List<MigratingDevice>>>()
    val supportedDevice: LiveData<Resource<List<MigratingDevice>>>
        get() = _supportedDevice

    val nextUnBelongedDevice: MigratingDevice?
        get() = migratingData.nextUnBelongedDevice()

    val hasChild: Boolean
        get() = migratingData.result().isNotEmpty()

    val uploadingChild: UploadingChild
        get() = migratingData.result()[0]

    val migrationDevice: MigratingDevice?
        get() = if (migratingData.isDeviceOne()) migratingData.nextUnBelongedDevice() else null

    val currentMigrationStep: Int
        get() = migratingData.migrationStep

    init {
        dispatchMigrationData()
    }

    fun updateMigrationStep(migrationStep: Int) {
        migratingData.migrationStep = migrationStep
        saveMigrationData()
    }

    fun addChild(childInfo: ChildInfo) {
        migratingData.addChild(UploadingChild(childInfo.name, childInfo.sex, childInfo.birthday, childInfo.grade, childInfo.relationship))
        saveMigrationData()
        dispatchMigrationData()
    }

    fun deviceBelongingToChild(migrationDevice: MigratingDevice, level: Int, guardItem: List<String>? = null, child: UploadingChild) {
        child.addDevice(UploadingDevice(migrationDevice.device_id
                ?: "", level.toString()/*, guardItem*/))
        migratingData.deviceMigrated(migrationDevice)
        saveMigrationData()
    }

    private fun saveMigrationData() {
        migrationRepository.saveMigrationData(migratingData)
    }

    private fun dispatchMigrationData() {
        _childList.postValue(migratingData.result())
    }

    fun loadMigrationDeviceList() {
        _supportedDevice.postValue(Resource.loading())
        migrationRepository.migrationDeviceList()
                .subscribe(
                        {
                            migratingData.setMigrationDevice(it.filter { device -> device.canDoMigrating() })
                            saveMigrationData()
                            _supportedDevice.postValue(Resource.success(it))
                        },
                        {
                            _supportedDevice.postValue(Resource.error(it))
                        }
                )
    }

    fun submitChildDeviceList() = migrationRepository.batchBindChildDevice(migratingData.result()).toResourceLiveData()

    fun startNewVersion() = migrationRepository.startNewVersion().toResourceLiveData()

    fun backToOldVersion() = if (migratingData.alreadyMarkedMigrating) {
        MutableLiveData<Resource<Any>>().apply { postValue(Resource.success()) }
    } else {
        migrationRepository.backToOldVersion()
                .observeOn(schedulerProvider.ui())
                .doOnComplete {
                    migratingData.alreadyMarkedMigrating = true
                    saveMigrationData()
                }
                .toResourceLiveData()
    }

}

