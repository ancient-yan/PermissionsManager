@file:JvmName("GlobalConstants")
package com.gwchina.sdk.base.config

import com.gwchina.sdk.base.AppContext

@Suppress("UNUSED")
const val TRANSITION_ANIMATION_PHOTOS = "transition_animation_photos"

fun appFileProviderAuthorities() = AppContext.getContext().packageName + ".file.provider"

const val DEFAULT_PAGE_START = 1
const val DEFAULT_PAGE_SIZE = 20

const val CHILD_USER_ID_KEY = "child_user_id_key"
const val DEVICE_ID_KEY = "device_id_key"