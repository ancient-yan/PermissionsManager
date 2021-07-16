package com.gwchina.sdk.base.utils

import android.support.v4.app.Fragment
import android.widget.ImageView
import com.android.base.imageloader.DisplayConfig
import com.android.base.imageloader.ImageLoader
import com.app.base.R


private val appDisplayConfig = DisplayConfig
        .create()
        .setErrorPlaceholder(R.drawable.icon_defaule_app)
        .setLoadingPlaceholder(R.drawable.shape_app_icon_default)

private val headPhotoDisplayConfig = DisplayConfig
        .create()
        .setErrorPlaceholder(R.drawable.img_head_father_38)
        .setLoadingPlaceholder(R.drawable.img_head_father_38)

fun ImageLoader.displayAppIcon(fragment: Fragment, imageView: ImageView, url: String?) {
    display(fragment, imageView, url, appDisplayConfig)
}

fun ImageLoader.displayHeadPhotoIcon(fragment: Fragment, imageView: ImageView, url: String?) {
    display(fragment, imageView, url, headPhotoDisplayConfig)
}
