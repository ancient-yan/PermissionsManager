package com.gwchina.parent.recommend.data

import com.android.base.app.dagger.ActivityScope
import com.android.sdk.net.kit.optionalExtractor
import com.android.sdk.net.kit.resultChecker
import com.github.dmstocking.optional.java.util.Optional
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 11:40
 */
@ActivityScope
class RecommendRepository @Inject constructor(
        private val mRecommendApi: RecommendApi
) {

    fun recommendList(
            childUserId: String,
            childDeviceId: String,
            recommendId: String
    ): Flowable<Optional<RecommendResponse>> {
        return mRecommendApi.loadRecommendList(
                childUserId,
                childDeviceId,
                recommendId
        ).optionalExtractor()
    }

    fun installAppForChild(childUserId: String, childDeviceId: String, softName: String, bundleId: String): Completable {
        return mRecommendApi.installAppForChild(
                childUserId,
                childDeviceId,
                softName,
                bundleId,
                "1"/*推荐来源界面 rec_source ，1 表示应用推荐*/
        ).resultChecker().ignoreElements()
    }

    fun loadSubjectDetail(rec_subject_id: String, child_user_id: String, childDeviceId: String): Observable<Optional<SubjectDetailResponse>> {
        return mRecommendApi.loadSubjectDetail(rec_subject_id, child_user_id, childDeviceId).optionalExtractor()
    }

}