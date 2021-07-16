package com.gwchina.sdk.base.web

import android.os.Bundle

interface JsCallInterceptor {

    fun intercept(method: String, args: Array<String>?, resultReceiver: ResultReceiver?): Boolean

}

interface BaseCustomJsCallInterceptor : JsCallInterceptor {

    fun onInit(host: BaseWebFragment, bundle: Bundle?)

}
