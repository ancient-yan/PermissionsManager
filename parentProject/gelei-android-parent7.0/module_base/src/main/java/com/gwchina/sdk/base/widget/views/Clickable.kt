package com.gwchina.sdk.base.widget.views

import android.view.MotionEvent
import android.view.View

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-12 16:07
 */
fun View.setClickFeedback(pressAlpha: Float = 0.5F) {
    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                v.alpha = pressAlpha
            }
            MotionEvent.ACTION_UP -> {
                v.alpha = 1F
            }
            MotionEvent.ACTION_CANCEL -> {
                v.alpha = 1F
            }
        }
        false
    }
}