package com.gwchina.parent.binding.presentation

import android.net.Uri
import android.os.Parcelable
import android.text.TextUtils
import android.webkit.URLUtil
import kotlinx.android.parcel.Parcelize
import timber.log.Timber


@Parcelize
class ChildInfo(
        var name: String = "",
        var sex: Int = -1,
        var grade: Int = -1/*from = 1, to = 12*/,
        var relationship: Int = -1,
        var birthday: String = ""
) : Parcelable {

    private fun hasName() = !TextUtils.isEmpty(name)
    private fun hasBirthday() = !TextUtils.isEmpty(birthday)
    private fun hasSetSex() = sex != -1
    private fun hasSetRelationship() = relationship != -1
    private fun hasSetGrade() = grade != -1

    fun completedInfo() = hasBirthday() &&
            hasName() &&
            hasSetGrade() &&
            hasSetRelationship() &&
            hasSetSex()

}

class DeviceInfo(
        val deviceSn: String,
        val manufacture: String,
        val osVersion: String,
        val romVersion: String
) {
    companion object {

        //http://home.gwchina.cn?child_device_sn=xxx&manufacture=HUAWEI&rom_version=EmotionUI_9.1.0&os_version=28
        private const val DEVICE_SN_KEY = "child_device_sn"
        private const val MANUFACTURE_KEY = "manufacture"
        private const val ROM_VERSION_KEY = "rom_version"
        private const val OS_VERSION_KEY = "os_version"

        //child_device_sn=xxx
        private const val OLD_SPLIT = "="
        private const val OLD_PREFIX = "child_device_sn"

        fun fromUrl(deviceInfo: String): DeviceInfo? {

            Timber.d("deviceInfo = $deviceInfo")

            val deviceSn: String?
            val manufacture: String?
            val romVersion: String?
            val osVersion: String?

            //新协议
            if (URLUtil.isHttpUrl(deviceInfo)) {
                val uri = Uri.parse(deviceInfo)
                deviceSn = uri.getQueryParameter(DEVICE_SN_KEY)
                manufacture = uri.getQueryParameter(MANUFACTURE_KEY)
                romVersion = uri.getQueryParameter(ROM_VERSION_KEY)
                osVersion = uri.getQueryParameter(OS_VERSION_KEY)
            } else /*旧协议*/ {
                val split = deviceInfo.split(OLD_SPLIT)
                deviceSn = if (split.size == 2 && split[0] == OLD_PREFIX) {
                    split[1]
                } else {
                    ""
                }
                manufacture = ""
                romVersion = ""
                osVersion = ""
            }

            Timber.d("child_device_sn = $deviceSn manufacture = $manufacture romVersion = $romVersion osVersion = $osVersion")

            return if (deviceSn.isNullOrEmpty()) {
                null
            } else {
                DeviceInfo(deviceSn, manufacture ?: "", osVersion ?: "", romVersion ?: "")
            }

        }
    }
}