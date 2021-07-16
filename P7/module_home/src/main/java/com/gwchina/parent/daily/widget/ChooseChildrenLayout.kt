package com.gwchina.parent.daily.widget

import android.annotation.SuppressLint
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.android.base.imageloader.DisplayConfig
import com.android.base.imageloader.ImageLoaderFactory
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.data.api.SEX_FEMALE
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.utils.foldText
import de.hdodenhof.circleimageview.CircleImageView

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-09 18:17
 */
class ChooseChildrenLayout : LinearLayout {
    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        orientation = HORIZONTAL
    }

    companion object {
        val chooseSa = SparseBooleanArray(3)
    }

    @SuppressLint("InflateParams")
    fun setChildrenData(childrenData: List<Child>) {
        check(childrenData.size >= 2) { "the number of children must be greater than 1" }
        childrenData.forEachIndexed { index, _ -> chooseSa.put(index, false) }
        removeAllViews()
        getSelectedChildrenIds()
        childrenData.forEachIndexed { index, _ ->
            val itemView = LayoutInflater.from(context).inflate(R.layout.daily_choose_children_item_layout, null)
            val childImgIv = itemView.findViewById<CircleImageView>(R.id.childImgIv)
            val childSelectedIv = itemView.findViewById<ImageView>(R.id.childSelectedIv)
            val childNameTv = itemView.findViewById<TextView>(R.id.childNameTv)
            val childLayoutRv = itemView.findViewById<ConstraintLayout>(R.id.childLayoutRv)
            childNameTv.text=childrenData[index].nick_name.foldText(5)
            val displayConfig = DisplayConfig.create().setErrorPlaceholder(if (childrenData[index].sex == SEX_FEMALE) R.drawable.img_head_girl_38 else R.drawable.img_head_boy_38)
            ImageLoaderFactory.getImageLoader().display(childImgIv, childrenData[index].head_photo_path, displayConfig)
            childLayoutRv.tag = index
            val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            if (chooseSa.get(index, false)) {
                childSelectedIv.visibility = View.VISIBLE
            } else {
                childSelectedIv.visibility = View.INVISIBLE
            }
            params.weight = 1.0f
            this.addView(itemView, params)
            childLayoutRv.setOnClickListener {
                val currentStatus = chooseSa.get(childLayoutRv.tag as Int, false)
                chooseSa.put(childLayoutRv.tag as Int, !currentStatus)
                setSelectedStyle(childImgIv, childSelectedIv, !currentStatus)
            }
        }
    }

    private fun setSelectedStyle(childImgIv: CircleImageView, childSelectedIv: ImageView, isSelected: Boolean) {
        if (isSelected) {
            childSelectedIv.visibility = View.VISIBLE
            childImgIv.borderWidth = 6
            childImgIv.borderColor = ContextCompat.getColor(context, R.color.green_level1)
        } else {
            childSelectedIv.visibility = View.INVISIBLE
            childImgIv.borderWidth = 0
        }
        getSelectedChildrenIds()

    }

    private val selectedChildrenList = mutableListOf<Int>()

    private fun getSelectedChildrenIds() {
        selectedChildrenList.clear()
        for (i in 0 until chooseSa.size()) {
            if (chooseSa.get(i, false)) {
                selectedChildrenList.add(i)
            }
        }
        onChildSelectListener?.onSelectedList(selectedChildrenList)
    }

    var onChildSelectListener: OnChildSelectListener? = null

    interface OnChildSelectListener {
        fun onSelectedList(selectedChildrenList: List<Int>)
    }
}