package com.gwchina.parent.daily.adapter

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import com.android.base.imageloader.ImageLoaderFactory
import com.lzy.ninegrid.NineGridView

/**
 *@author hujl
 *@Email: hujlin@163.com
 *@Date : 2019-08-06 14:14
 */
class GlideImageLoader : NineGridView.ImageLoader {

    override fun getCacheImage(url: String?): Bitmap? {
        return null
    }

    override fun onDisplayImage(context: Context?, imageView: ImageView?, url: String?) {
        context?.let {
            if (imageView != null) {
                ImageLoaderFactory.getImageLoader().display(imageView,url)
            }
        }
    }
}