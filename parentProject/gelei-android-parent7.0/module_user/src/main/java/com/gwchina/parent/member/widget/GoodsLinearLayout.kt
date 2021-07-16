package com.gwchina.parent.member.widget

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import com.android.base.kotlin.dip
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.parent.member.presentation.purchase.MemberPlanVO

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2020-03-09 10:32
 */
class GoodsLinearLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        val ITEM_VIEW_WIDTH = dip(100)
        val ITEM_VIEW_HEIGHT = dip(120)
        //露出来的宽度
        val EDGE_WIDTH = dip(23)
        //
        val MARGIN_END = dip(12)
    }

    var onItemClickListener: ((Int, MemberPlanVO) -> Unit)? = null

    init {
        orientation = HORIZONTAL
    }

    fun buildData(memberPlanList: List<MemberPlanVO>?, defaultPosition: Int?) {
        removeAllViews()
        if (memberPlanList.isNullOrEmpty()) return
        if (memberPlanList.size == 1) {
            val goodsItemLayout = GoodsItemLayout(context)
            val layoutParams = LayoutParams(ITEM_VIEW_WIDTH, LayoutParams.WRAP_CONTENT)
            layoutParams.marginEnd = MARGIN_END
            goodsItemLayout.buildData(memberPlanVO = memberPlanList[0])
            val p = this.layoutParams
            p.width = ScreenUtils.getScreenWidth()
            p.height = ViewGroup.LayoutParams.WRAP_CONTENT
            this.layoutParams = p
            this.gravity = Gravity.CENTER_HORIZONTAL
            this.addView(goodsItemLayout, layoutParams)
            goodsItemLayout.onGoodsItemClick = { position, memberPlan ->
                onItemClickListener?.invoke(position, memberPlan)
            }
        } else if (memberPlanList.size > 1) {
            val itemWidth = (ScreenUtils.getScreenWidth() - paddingStart - MARGIN_END * 3 - EDGE_WIDTH) / 3.0
            memberPlanList.forEach {
                val goodsItemLayout = GoodsItemLayout(context)
                val layoutParams = LayoutParams(itemWidth.toInt(), LayoutParams.WRAP_CONTENT)
                layoutParams.marginEnd = MARGIN_END
                this.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                this.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                this.gravity = Gravity.START
                this.addView(goodsItemLayout, layoutParams)
                goodsItemLayout.buildData(it)
                goodsItemLayout.onGoodsItemClick = { position, memberPlan ->
                    onItemClickListener?.invoke(position, memberPlan)
                }
            }
        }
        if (defaultPosition != null) {
            defaultSelect(defaultPosition)
        }
    }

    /**默认选中哪一个*/
    private fun defaultSelect(position: Int) {
        val childCount = childCount
        if (position < 0 || position >= childCount) return
        for (i in 0 until childCount) {
            if (getChildAt(i) is GoodsItemLayout) {
                val itemLayout = getChildAt(i) as GoodsItemLayout
                if (i == position) {
                    itemLayout.setSelectedState()
                    itemLayout.memberPlanVO?.let { onItemClickListener?.invoke(position, it) }
                } else {
                    itemLayout.resetSelectedState()
                }
            }
        }
    }
}