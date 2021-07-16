package com.gwchina.sdk.base.widget.dialog

import android.content.Context
import android.support.v7.app.AppCompatDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.app.base.R
import com.blankj.utilcode.util.ScreenUtils
import kotlinx.android.synthetic.main.dialog_bottom_sheet.*
import kotlinx.android.synthetic.main.dialog_bottom_sheet_item.*


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-03-14 17:01
 */
class BottomSheetDialog(private val builder: BottomSheetDialogBuilder) : AppCompatDialog(builder.context, R.style.Theme_Dialog_Common_Transparent) {

    init {
        setContentView(R.layout.dialog_bottom_sheet)
        setupList()
        setupBottomAction()
        setupTitle()
        setSpaceViewGone()
        fixDialogHeight()
    }

    private fun fixDialogHeight() {
        val screenWidth = ScreenUtils.getScreenWidth()
        val screenHeight = ScreenUtils.getScreenHeight()
        val realScreenWidth = if (screenWidth < screenHeight) screenWidth else screenHeight
        val realScreenHeight = if (screenWidth < screenHeight) screenHeight else screenWidth

        val maxHeight = (realScreenHeight * 0.7F).toInt()

        flLevelDialogFunctionsRoot.measure(
                View.MeasureSpec.makeMeasureSpec(realScreenWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(realScreenHeight, View.MeasureSpec.AT_MOST))

        window?.let {
            // 在底部，宽度撑满
            val params = it.attributes
            params.windowAnimations = R.style.Style_Anim_Bottom_In
            params.gravity = Gravity.BOTTOM or Gravity.CENTER
            params.width = realScreenWidth
            if (flLevelDialogFunctionsRoot.measuredHeight > maxHeight) {
                params.height = maxHeight
            } else {
                params.height = flLevelDialogFunctionsRoot.measuredHeight
            }
            it.attributes = params
        }
    }

    private fun setupTitle() {
        if (builder.titleText.isNotEmpty()) {
            tvDialogBottomSheetTitle.visible()
            tvDialogBottomSheetTitle.text = builder.titleText
        }
    }

    private fun setupBottomAction() {
        if (builder.actionText.isNotEmpty()) {
            tvDialogBottomSheetAction.text = builder.actionText
        } else {
            tvDialogBottomSheetAction.gone()
        }
        tvDialogBottomSheetAction.setOnClickListener {
            dismiss()
            builder.actionListener?.invoke()
        }
    }

    private fun setupList() {
        val customList = builder.customList
        if (customList != null) {
            customList(this, rvDialogBottomSheet)
        } else {
            rvDialogBottomSheet.layoutManager = LinearLayoutManager(context)
            val items = builder.items
            if (items != null) {
                rvDialogBottomSheet.adapter = BottomSheetDialogAdapter(context, items, builder) { position: Int, item: CharSequence ->
                    dismiss()
                    builder.itemSelectedListener?.invoke(position, item)
                }
            }
        }
    }

    /**设置取消上面的间隙隐藏*/
    private fun setSpaceViewGone() {
        if (builder.spaceViewGone) {
            tvDialogBottomSheetActionDivider.gone()
        }
    }

    override fun show() {
        //https://stackoverflow.com/questions/23520892/unable-to-hide-navigation-bar-during-alertdialog-logindialog
        fixSystemNavigator()
        //real show
        super.show()
    }

    private fun fixSystemNavigator() {
        window?.run {
            setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
            val uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
            decorView.systemUiVisibility = uiOptions
            clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        }
    }

}

private class BottomSheetDialogAdapter(
        context: Context, items: List<CharSequence>,
        private val builder: BottomSheetDialogBuilder,
        onItemClickedListener: (Int, CharSequence) -> Unit
) : RecyclerAdapter<CharSequence, KtViewHolder>(context, items) {

    private lateinit var mRecyclerView: RecyclerView

    private val mOnItemClickedListener = View.OnClickListener {
        onItemClickedListener(mRecyclerView.getChildAdapterPosition(it), it.tag as CharSequence)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            KtViewHolder(LayoutInflater.from(mContext).inflate(R.layout.dialog_bottom_sheet_item, parent, false)).apply {
                tvBottomSheetItem.gravity = builder.itemGravity
                tvBottomSheetItem.setTextColor(builder.itemTextColor)
            }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.tvBottomSheetItem.text = item
        viewHolder.itemView.tag = item
        viewHolder.itemView.setOnClickListener(mOnItemClickedListener)
    }

}