package com.gwchina.parent.migration.presentation

import com.gwchina.parent.migration.data.MigratingDevice

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 19:00
 */
class MigratingData {

    companion object {
        internal const val MIGRATING_STEP_NONE = 0
        internal const val MIGRATING_STEP_GUIDE = 1
        internal const val MIGRATING_STEP_ADDING = 2
        internal const val MIGRATING_STEP_BELONGING = 3
        internal const val MIGRATING_STEP_CONFIRMING = 4
        internal const val MIGRATING_STEP_DEVICE_STATE = 5
    }

    var alreadyMarkedMigrating: Boolean = false
    var migrationStep: Int = MIGRATING_STEP_NONE

    private val childList = mutableListOf<UploadingChild>()
    private val unBelongedDeviceList = mutableListOf<MigratingDevice>()

    fun result(): List<UploadingChild> {
        return childList
    }

    fun setMigrationDevice(it: List<MigratingDevice>) {
        unBelongedDeviceList.clear()
        unBelongedDeviceList.addAll(it)
    }

    fun addChild(uploadingChild: UploadingChild) {
        childList.add(uploadingChild)
    }

    fun deviceMigrated(migrationDevice: MigratingDevice) {
        unBelongedDeviceList.remove(migrationDevice)
    }

    fun nextUnBelongedDevice() = unBelongedDeviceList.firstOrNull()

    fun isDeviceOne() = unBelongedDeviceList.size == 1

}

@Suppress("UNUSED")
class UploadingChild(
        var name: String,
        var sex: Int,
        var birthday: String,
        var grade: Int,
        var relationship_code: Int
) {

    private val device_list = mutableListOf<UploadingDevice>()

    fun addDevice(device: UploadingDevice) {
        if (!device_list.contains(device)) {
            device_list.add(device)
        }
    }

}

data class UploadingDevice(val device_id: String, val guard_level_code: String/*, val guard_item_id_list: List<String>? = null*/)
