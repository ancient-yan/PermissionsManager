package com.gwchina.parent.main.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.HorizontalScrollView

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-16 16:29
 */
class MyHorizontalScrollView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : HorizontalScrollView(context, attrs, defStyleAttr) {

    var onScrollListener:((Int,Int,Int,Int)->Unit)?=null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        onScrollListener?.invoke(l,t,oldl,oldt)
    }
}