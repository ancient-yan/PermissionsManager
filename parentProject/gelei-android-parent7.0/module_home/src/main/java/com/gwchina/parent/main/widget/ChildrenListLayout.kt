package com.gwchina.parent.main.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.colorFromId
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.gwchina.lssw.parent.home.R
import com.gwchina.sdk.base.data.api.isMemberGuardExpired
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.mapChildAvatarBig
import kotlinx.android.synthetic.main.main_switch_child_item.*


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-16 10:40
 */
class ChildrenListLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val flexboxLayoutManager: FlexboxLayoutManager = FlexboxLayoutManager(context, FlexDirection.ROW, FlexWrap.WRAP)

    init {
        flexboxLayoutManager.justifyContent = JustifyContent.SPACE_AROUND
        layoutManager = flexboxLayoutManager
    }

    fun showChildrenAndSelectingOne(children: List<Child>, selectedId: String, onSelectChildListener: ((Child) -> Unit)) {
        val childAdapter = SingleChildAdapter(context)
        childAdapter.selectChildId = selectedId
        childAdapter.replaceAll(children)
        childAdapter.onSelectChildListener = onSelectChildListener
        adapter = childAdapter
    }

    fun showChildrenAndSelectingMulti(children: List<Child>, selectedChildren: List<Child>? = null): (() -> List<Child>) {
        val childAdapter = MultiChildAdapter(context)
        if (!selectedChildren.isNullOrEmpty()) {
            childAdapter.selectedChildren.addAll(selectedChildren)
        }
        childAdapter.replaceAll(children)
        val callback = fun(): List<Child> {
            return childAdapter.selectedChildren
        }
        adapter = childAdapter
        return callback
    }

}


private class MultiChildAdapter(context: Context) : SimpleRecyclerAdapter<Child>(context) {

    val selectedChildren = mutableListOf<Child>()

    private val mItemOnClickListener = View.OnClickListener {
        if (selectedChildren.contains(it.tag as Child)) {
            selectedChildren.remove(it.tag as Child)
        } else {
            selectedChildren.add(it.tag as Child)
        }
        notifyDataSetChanged()
    }

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.main_switch_child_item

    override fun bind(viewHolder: KtViewHolder, item: Child) {
        viewHolder.tvMainChildName.text = item.nick_name.foldText(5)
        viewHolder.ivMainChildAvatar.setImageResource(mapChildAvatarBig(item.sex))

        viewHolder.itemView.tag = item
        viewHolder.itemView.setOnClickListener(mItemOnClickListener)

        if (selectedChildren.contains(item)) {
            viewHolder.viewMainChildAvatarMark.visible()
            viewHolder.tvMainChildName.setTextColor(mContext.colorFromId(R.color.gray_level1))
        } else {
            viewHolder.viewMainChildAvatarMark.invisible()
            viewHolder.tvMainChildName.setTextColor(mContext.colorFromId(R.color.gray_level2))
        }
    }

}

private class SingleChildAdapter(context: Context) : SimpleRecyclerAdapter<Child>(context) {

    var onSelectChildListener: ((Child) -> Unit)? = null

    private val mItemOnClickListener = View.OnClickListener {
        onSelectChildListener?.invoke(it.tag as Child)
    }

    var selectChildId: String = ""

    private val colorFilter = PorterDuffColorFilter(Color.parseColor("#99000000"), PorterDuff.Mode.SRC_ATOP)

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.main_switch_child_item

    override fun bind(viewHolder: KtViewHolder, item: Child) {
        viewHolder.tvMainChildName.text = item.nick_name.foldText(5)
        viewHolder.ivMainChildAvatar.setImageResource(mapChildAvatarBig(item.sex))

        viewHolder.itemView.tag = item
        viewHolder.itemView.setOnClickListener(mItemOnClickListener)

        if (!isMemberGuardExpired(item.status)) {
            if (item.child_user_id == selectChildId) {
                viewHolder.viewMainChildAvatarMark.visible()
                viewHolder.tvMainChildName.setTextColor(mContext.colorFromId(R.color.gray_level1))
            } else {
                viewHolder.viewMainChildAvatarMark.invisible()
                viewHolder.tvMainChildName.setTextColor(mContext.colorFromId(R.color.gray_level2))
            }
            viewHolder.tvMainChildExpired.invisible()
            viewHolder.ivMainChildAvatar.colorFilter = null
        } else {
            viewHolder.tvMainChildExpired.visible()
            viewHolder.viewMainChildAvatarMark.invisible()
            viewHolder.tvMainChildName.setTextColor(mContext.colorFromId(R.color.gray_level3))
            viewHolder.ivMainChildAvatar.colorFilter = colorFilter
        }
    }

}