package com.gwchina.parent.apps.presentation.rules

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ActionProvider
import android.support.v4.view.MenuItemCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R


class PendingApprovalItemProvider(context: Context) : ActionProvider(context) {

    companion object {

        fun findSelf(menu: Menu): PendingApprovalItemProvider {
            val item = menu.findItem(R.id.menuAppsPendingApproval)
            return MenuItemCompat.getActionProvider(item) as PendingApprovalItemProvider
        }

    }

    private lateinit var menuItemView: View
    private lateinit var redDotView: View

    @SuppressLint("PrivateResource", "InflateParams")
    override fun onCreateActionView(): View {
        val menuItem = LayoutInflater.from(context).inflate(R.layout.widget_text_with_red_dot, null, false)

        val size = context.resources.getDimensionPixelSize(android.support.design.R.dimen.abc_action_bar_default_height_material)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, size)
        menuItem.layoutParams = layoutParams

        menuItem.findViewById<TextView>(R.id.tvBaseWidgetText).setText(R.string.pending_approval)
        redDotView = menuItem.findViewById(R.id.viewBaseWidgetRedDot)

        menuItemView = menuItem

        return menuItem
    }

    var showDot: Boolean
        get() = redDotView.visibility == View.VISIBLE
        set(value) {
            if (value) {
                redDotView.visible()
            } else {
                redDotView.invisible()
            }
        }

    fun setOnMenuClickListener(onClickListener: () -> Unit) {
        menuItemView.setOnClickListener {
            onClickListener()
        }
    }

    var visible: Boolean
        get() = menuItemView.visibility == View.VISIBLE
        set(value) {
            if (value) {
                menuItemView.visible()
            } else {
                menuItemView.invisible()
            }
        }

}