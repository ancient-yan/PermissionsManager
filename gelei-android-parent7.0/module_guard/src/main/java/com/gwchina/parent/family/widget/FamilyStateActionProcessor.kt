package com.gwchina.parent.family.widget

import android.view.View
import android.widget.TextView
import com.android.base.app.ui.StateLayoutConfig.EMPTY
import com.android.base.widget.StateActionProcessor
import com.gwchina.lssw.parent.guard.R

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-29 17:18
 */
class FamilyStateActionProcessor : StateActionProcessor() {

    private var familyTipView: TextView? = null

    override fun processStateInflated(viewState: Int, view: View) {
        if (viewState == EMPTY)
            familyTipView = view.findViewById(R.id.tip_title)
        super.processStateInflated(viewState, view)
    }

    fun setTipVisible(isVisible: Boolean) {
        familyTipView?.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}