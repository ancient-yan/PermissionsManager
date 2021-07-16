package com.gwchina.parent.main

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.view.View
import com.android.base.app.dagger.Injectable
import com.android.base.app.fragment.BaseFragment
import com.android.base.app.fragment.FragmentInfo
import com.android.base.app.fragment.TabManager
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.common.MainEventCenter
import com.gwchina.parent.main.presentation.home.HomeFragment
import com.gwchina.parent.main.presentation.mine.MineFragment
import com.roughike.bottombar.BottomBar
import io.reactivex.Observable
import kotlinx.android.synthetic.main.main_fragment_root.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-01-19 17:53
 */
class MainFragment : BaseFragment(), Injectable {

    private lateinit var tabManager: TabManager
    private lateinit var bottomBar: BottomBar

    @Inject lateinit var mainEventCenter: MainEventCenter

    interface MainFragmentChild

    override fun provideLayout() = R.layout.main_fragment_root

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        bottomBar = bottomBarMain
        initViews(savedInstanceState)
        subscribeViewModel()
    }

    private fun initViews(savedInstanceState: Bundle?) {
        //tab manager
        tabManager = MainTabManager(requireContext(), childFragmentManager, R.id.flMainContainer)
        tabManager.setup(savedInstanceState)

        //bottomBar
        bottomBar.setOnTabSelectListener { tabId ->
            try {
                tabManager.selectTabById(tabId)
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tabManager.onSaveInstanceState(outState)
    }

    fun selectTabAtPosition(page: Int) {
        try {
            if (page in 0..3) {
                bottomBar.selectTabAtPosition(page, true)
            }
        } catch (e: Exception) {
            Timber.d(e, "selectTabAtPosition page=$page")
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            childFragmentManager.fragments.forEach {
                if (!it.isHidden) {
                    it.onHiddenChanged(hidden)
                }
            }
        }
    }

    private fun subscribeViewModel() {
        mainEventCenter.showMineTabRedDotEvent.observe(this, Observer {
            if (true == it) {
                bottomBar.getTabAtPosition(1).setBadgeContent("")
            } else {
                bottomBar.getTabAtPosition(1).removeBadge()
            }
        })
    }

}

private class MainTabManager(
        context: Context,
        fragmentManager: FragmentManager, containerId: Int
) : TabManager(context, fragmentManager, MainTabs(), containerId, SHOW_HIDE) {

    private class MainTabs internal constructor() : TabManager.Tabs() {
        init {
            add(FragmentInfo.PageBuilder()
                    .clazz(HomeFragment::class.java)
                    .tag(HomeFragment::class.java.name)
                    .toStack(false)
                    .pagerId(R.id.main_home)
                    .build())

            add(FragmentInfo.PageBuilder()
                    .clazz(MineFragment::class.java)
                    .tag(MineFragment::class.java.name)
                    .toStack(false)
                    .pagerId(R.id.main_mine)
                    .build())
        }
    }

}