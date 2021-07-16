@file:JvmName("Debug")
package com.gwchina.sdk.base.debug

import com.app.base.BuildConfig

fun isOpenDebug(): Boolean {
    return BuildConfig.openDebug
}

fun ifOpenDebug(action: () -> Unit) {
    if (isOpenDebug()) {
        action()
    }
}