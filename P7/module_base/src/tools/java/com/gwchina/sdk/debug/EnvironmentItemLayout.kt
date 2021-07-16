package com.gwchina.sdk.debug

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.android.base.kotlin.dip
import com.android.base.kotlin.setPaddingAll
import com.app.base.R
import com.gwchina.sdk.base.data.Environment
import com.gwchina.sdk.base.data.EnvironmentContext
import kotlinx.android.synthetic.main.base_debug_environment_item.view.*

class EnvironmentItemLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        orientation = VERTICAL
        setPaddingAll(dip(10))
        View.inflate(context, R.layout.base_debug_environment_item, this)
    }

    private lateinit var list: List<Environment>
    private lateinit var categoryName: String

    fun bindEnvironmentList(categoryName: String, list: List<Environment>) {
        this.categoryName = categoryName
        this.list = list
        baseTvDebugHostName.text = categoryName

        showSelectedValue(EnvironmentContext.selected(categoryName))

        baseBtnDebugSwitch.setOnClickListener {
            showSwitchDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showSelectedValue(selected: Environment) {
        baseTvDebugHostValue.text = "${selected.name}：${selected.url}"
    }

    private fun showSwitchDialog() {
        val first = list.indexOf(EnvironmentContext.selected(categoryName))

        AlertDialog.Builder(context)
                .setSingleChoiceItems(list.map { it.name }.toTypedArray(), first) { dialog, which ->
                    dialog.dismiss()
                    val env = list[which]
                    showSelectedValue(env)
                    EnvironmentContext.select(categoryName, env)
                }.show()
    }

}