package com.gwchina.parent.family.presentation.group.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.gwchina.parent.family.presentation.group.FamilyGroupRangeManageFactory

class FamilyGroupRangeManagePageAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(p0: Int): Fragment {
        return FamilyGroupRangeManageFactory.createFragment(p0)
    }

    override fun getCount(): Int {
        return 2
    }

}