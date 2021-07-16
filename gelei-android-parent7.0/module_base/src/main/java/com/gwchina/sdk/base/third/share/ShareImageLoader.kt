package com.gwchina.sdk.base.third.share

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.android.sdk.cache.DiskLruStorageFactoryImpl
import com.android.sdk.cache.Storage
import com.blankj.utilcode.util.FileUtils
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.config.DirectoryManager
import com.gwchina.sdk.base.data.utils.OKHttpDownloader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-09-06 10:27
 *      微信的话只接收字节数组，QQ分享只接收图片的url或者本地图片路径
 */
interface ShareImageLoader {
    fun displayNull()

    fun displayUrl(url: String)

    fun displayLocalPath(localPath: String)

    fun displayResource(resourceId: Int)

}

class WeChatShareImageLoader(val result: (ByteArray?) -> Unit) : ShareImageLoader {

    companion object {
        val map = mutableMapOf<Any, ByteArray>()
        val diskLruCache: Storage = DiskLruStorageFactoryImpl().newBuilder(AppContext.getContext()).build()
    }

    override fun displayNull() {
        result(null)
    }

    override fun displayLocalPath(localPath: String) {
        if (map[localPath] != null) {
            result(map[localPath]!!)
            return
        }
        val file = File(localPath)
        if (!file.exists()) return
        val byteArray = file.readBytes()
        map[localPath] = byteArray
        result(byteArray)
    }

    override fun displayResource(resourceId: Int) {
        if (map[resourceId] != null) {
            result(map[resourceId]!!)
            return
        }
        val bitmap = BitmapFactory.decodeResource(AppContext.getContext().resources, resourceId)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val byteArray = baos.toByteArray()
        map[resourceId] = byteArray
        result(byteArray)
        bitmap.recycle()
    }

    override fun displayUrl(url: String) {
        //内存中有
        if (map[url] != null) {
            result(map[url]!!)
            return
        }
        //FileLruCache
        if (!diskLruCache.getString(url).isNullOrEmpty()) {
            val file = File(diskLruCache.getString(url))
            if (file.exists()) {
                result(file.readBytes())
                map[url] = file.readBytes()
                return
            }
        }
        //去网络下载 微信图片大小限制在32k否则调不起分享页而且没有提示
        val tempPath = DirectoryManager.createTempPicturePath(FileUtils.getFileExtension(url))
        val tempFile = File(tempPath)
        if (!tempFile.exists()) {
            tempFile.parentFile.mkdirs()
            tempFile.createNewFile()
        }
        OKHttpDownloader.download(url, tempFile)
                .subscribeOn(AppContext.schedulerProvider().io())
                .observeOn(AppContext.schedulerProvider().ui())
                .subscribe({
                    val file = File(it)
                    if (file.length() >= 32 * 1024) {
                        result(null)
                    } else {
                        val byteArray = file.readBytes()
                        result(byteArray)
                        map[url] = byteArray
                        diskLruCache.putString(url, it)
                    }
                }, {
                    result(null)
                })
    }
}

class QQShareImageLoader(val result: (String?, Int) -> Unit) : ShareImageLoader {

    companion object {
        const val REMOTE_URL = 0
        const val LOCAL_PATH = 1
        val map = mutableMapOf<Any, String>()
        val diskLruCache: Storage = DiskLruStorageFactoryImpl().newBuilder(AppContext.getContext()).build()
    }

    override fun displayNull() {
        result(null, LOCAL_PATH)
    }


    override fun displayUrl(url: String) {
        result(url, REMOTE_URL)
    }

    override fun displayLocalPath(localPath: String) {
        result(localPath, LOCAL_PATH)
    }

    override fun displayResource(resourceId: Int) {
        if (map[resourceId] != null) {
            result(map[resourceId]!!, LOCAL_PATH)
            return
        }
        if (diskLruCache.getString(resourceId.toString()) != null) {
            val path = diskLruCache.getString(resourceId.toString())
            if (!path.isNullOrEmpty()) {
                val file = File(path)
                if (file.exists()) {
                    result(file.absolutePath, LOCAL_PATH)
                    map[resourceId] = file.absolutePath
                    return
                }
            }
        }
        val bitmap = BitmapFactory.decodeResource(AppContext.getContext().resources, resourceId)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val tempFilePath = DirectoryManager.createTempPicturePath(DirectoryManager.PICTURE_FORMAT_PNG)
        val tempFile = File(tempFilePath)
        if (!tempFile.exists()) {
            tempFile.parentFile.mkdirs()
            tempFile.createNewFile()
        }
        val fos = FileOutputStream(tempFile)
        fos.write(baos.toByteArray())
        map[resourceId] = tempFilePath
        result(tempFilePath, LOCAL_PATH)
        bitmap.recycle()
    }

}

