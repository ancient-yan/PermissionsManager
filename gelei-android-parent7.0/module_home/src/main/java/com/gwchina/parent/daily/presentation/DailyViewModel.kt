package com.gwchina.parent.daily.presentation

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.RxKit
import com.android.base.rx.SchedulerProvider
import com.gwchina.parent.daily.data.CommentData
import com.gwchina.parent.daily.data.DailyMessageListBean
import com.gwchina.parent.daily.data.DailyRecord
import com.gwchina.parent.daily.data.DailyRepository
import com.gwchina.parent.daily.event.RefreshEventCenter
import com.gwchina.parent.daily.widget.ReplyView
import com.gwchina.sdk.base.config.DEFAULT_PAGE_SIZE
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.Child
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import javax.inject.Inject

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-13 11:53
 */
class DailyViewModel @Inject constructor(
        val dailyRepository: DailyRepository,
        val appDataSource: AppDataSource,
        private val schedulerProvider: SchedulerProvider,
        internal val refreshEventCenter: RefreshEventCenter
) : ArchViewModel() {

    private var dailyCommentDisposable: CompositeDisposable? = null
    private var dailyPublishDisposable: CompositeDisposable? = null
    private var dailyDeleteDisposable: CompositeDisposable? = null


    //回复
    private val _dailyCommentRecord = MutableLiveData<Resource<CommentData>>()
    val dailyCommentRecord: LiveData<Resource<CommentData>>
        get() = _dailyCommentRecord

    //发布
    private val _dailyPublish = MutableLiveData<Resource<String>>()
    val dailyPublish: MutableLiveData<Resource<String>>
        get() = _dailyPublish

    //删除
    private val _dailyDelete = MutableLiveData<Resource<Int>>()
    val dailyDelete: MutableLiveData<Resource<Int>>
        get() = _dailyDelete


    private fun dailyCommentDisposableIfNeed(): CompositeDisposable {
        val compositeDisposable = RxKit.newCompositeIfUnsubscribed(dailyCommentDisposable)
        this.dailyCommentDisposable = compositeDisposable
        return compositeDisposable
    }

    private fun dailyPublishDisposableIfNeed(): CompositeDisposable {
        val compositeDisposable = RxKit.newCompositeIfUnsubscribed(dailyPublishDisposable)
        this.dailyPublishDisposable = compositeDisposable
        return compositeDisposable
    }

    private fun dailyDeleteDisposableIfNeed(): CompositeDisposable {
        val compositeDisposable = RxKit.newCompositeIfUnsubscribed(dailyDeleteDisposable)
        this.dailyDeleteDisposable = compositeDisposable
        return compositeDisposable
    }

    fun dailyRecord(page: Int = 0, pageNum: Int = DEFAULT_PAGE_SIZE): Observable<DailyRecord> {
        return dailyRepository.dailyRecord(page, pageNum).subscribeOn(schedulerProvider.io()).map { it.get() }.observeOn(schedulerProvider.ui())
    }

    fun deleteDaily(dailyId: String, position: Int) {
        _dailyDelete.postValue(Resource.loading())
        dailyRepository.deleteDaily(dailyId).subscribe({
            _dailyDelete.postValue(Resource.success(position))
        }, {
            _dailyDelete.postValue(Resource.error(it))
        }).addTo(dailyDeleteDisposableIfNeed())
    }

    fun commentDaily(life_record_id: String, content: String, reply_comment_id: String = "", itemPosition: Int, commentPosition: Int = -1, replyPosition: Int = -1, @ReplyView.RePlyType type: Int, cacheKey: String) {
        _dailyCommentRecord.postValue(Resource.loading())
        dailyRepository.commentDaily(life_record_id, content, reply_comment_id).subscribe({
            val commentResult = it.get()
            _dailyCommentRecord.postValue(Resource.success(CommentData(commentResult, itemPosition, commentPosition, replyPosition, type, cacheKey)))
        }, {
            _dailyCommentRecord.postValue(Resource.error(it))
        }).addTo(dailyCommentDisposableIfNeed())
    }

    fun getMessageList(pageStart: Int = 0, pageNum: Int = DEFAULT_PAGE_SIZE): Observable<List<DailyMessageListBean>> {
        return dailyRepository.dailyMessageList(pageStart, pageNum)
                .subscribeOn(schedulerProvider.io())
                .map {
                    it.get()
                }
                .observeOn(schedulerProvider.ui())
    }

    fun publishDaily(picList: List<String>, receiverChildrenId: List<String>, dailyContent: String) {
//        _dailyPublish.postValue(Resource.loading())
        dailyRepository.publishDaily(picList, receiverChildrenId, dailyContent).subscribe({
            _dailyPublish.postValue(Resource.success())
        }, {
            _dailyPublish.postValue(Resource.error(it))
        }).addTo(dailyPublishDisposableIfNeed())
    }

    override fun onCleared() {
        super.onCleared()
        dailyCommentDisposable?.dispose()
        dailyPublishDisposable?.dispose()
        dailyDeleteDisposable?.dispose()
    }

    //孩子列表
    fun getChildData(): List<Child>? {
        val user = appDataSource.user()
        return user.childList
    }

    fun getUserId(): String {
        return appDataSource.user().patriarch.user_id
    }
}