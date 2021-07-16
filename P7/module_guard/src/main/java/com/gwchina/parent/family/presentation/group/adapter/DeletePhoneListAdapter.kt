package com.gwchina.parent.family.presentation.group.adapter

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.data.model.Phone
import kotlinx.android.synthetic.main.family_item_phone_del.*

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-10-17 16:20
 *      删除一个组内的多个号码
 */
class DeletePhoneListAdapter(host: Fragment) : RecyclerAdapter<Phone, KtViewHolder>(host.requireContext()) {

    private val selectedList = mutableListOf<String>()

    var onItemClickListener: ((selectedList: MutableList<String>) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.family_item_phone_del, parent, false)
        view.setOnClickListener {
            val position = it.tag as Int
            val item = getItem(position)
            if (selectedList.contains(item.rule_id)) {
                selectedList.remove(item.rule_id)
            } else {
                selectedList.add(item.rule_id)
            }
            notifyItemChanged(position)
            onItemClickListener?.invoke(selectedList)
        }

        return KtViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.tvFamilyName.text = item.phone_remark
        viewHolder.tvFamilyPhone.text = item.phone
        viewHolder.itemView.tag = position
        viewHolder.cbFamilyDelete.isChecked = selectedList.contains(item.rule_id)
    }
}