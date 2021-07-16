package com.gwchina.parent.times.presentation.make

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.dip
import com.coorchice.library.SuperTextView
import com.gwchina.lssw.parent.guard.R

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-07-24 10:53
 */
class OptionalNameAdapter(context: Context) : SimpleRecyclerAdapter<Name>(context) {

    var onNameSelected: ((Name) -> Unit)? = null

    private var usingName: Name? = null

    private val _onItemClickListener = View.OnClickListener {
        val name = it.tag as Name
        name.using = true
        val oldUsingName = usingName
        usingName = name
        onNameSelected?.invoke(name)
        notifyEntryChanged(name)
        if (oldUsingName != null) {
            oldUsingName.using = false
            notifyEntryChanged(oldUsingName)
        }
    }

    override fun provideLayout(parent: ViewGroup, viewType: Int) = SuperTextView(mContext).apply {
        textSize = 12F
        setPadding(dip(11), dip(3), dip(11), dip(3))
        corner = dip(2F)
        strokeColor = colorFromId(R.color.gray_level4)
        strokeWidth = 1F
    }

    override fun bind(viewHolder: KtViewHolder, item: Name) {
        with(viewHolder.itemView as TextView) {
            text = item.value
            tag = item
            if (item.using) {
                setTextColor(colorFromId(R.color.gray_level3))
                setOnClickListener(null)
            } else {
                setTextColor(colorFromId(R.color.gray_level2))
                setOnClickListener(_onItemClickListener)
            }
        }
    }

    fun enableAllItems() {
        usingName?.let {
            it.using = false
            notifyEntryChanged(it)
        }
    }

}

data class Name(
        val value: String,
        var using: Boolean
)
