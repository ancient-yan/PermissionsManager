package com.gwchina.parent.family.presentation.home

import android.os.Bundle
import android.view.View
import com.android.base.app.fragment.BaseFragment
import com.android.base.app.fragment.exitFragment
import com.gwchina.lssw.parent.guard.R
import kotlinx.android.synthetic.main.family_iphone_page_layout.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-21 11:23
 */
class FamilyIphonePageFragment : BaseFragment() {

    override fun provideLayout(): Any? {
        return R.layout.family_iphone_page_layout
    }

    override fun onViewPrepared(view: View, savedInstanceState: Bundle?) {
        super.onViewPrepared(view, savedInstanceState)
        finish.setOnClickListener { activity?.exitFragment(false) }
    }
}