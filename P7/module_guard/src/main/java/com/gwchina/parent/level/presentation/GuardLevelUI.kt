package com.gwchina.parent.level.presentation

import android.graphics.Rect
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.kotlin.dip
import com.android.base.widget.recyclerview.MarginDecoration
import kotlinx.android.synthetic.main.level_fragment_setup.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-30 15:33
 */
class GuardLevelUI(private val host: SetGuardLevelFragment) {

    private val context = host.requireContext()
    private val contentAdapter = MultiTypeAdapter(context)

    init {
        initContentListView()
    }

    private fun initContentListView() {
        //列表
        host.levelRvFunctions.layoutManager = LinearLayoutManager(context)
        host.levelRvFunctions.addItemDecoration(MarginDecoration(0, 0, 0, dip(10)))
        with(contentAdapter) {
            setHasStableIds(true)
            register(GuardItemVO::class.java, FunctionItemItemViewBinder(host))
            register(GuardGroupItemVO::class.java, FunctionGroupItemViewBinder())
        }
        host.levelRvFunctions.addItemDecoration(ContentItemDecoration())
        host.levelRvFunctions.isNestedScrollingEnabled = false
        host.levelRvFunctions.adapter = contentAdapter
        host.levelRvFunctions.isFocusable = false
    }

    fun setList(guardGroupItemVOList: List<GuardGroupItemVO>) {
        val data = ArrayList<Any>()
        guardGroupItemVOList.forEach {
            if (it.item_list.isNotEmpty()) {
                data.add(it)
                data.addAll(it.item_list)
            }
        }
        if (data.isNotEmpty()) {
            contentAdapter.replaceAll(data)
        }
    }

    private class ContentItemDecoration : RecyclerView.ItemDecoration() {

        private val mItemVerticalMargin = dip(10)
        private val mNormalItemVerticalMargin = dip(20)

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

            val position = parent.getChildAdapterPosition(view)
            val adapter = parent.adapter as MultiTypeAdapter

            val itemVO = adapter.getItem(position)
            if (itemVO is GuardGroupItemVO && position != 0) {
                outRect.top = mItemVerticalMargin
                outRect.bottom = mNormalItemVerticalMargin
                outRect.left = 0
                outRect.right = 0
            } else {
                outRect.top = 0
                outRect.bottom = mNormalItemVerticalMargin
                outRect.left = 0
                outRect.right = 0
            }
        }
    }

}