package com.gwchina.parent.family.widget

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.dip
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.data.model.GroupPhone
import kotlinx.android.synthetic.main.family_group_list_window.view.*
import kotlinx.android.synthetic.main.family_popup_item_group_name.*


class GroupListPopupWindow(private val context: Context, private val selectGroupId: String = ALL) : PopupWindow() {

    private val datas: MutableList<GroupPhone> = mutableListOf()

    private val adapter: GroupListAdapter by lazy { GroupListAdapter(context, onClickListener, selectGroupId) }

    companion object {
        val ALL = "-1"
    }

    init {
        contentView = LayoutInflater.from(context).inflate(R.layout.family_group_list_window, null)
        contentView.rvFamilyGroupList.layoutManager = LinearLayoutManager(context)
        contentView.rvFamilyGroupList.adapter = adapter

        //设置SelectPicPopupWindow弹出窗体的宽
        this.width = ViewGroup.LayoutParams.WRAP_CONTENT
        //设置SelectPicPopupWindow弹出窗体的高
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        //设置SelectPicPopupWindow弹出窗体可点击
        this.isFocusable = true
        //实例化一个ColorDrawable颜色为半透明
        val dw = ColorDrawable(0X00FFFFFF)
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw)

    }

    fun setdatas(datas: List<GroupPhone>) {
        this.datas.clear()

        val groupPhone = GroupPhone(0, "", ALL, context.getString(R.string.all), "", "", "", listOf(), System.currentTimeMillis(), "")

        this.datas.add(0, groupPhone)
        this.datas.addAll(datas)

        adapter.replaceAll(this.datas)
    }


    override fun showAsDropDown(anchor: View) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
            val rect = Rect()
            anchor.getGlobalVisibleRect(rect)
            val h = anchor.resources.displayMetrics.heightPixels - rect.bottom
            height = h
        }
        super.showAsDropDown(anchor)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        super.showAsDropDown(anchor, xoff, dip(5))
    }

    var onClickListener: ((v: View, selectGroupId: String) -> Unit)? = null
        set(value) {
            adapter.onClickListener = value
            field = value
        }

}


class GroupListAdapter(val context: Context, var onClickListener: ((v: View, selectGroupId: String) -> Unit)?, val selectGroupId: String = GroupListPopupWindow.ALL) : RecyclerAdapter<GroupPhone, KtViewHolder>(context) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder =
            KtViewHolder(LayoutInflater.from(mContext).inflate(R.layout.family_popup_item_group_name, parent, false))

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.tvFamilyGroupName.text = item.group_name
        viewHolder.tvFamilyGroupName.isSelected = item.group_id == selectGroupId

        viewHolder.itemView.setOnClickListener {
            onClickListener?.invoke(it, item.group_id)
        }
    }
}
