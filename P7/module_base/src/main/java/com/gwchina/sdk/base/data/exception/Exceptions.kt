package com.gwchina.sdk.base.data.exception

import com.android.sdk.net.exception.ApiErrorException

class DeviceAlreadyBoundException(code: Int, message: String) : ApiErrorException(code, message)

class DeviceNotExistException(code: Int, message: String) : ApiErrorException(code, message)

class DeviceNotSupportException(code: Int, message: String) : ApiErrorException(code, message)