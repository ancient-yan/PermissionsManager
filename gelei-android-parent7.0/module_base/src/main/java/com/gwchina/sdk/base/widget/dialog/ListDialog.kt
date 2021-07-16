package com.gwchina.sdk.base.widget.dialog

import android.content.Context
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.adapter.recycler.ViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.android.base.utils.android.UnitConverter
import com.app.base.R
import kotlinx.android.synthetic.main.dialog_list_layout.*

/**
 * 列表对话框
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-12-12 19:51
 */
internal class ListDialog(
        listDialogBuilder: ListDialogBuilder
) : BaseDialog(listDialogBuilder.context, true, listDialogBuilder.style) {

    private var selectedItemIndex: Int = 0
    private var selectableBgId = 0

    private val dialogController = object : DialogController {
        override var positiveEnable: Boolean
            get() = dblListDialogBottom?.positiveEnable ?: false
            set(value) {
                dblListDialogBottom?.positiveEnable = value
            }
    }

    init {
        maxDialogWidthPercent = listDialogBuilder.maxWidthPercent

        setContentView(R.layout.dialog_list_layout)
        getSelectedBg()
        applyListDialogBuilder(listDialogBuilder)

        listDialogBuilder.onDialogPreparedListener?.invoke(this, dialogController)
    }

    private fun applyListDialogBuilder(listDialogBuilder: ListDialogBuilder) {
        //title
        val title = listDialogBuilder.title
        if (title != null) {
            tvListDialogTitle.visible()
            tvListDialogTitle.text = title
            tvListDialogTitle.textSize = listDialogBuilder.titleSize
            tvListDialogTitle.setTextColor(listDialogBuilder.titleColor)
        } else {
            tvListDialogTitle.gone()
        }

        //cancel
        dblListDialogBottom.negativeText(listDialogBuilder.negativeText)
        dblListDialogBottom.onNegativeClick(View.OnClickListener {
            checkDismiss(listDialogBuilder)
            listDialogBuilder.negativeListener?.invoke()
        })

        //confirm
        dblListDialogBottom.positiveText(listDialogBuilder.positiveText)

        //list
        rvDialogListContent.layoutManager = LinearLayoutManager(context)
        val items = listDialogBuilder.items
        val adapter = listDialogBuilder.adapter

        if (items != null) {
            setupDefault(items, listDialogBuilder)
        } else if (adapter != null) {
            setupUsingSpecifiedAdapter(adapter, listDialogBuilder)
        }
    }

    private fun setupUsingSpecifiedAdapter(adapter: RecyclerView.Adapter<*>?, listDialogBuilder: ListDialogBuilder) {
        rvDialogListContent.adapter = adapter
        dblListDialogBottom.onPositiveClick(View.OnClickListener {
            checkDismiss(listDialogBuilder)
            listDialogBuilder.positiveListener?.invoke(-1, "")
        })
    }

    private fun setupDefault(items: Array<CharSequence>, listDialogBuilder: ListDialogBuilder) {
        val size = items.size
        selectedItemIndex = listDialogBuilder.selectedPosition

        if (selectedItemIndex < 0) {
            selectedItemIndex = 0
        } else if (selectedItemIndex >= size) {
            selectedItemIndex = size - 1
        }

        rvDialogListContent.adapter = Adapter(context, items.toList())
        dblListDialogBottom.onPositiveClick(View.OnClickListener {
            checkDismiss(listDialogBuilder)
            listDialogBuilder.positiveListener?.invoke(selectedItemIndex, items[selectedItemIndex])
        })

        rvDialogListContent.post {
            rvDialogListContent?.smoothScrollToPosition(selectedItemIndex)
        }
    }

    private fun checkDismiss(listDialogBuilder: ListDialogBuilder) {
        if (listDialogBuilder.autoDismiss) {
            dismiss()
        }
    }

    private fun getSelectedBg() {
        try {
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            selectableBgId = outValue.resourceId
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class Adapter internal constructor(
            context: Context,
            data: List<CharSequence>
    ) : RecyclerAdapter<CharSequence, ViewHolder>(context, data) {

        private val mOnClickListener = View.OnClickListener { view ->
            val childAdapterPosition = rvDialogListContent!!.getChildAdapterPosition(view)
            val oldSelectedItemIndex = selectedItemIndex
            selectedItemIndex = childAdapterPosition
            notifyItemChanged(oldSelectedItemIndex)
            notifyItemChanged(selectedItemIndex)
        }

        private val hPadding = UnitConverter.dpToPx(19)
        private val vPadding = UnitConverter.dpToPx(12)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val textView = AppCompatTextView(context)
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sel_tick_choice, 0, 0, 0)
            textView.compoundDrawablePadding = UnitConverter.dpToPx(10)
            textView.setTextColor(mContext.colorFromId(R.color.gray_level2))
            textView.gravity = Gravity.CENTER_VERTICAL
            textView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            if (selectableBgId != 0) {
                textView.setBackgroundResource(selectableBgId)
            }
            textView.setPadding(hPadding, vPadding, hPadding, vPadding)
            return ViewHolder(textView)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            (viewHolder.itemView as TextView).text = getItem(position)
            viewHolder.itemView.isSelected = viewHolder.adapterPosition == selectedItemIndex
            if (!viewHolder.itemView.isSelected) {
                viewHolder.itemView.setOnClickListener(mOnClickListener)
            } else {
                viewHolder.itemView.setOnClickListener(null)
            }
        }

    }

}
