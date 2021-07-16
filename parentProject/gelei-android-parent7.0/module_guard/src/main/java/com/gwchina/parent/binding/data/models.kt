package com.gwchina.parent.binding.data

import com.gwchina.sdk.base.data.api.NO_FLAG


data class BindingResponse(
        var child_user_id: String = "",
        var device_type: String? = "",
        var first_bind_device: String? = null,
        val custom_device_flag: String? = NO_FLAG
)

data class BindingNewChildResponse(val child_user_id: String, val device: Device)

data class Device(val first_bind_device: String, val custom_device_flag: String = NO_FLAG)