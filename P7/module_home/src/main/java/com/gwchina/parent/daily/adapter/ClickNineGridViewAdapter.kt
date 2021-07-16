package com.gwchina.parent.daily.adapter

import android.content.Context
import com.lzy.ninegrid.ImageInfo
import com.lzy.ninegrid.NineGridView
import com.lzy.ninegrid.NineGridViewAdapter

/**
 *@author hujl
 *@Email: hujlin@163.com
 *@Date : 2019-08-06 14:12
 */
class ClickNineGridViewAdapter(context: Context, imageInfo: List<ImageInfo>, var onImageItemClick: (Int, ArrayList<String>) -> Unit) : NineGridViewAdapter(context, imageInfo) {

    companion object {
        fun generateImageInfoList(pathList: List<String?>?): List<ImageInfo> {
            val imageInfoList = mutableListOf<ImageInfo>()
            pathList?.forEach {
                val imageInfo = ImageInfo()
                imageInfo.bigImageUrl = it
                imageInfo.thumbnailUrl = it
                imageInfoList.add(imageInfo)
            }
            return imageInfoList
        }
    }

    override fun onImageItemClick(context: Context?, nineGridView: NineGridView?, index: Int, imageInfo: MutableList<ImageInfo>?) {
        super.onImageItemClick(context, nineGridView, index, imageInfo)
        val list = imageInfo?.map {
            it.bigImageUrl
        } as ArrayList<String>
        onImageItemClick(index, list)
    }

}
