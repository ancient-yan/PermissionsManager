package com.gwchina.parent.family.widget

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.android.base.kotlin.gone
import com.android.base.kotlin.ifNull
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.family_dialog_add_update_group.*


class AddAndUpdateDialogBuilder(val context: Context) {

    companion object {
        internal const val add_group = 0
        internal const val update_group = 1
    }

    var groupName: String = ""
    var type: Int = add_group
    var positiveListener: ((dialog: AddAndUpdateGroupDialog, groupName: String) -> Unit)? = null
    var closeListener: ((dialog: AddAndUpdateGroupDialog) -> Unit)? = null

}

fun showAddAndUpdateGroupDialog(context: Context, builder: AddAndUpdateDialogBuilder.() -> Unit): Dialog {
    val addUrlDialog = AddAndUpdateDialogBuilder(context)
    builder.invoke(addUrlDialog)
    val dialog = AddAndUpdateGroupDialog(addUrlDialog)
    dialog.show()
    return dialog
}

class AddAndUpdateGroupDialog(builder: AddAndUpdateDialogBuilder) : BaseDialog(builder.context) {
    var type: Int = AddAndUpdateDialogBuilder.add_group

    init {
        setContentView(R.layout.family_dialog_add_update_group)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CUPCAKE) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        applyBuilder(builder)
    }

    private fun applyBuilder(builder: AddAndUpdateDialogBuilder) {
        setCanceledOnTouchOutside(false)
        this.type = builder.type

        when (builder.type) {
            AddAndUpdateDialogBuilder.add_group -> {
                tvFamilyTitle.text = builder.context.getString(R.string.new_group)
            }

            AddAndUpdateDialogBuilder.update_group -> {
                tvFamilyTitle.text = builder.context.getString(R.string.edit_group)
            }
        }

        tvFamilyPositive.setOnClickListener {

            var groupName = etFamilyGroupName.text.toString().trim()
            if (groupName.isEmpty()) {
                return@setOnClickListener
            }

            builder.positiveListener?.let {
                it(this, groupName)
            }
        }

        ivFamilyClose.setOnClickListener {
            dismiss()
            builder.closeListener?.let {
                it(this)
            }
        }


        ivFmailyGroupClear.setOnClickListener {
            etFamilyGroupName.setText("")
        }

        etFamilyGroupName.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (etFamilyGroupName.text.toString().isNotEmpty()) {
                    // 获得焦点
                    ivFmailyGroupClear.visibility = View.VISIBLE
                } else {
                    ivFmailyGroupClear.visibility = View.GONE
                }
                llFamilyGroupEdit.setBackgroundResource(R.drawable.shape_gray_solid_stroke_round_5)
            } else {
                // 失去焦点
                ivFmailyGroupClear.visibility = View.GONE
                llFamilyGroupEdit.setBackgroundResource(R.drawable.shape_gray_solid_round_5)
            }
        }


        etFamilyGroupName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val content = it.toString()
                    if (content.isEmpty()) {
                        tvFamilyPositive.setTextColor(builder.context.resources.getColor(R.color.gray_level3))
                        ivFmailyGroupClear.visibility = View.GONE
                    } else {
                        tvFamilyPositive.setTextColor(builder.context.resources.getColor(R.color.green_level1))
                        if (etFamilyGroupName.hasFocus()) {
                            ivFmailyGroupClear.visibility = View.VISIBLE
                        }
                    }
                }.ifNull {
                    ivFmailyGroupClear.visibility = View.GONE
                }
                tvFamilyGroupTips.gone()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        etFamilyGroupName.setText(builder.groupName)
        etFamilyGroupName.setSelection(builder.groupName.length)
    }

    fun cannotAddedTips(isShow: Boolean, message: String? = null) {
        if (isShow) {
            tvFamilyGroupTips.visibility = View.VISIBLE
            tvFamilyGroupTips.text = message
        } else {
            tvFamilyGroupTips.visibility = View.GONE
        }
    }

    override fun show() {
        super.show()
        val layoutParams = window!!.attributes
        layoutParams.gravity = Gravity.BOTTOM
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

        window!!.decorView.setPadding(0, 0, 0, 0)

        window!!.attributes = layoutParams
    }
}