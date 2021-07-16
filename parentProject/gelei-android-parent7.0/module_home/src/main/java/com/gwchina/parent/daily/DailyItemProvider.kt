package com.gwchina.parent.daily

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ActionProvider
import android.support.v4.view.MenuItemCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.gwchina.lssw.parent.home.R

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-09 14:17
 */
class DailyItemProvider(context: Context) : ActionProvider(context) {

    companion object {
        fun findSelf(menu: Menu): DailyItemProvider {
            val item = menu.findItem(R.id.daily_menu_right)
            return MenuItemCompat.getActionProvider(item) as DailyItemProvider
        }
    }

    private lateinit var menuItemView: View

    lateinit var dailyMessage: ImageView
    private lateinit var publishDailyTv: TextView


    @SuppressLint("InflateParams", "PrivateResource")
    override fun onCreateActionView(): View {
        menuItemView = LayoutInflater.from(context).inflate(R.layout.daily_menu_right_layout, null, false)
        val size = context.resources.getDimensionPixelSize(android.support.design.R.dimen.abc_action_bar_default_height_material)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, size)
        menuItemView.layoutParams = layoutParams
        dailyMessage = menuItemView.findViewById(R.id.dailyMessage)
        publishDailyTv = menuItemView.findViewById(R.id.publishDailyTv)

        return menuItemView
    }

    fun setOnMenuClickListener(showImageView: Boolean = true, showTextView: Boolean = false, onClickListener: () -> Unit) {
        require(!(showImageView && showTextView)) { "two view can not displayed at the same time!" }
        if (showImageView) {
            dailyMessage.visibility = View.VISIBLE
        }
        if (showTextView) publishDailyTv.visibility = View.VISIBLE
        menuItemView.setOnClickListener {
            onClickListener()
        }
    }

    fun getMenuItemView(): View {
        return menuItemView
    }
}