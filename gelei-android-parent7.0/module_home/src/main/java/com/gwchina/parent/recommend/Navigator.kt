package com.gwchina.parent.recommend

import com.android.base.app.fragment.inFragmentTransaction
import com.gwchina.parent.recommend.data.SoftItem
import com.gwchina.parent.recommend.presentation.AppDetailFragment
import com.gwchina.parent.recommend.presentation.SubjectDetailFragment
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 19:15
 */
class Navigator @Inject constructor(private val activity: RecommendActivity) {

    fun showAppDetail(soft: SoftItem) {
        activity.inFragmentTransaction {
            replaceWithStack(fragment = AppDetailFragment.newInstance(soft))
        }
    }

    fun showSubjectDetail(subjectId: String) {
        activity.inFragmentTransaction {
            replaceWithStack(fragment = SubjectDetailFragment.newInstance(subjectId))
        }
    }

}
