package com.gwchina.parent.main.presentation.mine

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.View
import com.android.base.app.mvvm.getViewModel
import com.android.base.data.onSuccessWithData
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.MainFragment
import com.gwchina.parent.main.MainNavigator
import com.gwchina.parent.main.common.MainEventCenter
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.data.api.isFlagPositive
import com.gwchina.sdk.base.data.models.logined
import kotlinx.android.synthetic.main.mine_fragment.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.min

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2018-11-02 14:40
 */
class MineFragment : InjectorBaseFragment(), MainFragment.MainFragmentChild {

    @Inject lateinit var mainNavigator: MainNavigator
    @Inject lateinit var mainEventCenter: MainEventCenter

    private val viewModule by lazy {
        getViewModel<MineViewModel>(viewModelFactory)
    }

    private lateinit var uiPresenter: MineUIPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeViewModel()
    }

    override fun provideLayout() = R.layout.mine_fragment

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        uiPresenter = MineUIPresenter(this)
        installScrollingVisibleTitleBar {
            flMineTitleLayout.alpha = it
            clMineTitleContent.alpha = (1F - min(1F, (it * 1.25F)))
        }
    }

    override fun onResume() {
        super.onResume()
        doRefreshIfVisible()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        doRefreshIfVisible()
    }

    private fun doRefreshIfVisible() {
        if (!isHidden) {
            viewModule.refreshPageInfo()
        }
    }

    private fun subscribeViewModel() {
        viewModule.user
                .observe(this, Observer {
                    Timber.d("subscribeViewModel $it")
                    it ?: return@Observer
                    when {
                        //登录，且至少有一个孩子
                        it.childList?.isNullOrEmpty() == false -> uiPresenter.showChildCards(it)
                        //仅仅是登录
                        it.logined() -> uiPresenter.showLoginedButNotBound(it)
                        //未登录
                        else -> uiPresenter.showLogout()
                    }
                })

        viewModule.minePageResponse
                .observe(this, Observer {
                    it?.onSuccessWithData { response ->
                        uiPresenter.showPageInfo(response)
                        mainEventCenter.setMineTabRedDotVisible(isFlagPositive(response.exist_notification) || isFlagPositive(response.exist_report))
                    }
                })
    }

}