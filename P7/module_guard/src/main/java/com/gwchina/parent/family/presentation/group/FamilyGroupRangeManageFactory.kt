package com.gwchina.parent.family.presentation.group

import android.os.Bundle
import android.support.v4.app.Fragment
import java.util.*

object FamilyGroupRangeManageFactory {

    private val mFragments = WeakHashMap<Int, Fragment>()

    fun createFragment(position: Int): Fragment {
        var fragment = mFragments.get(position)
        if (fragment == null) {
            when (position) {
                0 -> {
                    fragment = GroupRangeSettingFragment()

                    val bundle = Bundle()
                    bundle.putString("type", GroupRangeSettingFragment.CALL)
                    fragment.arguments = bundle
                    mFragments[position] = fragment!!
                }
                else -> {
                    fragment = GroupRangeSettingFragment()
                    val bundle = Bundle()
                    bundle.putString("type", GroupRangeSettingFragment.ANSWER)
                    fragment.arguments = bundle
                    mFragments[position] = fragment!!
                }
            }
        }
        return fragment
    }

    fun clearData() {
        mFragments.clear()
    }
}