package com.gwchina.parent.net.presentation.netManagement.fragment

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.android.base.utils.android.compat.SystemBarCompat
import com.blankj.utilcode.util.ColorUtils.getColor
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.net.common.NetEventCenter
import com.gwchina.parent.net.presentation.netManagement.adapter.NetManagementPageAdapter
import com.gwchina.parent.net.presentation.netManagement.adapter.NetMnagementFragmentFactory
import com.gwchina.sdk.base.app.InjectorBaseFragment
import com.gwchina.sdk.base.third.umeng.StatisticalManager
import com.gwchina.sdk.base.third.umeng.UMEvent
import kotlinx.android.synthetic.main.net_fragemnt_management.*
import javax.inject.Inject


/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-17 10:34
 *      Desc : 上网管理
 */
class NetWorkManagementFragment : InjectorBaseFragment() {

    private val adapter: NetManagementPageAdapter by lazy { NetManagementPageAdapter(childFragmentManager) }

    private var mCurrentIndex: Int = 0

    @Inject
    lateinit var netEventCenter: NetEventCenter

    override fun provideLayout(): Any? = R.layout.net_fragemnt_management

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        common_toolbar.apply {
            setNavigationOnClickListener {
                exitFragment(false)
            }
            inflateMenu(R.menu.net_delete_url_menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.net_delete_url -> {
                        netEventCenter.notifyNetDeleteTypeUpdated(if (mCurrentIndex == 0) URLManagementFragment.BLACKLIST else URLManagementFragment.WHITELIST)
                        if(mCurrentIndex == 0) {
                            StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_WEBBLACK_DELETE)
                        } else {
                            StatisticalManager.onEvent(UMEvent.ClickEvent.GUARDWEB_BTN_WEBWHITE_DELETE)
                        }
                    }
                }
                true
            }
        }

        net_vp_fragment.adapter = adapter

        net_tv_blacklist_title.setOnClickListener {
            net_vp_fragment.setCurrentItem(0, true)
            updateTabIndex(0)
        }


        net_tv_whitelist_title.setOnClickListener {
            net_vp_fragment.setCurrentItem(1, true)
            updateTabIndex(1)
        }


        net_vp_fragment.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                updateTabIndex(position)
            }

            override fun onPageScrollStateChanged(p0: Int) {}
        })

    }

    private fun updateTabIndex(position: Int) {
        mCurrentIndex = position
        if (position == 1) {
            net_tv_blacklist_title.setTextColor(getColor(R.color.gray_level2))
            net_view_blacklist_index.setBackgroundColor(getColor(R.color.white))

            net_tv_whitelist_title.setTextColor(getColor(R.color.green_level1))
            net_view_whitelist_index.setBackgroundColor(getColor(R.color.green_level1))
        } else {
            net_tv_blacklist_title.setTextColor(getColor(R.color.green_level1))
            net_view_blacklist_index.setBackgroundColor(getColor(R.color.green_level1))

            net_tv_whitelist_title.setTextColor(getColor(R.color.gray_level2))
            net_view_whitelist_index.setBackgroundColor(getColor(R.color.white))
        }

    }

    override fun onResume() {
        super.onResume()
        SystemBarCompat.setStatusBarColor(activity, getColor(R.color.white))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        NetMnagementFragmentFactory.clearData()
    }

}