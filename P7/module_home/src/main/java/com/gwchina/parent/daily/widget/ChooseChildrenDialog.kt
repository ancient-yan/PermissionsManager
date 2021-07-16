package com.gwchina.parent.daily.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.daily_choose_dialog_layout.*

/**
 *@author hujl
 *@Email: hujlin@163.com
 *@Date : 2019-08-08 14:37
 * 选择孩子对话框
 */
class ChooseChildrenDialog(context: Context, private var childrenMessage: List<Child>, var confirmAction: (List<Int>) -> Unit, var cancelAction: () -> Unit) : BaseDialog(context) {

    init {
        setContentView(R.layout.daily_choose_dialog_layout)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setChildrenMessage()
    }

    lateinit var mSelectedChildrenList: List<Int>

    override var maxDialogWidthPercent: Float = 0.9F

    private fun setChildrenMessage() {
        chooseChildrenLayout.setChildrenData(childrenMessage)
        chooseChildrenLayout.onChildSelectListener = object : ChooseChildrenLayout.OnChildSelectListener {

            override fun onSelectedList(selectedChildrenList: List<Int>) {
                mSelectedChildrenList = selectedChildrenList
                if (selectedChildrenList.isNotEmpty()) {
                    confirm.isEnabled = true
                    confirm.setTextColor(ContextCompat.getColor(context, R.color.green_level1))
                } else {
                    confirm.isEnabled = false
                    confirm.setTextColor(ContextCompat.getColor(context, R.color.gray_level3))
                }
            }
        }
        confirm.setOnClickListener {
            dismiss()
            confirmAction(mSelectedChildrenList)
        }
        cancel.setOnClickListener {
            dismiss()
            cancelAction()
        }
    }

}