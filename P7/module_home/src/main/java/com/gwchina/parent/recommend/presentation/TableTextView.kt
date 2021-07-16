package com.gwchina.parent.recommend.presentation

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.util.AttributeSet

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-02-13 16:54
 */
class TableTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        paint.isFakeBoldText = selected
    }

}