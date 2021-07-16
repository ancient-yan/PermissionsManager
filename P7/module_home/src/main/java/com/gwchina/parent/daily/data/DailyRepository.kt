package com.gwchina.parent.daily.data

import com.android.base.app.dagger.ActivityScope
import com.android.base.rx.observeOnUI
import com.android.sdk.net.kit.optionalExtractor
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.sdk.base.data.services.FileInfo
import com.gwchina.sdk.base.data.services.ServiceManager
import com.gwchina.sdk.base.utils.ImageCompressor
import io.reactivex.Observable
import io.reactivex.Single
import java.io.File
import javax.inject.Inject

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-13 11:48
 */
@ActivityScope
class DailyRepository @Inject constructor(
        private val dailyApi: DailyApi,
        serviceManager: ServiceManager
) {

    private val fileUploadService = serviceManager.newFileUploadService()

    fun dailyRecord(pos: Int, limit: Int): Observable<Optional<DailyRecord>> {
        return dailyApi.dailyRecord(pos, limit).optionalExtractor()
    }

    fun commentDaily(life_record_id: String, content: String, reply_comment_id: String = ""): Observable<Optional<CommentResult>> {
        return dailyApi.replyDaily(life_record_id, reply_comment_id, content).optionalExtractor()
    }

    fun dailyMessageList(page: Int, pageSize: Int): Observable<Optional<List<DailyMessageListBean>>> {
        return dailyApi.dailyMessageList(page, pageSize).optionalExtractor()
    }

    //删除日记
    fun deleteDaily(dailyId: String): Observable<Optional<String>> {
        return dailyApi.deleteDaily(dailyId).optionalExtractor()
    }

    //文件批量压缩
    fun compressMulti(photoPathList: List<String>): Single<List<File>> {
        return ImageCompressor.compress(photoPathList)
    }

    fun doUpload(pictures: MutableList<String>): Single<List<FileInfo>> {
        return fileUploadService.uploadFiles(pictures).observeOnUI()
    }


    //多个用“,”分开
    fun publishDaily(picList: List<String>, receiverChildrenId: List<String>, dailyContent: String): Observable<Optional<String>> {
        return dailyApi.publishDaily(list2String(receiverChildrenId), list2String(picList), dailyContent).optionalExtractor()
    }

    private fun list2String(list: List<String>): String {
        if (list.isEmpty()) return ""
        return list.joinToString(separator = ",")
    }
}