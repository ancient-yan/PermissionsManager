package com.gwchina.parent.times.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.gwchina.lssw.parent.guard.R
import kotlinx.android.synthetic.main.times_widget_operation_tips.view.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-19 17:08
 */
class OperationTipsView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
        View.inflate(context, R.layout.times_widget_operation_tips, this)

        with(context.obtainStyledAttributes(attrs, R.styleable.OperationTipsView)) {
            tvTimesWidgetTips.text = getText(R.styleable.OperationTipsView_opv_tips)
            recycle()
        }
    }

}