package com.gwchina.parent.recommend.presentation

import android.annotation.SuppressLint
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.LiveDataReactiveStreams
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations
import android.content.Context
import com.android.base.app.aac.SingleLiveData
import com.android.base.app.dagger.ContextType
import com.android.base.app.mvvm.ArchViewModel
import com.android.base.data.Resource
import com.android.base.rx.SchedulerProvider
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.recommend.data.*
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.models.currentChildDeviceId
import com.gwchina.sdk.base.data.models.currentChildId
import io.reactivex.Completable
import io.reactivex.Observable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 11:15
 */
class RecommendViewModel @Inject constructor(
        private val recommendRepository: RecommendRepository,
        private val appDataSource: AppDataSource,
        private val schedulerProvider: SchedulerProvider,
        @ContextType val context: Context
) : ArchViewModel() {

    companion object {
        const val ALL_CATEGORY_ID = "all_category"
    }

    val user = LiveDataReactiveStreams.fromPublisher(appDataSource.observableUser())

    private val _categories = MutableLiveData<Resource<List<Category>>>()
    /**所有分类*/
    val categories: LiveData<Resource<List<Category>>>
        get() = _categories

    private val _grades = MutableLiveData<List<GradeInfo>>()
    /**所有年纪*/
    val grades: LiveData<List<GradeInfo>>
        get() = _grades

    private val _subject = MutableLiveData<List<SubjectInfo>>()
    /**专题数据*/
    val subject: LiveData<List<SubjectInfo>>
        get() = _subject

    /**所有列表数据*/
    private val _softItems = MutableLiveData<List<SoftList>>()

    private val _installEvent = SingleLiveData<String>()
    /**安装状态变更*/
    val installEvent: LiveData<String>
        get() = _installEvent

    private var mCurrentRecommendId: String? = null
    @Volatile private var mIsRefreshing = false

    /**初始加载或者切换年级时进行刷新*/
    fun loadRecommendList(recommendId: String) {
        _categories.postValue(Resource.loading())
        doLoadRecommendList(recommendId)
    }

    /**由列表页触发刷新*/
    fun refreshList() {
        if (mIsRefreshing) {
            return
        }
        mIsRefreshing = true
        val currentRecommendId = mCurrentRecommendId ?: return
        doLoadRecommendList(currentRecommendId)
    }

    @SuppressLint("CheckResult")
    private fun doLoadRecommendList(recommendId: String) {
        mCurrentRecommendId = recommendId

        val childUserId = appDataSource.user().currentChildId ?: return
        val childDeviceId = appDataSource.user().currentChildDeviceId ?: return

        recommendRepository.recommendList(childUserId, childDeviceId, recommendId)
                .subscribe(
                        {
                            distributeData(it.orElse(null))
                            mIsRefreshing = false
                        },
                        {
                            _categories.postValue(Resource.error(it))
                            mIsRefreshing = false
                        }
                )
    }

    fun doLoadSubjectDetail(subjectId: String): Observable<SubjectDetailResponse> {
        val childUserId = appDataSource.user().currentChildId ?: ""
        val childDeviceId = appDataSource.user().currentChildDeviceId ?: ""

        return recommendRepository.loadSubjectDetail(subjectId, childUserId, childDeviceId)
                .subscribeOn(schedulerProvider.io())
                .map {it.get()}
                .observeOn(schedulerProvider.ui())
    }

    private fun distributeData(response: RecommendResponse?) {
        if (_grades.value != response?.system_grade_list) {
            _grades.postValue(response?.system_grade_list)
        }

        if (_subject.value != response?.grade_rec_subject_list) {
            _subject.postValue(response?.grade_rec_subject_list)
        }

        //添加全部 table
        val categoryList = response?.system_subject_soft_list?.map { Category(it.subject_name, it.subject_code) }?.toMutableList()
        if (!categoryList.isNullOrEmpty()) {
            categoryList.add(0, Category(context.getString(R.string.all), ALL_CATEGORY_ID))
        }

        if (categories.value?.orElse(null) != categoryList) {
            _categories.postValue(Resource.success(categoryList))
        }

        //添加全部列表
        val list = response?.system_subject_soft_list?.toMutableList()
        if (!list.isNullOrEmpty()) {
            val allItemList = mutableListOf<SoftItem>()
            list.forEach { allItemList.addAll(it.soft_list) }
            val distinctList = allItemList.distinctBy {
                it.bundle_id
            }
            list.add(0, SoftList(subject_code = ALL_CATEGORY_ID, subject_name = context.getString(R.string.all), soft_list = distinctList))
        }
        _softItems.postValue(list)
    }

    fun installForChild(soft: SoftItem): Completable {
        val childUserId = appDataSource.user().currentChildId ?: ""
        val childDeviceId = appDataSource.user().currentChildDeviceId ?: ""

        return recommendRepository.installAppForChild(childUserId, childDeviceId, soft.soft_name, soft.bundle_id)
                .observeOn(schedulerProvider.ui())
                .doOnComplete {
                    soft.setToInstalling()
                    modifySameIdItemIfHave(soft)
                }
    }

    private fun modifySameIdItemIfHave(soft: SoftItem) {
        val resourceData = _softItems.value ?: return
        resourceData.forEach { list ->
            list.soft_list.forEach sub@{ item ->
                if (soft.bundle_id == item.bundle_id) {
                    item.install_flag = soft.install_flag
                    return@sub
                }
            }
        }

        _installEvent.postValue(soft.bundle_id)
    }

    fun categoryList(listId: String): LiveData<List<SoftItem>> {
        return Transformations.map(_softItems) {
            it.find { list ->
                list.subject_code == listId
            }?.soft_list
        }
    }
}