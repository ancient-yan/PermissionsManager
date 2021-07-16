package com.gwchina.parent.binding.presentation

import android.view.View

internal fun composeToSingleSelectable(vararg views: View, onSelected: (View) -> Unit) {

    var selected = views.find {
        it.isSelected
    }

    val onClickListener = View.OnClickListener {
        selected?.isSelected = false
        it.isSelected = true
        selected = it
        onSelected(it)
    }

    views.forEach {
        it.setOnClickListener(onClickListener)
    }

}