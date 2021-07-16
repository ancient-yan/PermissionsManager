package com.gwchina.parent.family.presentation.home

import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.data.model.Phone
import kotlinx.android.synthetic.main.family_item_phone.*

/**
 * 亲情号码首页和分组设置的适配器
 */
class FamilyPhoneListAdapter(
        host: Fragment
) : RecyclerAdapter<Phone, KtViewHolder>(host.requireContext()) {

    companion object {
        //private const val TYPE_HEAD = 1
        private const val TYPE_NORMAL = 1
        private const val TYPE_EMPTY = 2
    }

    private var headerView: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {

        return when (viewType) {
//            TYPE_HEAD -> KtViewHolder(headerView!!)
            TYPE_NORMAL -> {
                var contentView = LayoutInflater.from(mContext).inflate(R.layout.family_item_phone, parent, false)
                contentView.setOnClickListener {
                    onItemClickListener?.invoke(it)
                }
                KtViewHolder(contentView)
            }
            else -> {
                val emptyView = LayoutInflater.from(mContext).inflate(R.layout.app_base_layout_empty, parent, false)
                emptyView.findViewById<AppCompatButton>(R.id.base_retry_btn).visibility = View.GONE
                emptyView.findViewById<TextView>(R.id.base_retry_tv).text = mContext.getString(R.string.group_list_empty)
                val mEmptyLayout = FrameLayout(mContext)
                val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                val lp = emptyView.layoutParams
                lp?.let {
                    layoutParams.width = it.width
                }
                mEmptyLayout.layoutParams = layoutParams
                mEmptyLayout.removeAllViews()
                mEmptyLayout.addView(emptyView)
                KtViewHolder(mEmptyLayout)
            }
        }

    }


    fun addHeaderView(view: View) {
        headerView = view
        setHeaderSize { 1 }
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        if (viewHolder.itemViewType == TYPE_NORMAL) {

            val item = getItem(position)
            viewHolder.tvFamilyIcon.text = item.phone_remark?.firstOrNull().toString()
            viewHolder.tvFamilyName.text = item.phone_remark
            viewHolder.tvFamilyPhone.text = item.phone
            viewHolder.itemView.tag = position

        }
    }

    override fun getItemCount(): Int {

        if (1 == getEmptyViewCount()) {
            //header + empty
            return 1
        } else {

            if (headerView != null) {
                return 1 + super.getItemCount()
            }
            return super.getItemCount()
        }
    }

    override fun getItemViewType(position: Int): Int =
            if (getEmptyViewCount() == 1) {
                TYPE_EMPTY
            } else {
                TYPE_NORMAL
            }


    var onItemClickListener: ((v: View) -> Unit)? = null

    private fun getEmptyViewCount(): Int {
        if (items.size != 0) {
            return 0
        }
        return 1
    }


}