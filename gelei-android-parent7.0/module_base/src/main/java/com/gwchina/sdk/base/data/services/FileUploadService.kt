package com.gwchina.sdk.base.data.services

import com.android.base.rx.SchedulerProvider
import com.android.sdk.net.kit.create
import com.android.sdk.net.kit.resultChecker
import com.android.sdk.net.kit.resultExtractor
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.sdk.base.data.api.ApiHelper
import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.api.HttpResult
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*
import java.io.File

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-15 12:27
 */
interface FileUploadService {

    fun uploadFiles(fileParts: List<String>): Single<List<FileInfo>>

}

data class FileInfo(
        val file_name: String = "",
        val file_size: Int = 0,
        val file_type: String = "",
        val hash: String = "",
        val url: String = "",
        internal var index: Int = 0
)

internal interface FileUploadApi {

    //文件秒传（验证文件hash值，判断是否已上传。）
    @FormUrlEncoded
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @POST("/file/data/secondpass")
    fun checkFileIsExist(@Field("hash") hash: String): Flowable<FileCheckHttpResult<FileInfo>>

    //@Part file: MultipartBody.Part, @Part("hash") text: RequestBody
    //文件上传（验证文件hash值，判断是否已上传。）
    @Headers(ApiParameter.WITH_APP_TOKEN)
    @Multipart
    @POST("/file/data/upload")
    fun uploadFile(@PartMap parts: Map<String, @JvmSuppressWildcards RequestBody>): Flowable<HttpResult<FileInfo>>

}

internal class FileCheckHttpResult<T> : HttpResult<T>() {

    override fun isSuccess(): Boolean {
        return super.isSuccess() || code == ApiHelper.CODE_PENDING_UPLOAD
    }

}

internal class FileUploadServiceImpl(
        serviceFactory: ServiceFactory,
        private val schedulerProvider: SchedulerProvider
) : FileUploadService {

    private val fileUploadApi = serviceFactory.create<FileUploadApi>()

    companion object {
        private const val MAX_PARALLEL_COUNT = 3
        private const val HASH_KEY = "hash"
        private const val FILE_KEY = "file"
    }

    override fun uploadFiles(fileParts: List<String>): Single<List<FileInfo>> {

        val indexFiles = fileParts.mapIndexed { index, path -> Pair(index, path) }

        return Flowable.fromIterable(indexFiles)
                .subscribeOn(schedulerProvider.io())
                .map {

                    val file = File(it.second)
                    Triple(it.first, file, ApiParameter.fileHash(file))

                }.flatMap({
                    fileUploadApi.checkFileIsExist(it.third)
                            .resultChecker()
                            .flatMap { result ->
                                if (result.code == ApiHelper.CODE_PENDING_UPLOAD) {
                                    fileUploadApi.uploadFile(buildBody(it.second, it.third)).resultExtractor()
                                } else {
                                    Flowable.just(result.data)
                                }
                            }.doOnNext { fileInfo ->
                                fileInfo.index = it.first
                            }
                }, MAX_PARALLEL_COUNT)
                .toList()
                .doOnSuccess {
                    it.sortBy { fileInfo -> fileInfo.index }
                }
    }

    private fun buildBody(file: File, hash: String): Map<String, RequestBody> = ApiParameter.buildMultiPartRequestBody(mapOf(HASH_KEY to hash), mapOf(FILE_KEY to file))

}