package com.gwchina.parent.net.presentation.netManagement.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.view.ViewGroup

class NetManagementPageAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    lateinit var mCurrentFragment: Fragment

    override fun getItem(p0: Int): Fragment {
        return NetMnagementFragmentFactory.createFragment(p0)
    }

    override fun getCount(): Int {
        return 2
    }


    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        mCurrentFragment = `object` as Fragment
        super.setPrimaryItem(container, position, `object`)
    }

}