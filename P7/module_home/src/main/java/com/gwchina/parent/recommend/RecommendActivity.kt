package com.gwchina.parent.recommend

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.android.base.app.fragment.inFragmentTransaction
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.recommend.presentation.RecommendFragment
import com.gwchina.sdk.base.app.InjectorAppBaseActivity
import com.gwchina.sdk.base.router.RouterPath.Recommend
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent

/**
 * 应用推荐
 *
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 10:57
 */
@Route(path = Recommend.PATH)
class RecommendActivity : InjectorAppBaseActivity() {

    @JvmField
    @Autowired(name = Recommend.RECOMMEND_ID_KEY)
    var recommendId: String? = null

    override fun layout() = R.layout.app_base_activity

    override fun setupView(savedInstanceState: Bundle?) {
        super.setupView(savedInstanceState)
        StatisticalManager.onEvent(UMEvent.PageEvent.PAGE_COMMEND)
        savedInstanceState.ifNull {
            inFragmentTransaction {
                addWithDefaultContainer(RecommendFragment.newInstance(recommendId ?: ""))
            }
        }
    }

}