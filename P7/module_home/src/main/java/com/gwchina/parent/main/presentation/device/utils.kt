package com.gwchina.parent.main.presentation.device

import com.gwchina.sdk.base.data.models.Device


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-05-20 11:16
 */
internal fun createPendingUnbindDeviceName(device: Device): String {
    return if (device.index < 1) {
        device.device_name?:""
    } else {
        "${device.device_name} ${device.index}"
    }
}