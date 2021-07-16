package com.gwchina.parent.family.presentation.group

import android.graphics.Typeface.*
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.View
import com.android.base.app.fragment.exitFragment
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.presentation.group.adapter.FamilyGroupRangeManagePageAdapter
import com.gwchina.sdk.base.app.InjectorBaseFragment
import kotlinx.android.synthetic.main.family_fragment_group_range_manage.*

/**
 *@author TianZH
 *      Email: tianzh@txtws.com
 *      Date : 2019-07-23 15:41
 *      Desc :  分组拨打接听管理
 */
class GroupRangeManageFragment : InjectorBaseFragment() {

    private val adapter: FamilyGroupRangeManagePageAdapter by lazy { FamilyGroupRangeManagePageAdapter(childFragmentManager) }

    private var mCurrentIndex: Int = 0

    override fun provideLayout(): Any? = R.layout.family_fragment_group_range_manage

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        common_toolbar.apply {
            setNavigationOnClickListener {
                exitFragment(false)
            }
        }
        vpFamilyGroupManageFragment.adapter = adapter

        tvFamilyCallRangeTitle.setOnClickListener {
            vpFamilyGroupManageFragment.setCurrentItem(0, true)
            updateTabIndex(0)
        }


        tvFamilyAnswerRangeTitle.setOnClickListener {
            vpFamilyGroupManageFragment.setCurrentItem(1, true)
            updateTabIndex(1)
        }


        vpFamilyGroupManageFragment.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
            tvFamilyCallRangeTitle.setTextColor(resources.getColor(R.color.gray_level2))
            tvFamilyCallRangeTitle.typeface= defaultFromStyle(NORMAL)
            viewFamilyCallRangeIndex.setBackgroundColor(resources.getColor(R.color.white))

            tvFamilyAnswerRangeTitle.setTextColor(resources.getColor(R.color.green_level1))
            tvFamilyAnswerRangeTitle.typeface= defaultFromStyle(BOLD)
            viewFamilyAnswerRangeIndex.setBackgroundColor(resources.getColor(R.color.green_level1))
        } else {
            tvFamilyCallRangeTitle.setTextColor(resources.getColor(R.color.green_level1))
            tvFamilyCallRangeTitle.typeface= defaultFromStyle(BOLD)
            viewFamilyCallRangeIndex.setBackgroundColor(resources.getColor(R.color.green_level1))

            tvFamilyAnswerRangeTitle.setTextColor(resources.getColor(R.color.gray_level2))
            tvFamilyAnswerRangeTitle.typeface= defaultFromStyle(NORMAL)
            viewFamilyAnswerRangeIndex.setBackgroundColor(resources.getColor(R.color.white))
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        FamilyGroupRangeManageFactory.clearData()
    }

}