package com.gwchina.parent.recommend.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.view.ActionProvider
import android.support.v4.view.MenuItemCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gwchina.lssw.parent.home.R

class SwitchGradeItemProvider(context: Context) : ActionProvider(context) {

    companion object {

        fun findSelf(menu: Menu): SwitchGradeItemProvider {
            val item = menu.findItem(R.id.menuHomeGuardReport)
            return MenuItemCompat.getActionProvider(item) as SwitchGradeItemProvider
        }

    }

    private lateinit var titleText: TextView
    private lateinit var menuItemView: View

    @SuppressLint("PrivateResource", "InflateParams")
    override fun onCreateActionView(): View {
        menuItemView = LayoutInflater.from(context).inflate(R.layout.reco_switch_grade_menu, null, false)
        val size = context.resources.getDimensionPixelSize(android.support.design.R.dimen.abc_action_bar_default_height_material)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, size)
        menuItemView.layoutParams = layoutParams

        titleText = menuItemView.findViewById(R.id.tvSwitchGradle)

        return menuItemView
    }

    fun setOnMenuClickListener(onClickListener: () -> Unit) {
        menuItemView.setOnClickListener {
            onClickListener()
        }
    }

    fun showTitle(title: String?) {
        titleText.text = title
    }

}