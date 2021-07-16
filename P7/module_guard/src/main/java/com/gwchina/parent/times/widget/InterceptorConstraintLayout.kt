package com.gwchina.parent.times.widget

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.MotionEvent

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-10-16 10:59
 *      拦截子view的vg
 */
class InterceptorConstraintLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

}