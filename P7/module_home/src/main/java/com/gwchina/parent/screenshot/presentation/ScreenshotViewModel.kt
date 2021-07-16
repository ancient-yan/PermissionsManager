package com.gwchina.parent.screenshot.presentation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.android.base.app.aac.toResourceLiveData
import com.android.base.app.fragment.BaseFragment
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.permission.AutoPermissionRequester
import com.android.base.rx.SchedulerProvider
import com.android.base.utils.android.XIntentUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ToastUtils
import com.github.dmstocking.optional.java.util.Optional
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.screenshot.ScreenshotUtils
import com.gwchina.parent.screenshot.data.ScreenshotData
import com.gwchina.parent.screenshot.data.ScreenshotRepository
import com.gwchina.parent.screenshot.data.ScreenshotStatisticsData
import com.gwchina.parent.screenshot.event.RefreshEventCenter
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.config.DirectoryManager
import com.gwchina.sdk.base.data.utils.OKHttpDownloader
import com.gwchina.sdk.base.widget.dialog.showBottomSheetListDialog
import com.yanzhenjie.permission.runtime.Permission
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 11:15
 */
class ScreenshotViewModel @Inject constructor(
        private val screenshotRepository: ScreenshotRepository,
        internal val refreshEventCenter: RefreshEventCenter,
        private val schedulerProvider: SchedulerProvider
) : ArchViewModel() {

    private var disposable: Disposable? = null

    private val _screenshotItem = MutableLiveData<Resource<ScreenshotData>>()
    val screenshotItem: LiveData<Resource<ScreenshotData>>
        get() = _screenshotItem

    private val _screenshotList = MutableLiveData<Resource<List<ScreenshotData>>>()
    val screenshotList: LiveData<Resource<List<ScreenshotData>>>
        get() = _screenshotList

    private val _screenStatisticsData = MutableLiveData<Resource<ScreenshotStatisticsData>>()
    val screenStatisticsData: LiveData<Resource<ScreenshotStatisticsData>>
        get() = _screenStatisticsData

    /**
     * 获取截屏列表
     */
    fun getScreenshotList() {
        _screenshotList.value = Resource.loading()
        screenshotRepository.screenPicList()?.subscribeOn(schedulerProvider.io())?.observeOn(schedulerProvider.ui())?.autoDispose()?.subscribe({
            _screenshotList.value = Resource.success(it.get())
        }, {
            _screenshotList.value = Resource.error(it)
        })
    }

    /**
     * 根据截屏id查询截屏结果
     */
    private fun getScreenshotDetailById(recordId: String, recordTime: Long?) {
        if (recordTime != null && abs(recordTime - System.currentTimeMillis()) > 30 * 1000) {
            disposable?.dispose()
            disposable = null
            _screenshotItem.postValue(Resource.error(Throwable("哎呀，截图失败了")))
            return
        }
        screenshotRepository.picDetail(recordId)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui()).autoDispose().subscribe(
                        {
                            _screenshotItem.value = Resource.success(it.get())
                            //截屏成功
                            if (it.get() != null && it.get().pic_hash != null) {
                                disposable?.dispose()
                                disposable = null
                                ScreenshotUtils.putLastScreenParams(recordId, true)
                            }
                        }, {
                    disposable?.dispose()
                    disposable = null
//                    _screenshotItem.value = Resource.error(it)
                    _screenStatisticsData.value = Resource.error(it)

                })
    }

    /**
     * 轮询截屏结果
     * 30s 每3s查询一次
     */
    fun getScreenshotDetailInterval(recordId: String, recordTime: Long? = null) {
        if (disposable != null && disposable!!.isDisposed) {
            disposable = null
        }
        if (disposable == null) {
            disposable = Observable.intervalRange(1, 10, 1, 3, TimeUnit.SECONDS)
                    .doOnComplete {
                        _screenshotItem.postValue(Resource.error(Throwable("哎呀，截图失败了")))
                    }.subscribe {
                        getScreenshotDetailById(recordId, recordTime)
                    }
        }
    }

    /**
     * 开始截屏
     */
    fun addScreenshot() {
        _screenshotItem.value = Resource.loading()
        screenshotRepository.addScreenshot()
                ?.subscribeOn(schedulerProvider.io())
                ?.observeOn(schedulerProvider.ui())
                ?.autoDispose()
                ?.subscribe(
                        {
                            it.get().record_id?.let { record ->
                                ScreenshotUtils.putLastScreenParams(record, false)
                                getScreenshotDetailInterval(record)
                            }
                        }, {
                    if (it.message != null && it.message!!.contains("设备离线")) {
                        _screenshotItem.value = Resource.error(it)
                    } else {
                        _screenStatisticsData.value = Resource.error(it)
                    }
                })
    }

    /**
     * 截屏次数（开始截屏之前调用）
     */
    fun screenStatisticsData(isFromPage: Boolean) {
        screenshotRepository.screenshotStatisticsData()
                ?.subscribeOn(schedulerProvider.io())
                ?.observeOn(schedulerProvider.ui())
                ?.autoDispose()
                ?.subscribe({
                    _screenStatisticsData.value = Resource.success(it.get().copy(isOpenPage = isFromPage))
                }, {
                    _screenStatisticsData.value = Resource.error(it)
                })
    }


    /**
     * 删除截屏
     */
    fun delPicList(picList: List<String>): LiveData<Resource<Optional<String>>> {
        return screenshotRepository
                .delPicList(picList)
                .subscribeOn(schedulerProvider.io())
                .autoDispose()
                .toResourceLiveData()
    }

    /**
     * 退出页面的时候
     */
    fun disposable() {
        disposable?.dispose()
        disposable = null
    }

    /**
     * 保存图片到本地
     */
    private var tempFile: File? = null

    fun savePic(imageUrl: String, fragment: BaseFragment) {
        val tempPath = DirectoryManager.createTempPicturePath(FileUtils.getFileExtension(imageUrl))
        tempFile = File(tempPath)
        OKHttpDownloader.download(imageUrl, tempFile).map {
            BitmapFactory.decodeFile(it)
        }.subscribe { bitmap ->
            AutoPermissionRequester.with(fragment)
                    .permission(Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)
                    .onGranted {
                        bitmap?.let { it1 -> savePic(fragment, it1) }
                    }
                    .request()
        }
    }

    private fun savePic(host: BaseFragment, bitmap: Bitmap) {
        host.showBottomSheetListDialog {
            spaceViewGone = true
            items = listOf(host.getString(R.string.home_save_pic))
            actionTextId = R.string.cancel_
            itemSelectedListener = { position, _ ->
                if (position == 0) {
                    saveBitmapFile(host, bitmap)
                }
            }
        }
    }

    private fun saveBitmapFile(fragment: BaseFragment, bitmap: Bitmap) {
        fragment.showLoadingDialog()
        Flowable.just(bitmap).subscribeOn(AppContext.schedulerProvider().io()).map { bitmap1 ->
            val storePath = DirectoryManager.createDCIMPictureStorePath(DirectoryManager.createTempFileName(DirectoryManager.PICTURE_FORMAT_JPEG))
            ImageUtils.save(bitmap1, storePath, Bitmap.CompressFormat.PNG)
            storePath
        }.observeOn(AppContext.schedulerProvider().ui()).subscribe(
                { path ->
                    fragment.dismissLoadingDialog()
                    showImageSaveSuccess(fragment, path)
                },
                {
                    fragment.dismissLoadingDialog()
                    ToastUtils.showLong(fragment.context?.getString(R.string.image_save_fail_tips))
                })
    }

    private fun showImageSaveSuccess(mFragment: BaseFragment, path: String) {
        mFragment.showMessage(mFragment.getString(com.app.base.R.string.image_save_success_tips))
        val context = mFragment.context
        if (context != null) {
            XIntentUtils.notifyImageSaved(context, path)
        }
        tempFile?.deleteOnExit()
    }
}