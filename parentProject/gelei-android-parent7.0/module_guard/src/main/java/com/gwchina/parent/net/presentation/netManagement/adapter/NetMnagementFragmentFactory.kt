package com.gwchina.parent.net.presentation.netManagement.adapter

import android.os.Bundle
import android.support.v4.app.Fragment
import com.gwchina.parent.net.presentation.netManagement.fragment.URLManagementFragment
import java.util.*

object NetMnagementFragmentFactory {

    private val mFragments = WeakHashMap<Int, Fragment>()

    fun createFragment(position: Int): Fragment {
        var fragment = mFragments[position]
        if (fragment == null) {
            when (position) {
                0 -> {
                    fragment = URLManagementFragment()

                    val bundle = Bundle()
                    bundle.putString("type",URLManagementFragment.BLACKLIST)
                    fragment.arguments = bundle
                    mFragments[position] = fragment
                }
                else -> {
                    fragment = URLManagementFragment()
                    val bundle = Bundle()
                    bundle.putString("type",URLManagementFragment.WHITELIST)
                    fragment.arguments = bundle
                    mFragments[position] = fragment
                }
            }
        }
        return fragment
    }

    fun clearData() {
        mFragments.clear()
    }
}