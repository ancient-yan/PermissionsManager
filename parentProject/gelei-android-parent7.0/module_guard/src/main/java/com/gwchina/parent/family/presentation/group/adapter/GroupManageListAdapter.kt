package com.gwchina.parent.family.presentation.group.adapter

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.gone
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.sdk.base.widget.dialog.TipsManager
import kotlinx.android.synthetic.main.family_item_group.*

class GroupManageListAdapter(private val host: Fragment, val isDelete: Boolean = false) : RecyclerAdapter<GroupPhone, KtViewHolder>(host.requireContext()) {

    var onItemClickListener: ((v: View) -> Unit)? = null

    val selectIdList = mutableListOf<String>()
    val selectItemList = mutableListOf<GroupPhone>()
    private val _selectListID = MutableLiveData<List<String>>()
    internal val selectListID: LiveData<List<String>>
        get() = _selectListID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.family_item_group, parent, false)

        view.setOnClickListener {
            val position = it.tag as Int
            val item = getItem(position)
            if (isDelete && item.is_default == "1") {
                TipsManager.showMessage(R.string.delete_family_group)
                return@setOnClickListener
            }
            onItemClickListener?.let { listener ->
                listener.invoke(view)
                return@setOnClickListener
            }
            if (item.isSelected) {
                item.isSelected = false
                selectIdList.remove(item.group_id)
                selectItemList.remove(item)
            } else {
                item.isSelected = true
                selectIdList.add(item.group_id)
                selectItemList.add(item)
            }
            _selectListID.postValue(selectIdList)
            notifyItemChanged(position)
        }
        return KtViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        var item = getItem(position)
        if (!isDelete) {
            viewHolder.cbFamilyDelete.gone()
        } else {
            if (item.is_default=="1") {
                viewHolder.cbFamilyDelete.invisible()
            } else {
                viewHolder.cbFamilyDelete.visible()
            }
        }
        viewHolder.cbFamilyDelete.isChecked = item.isSelected
        viewHolder.ivFamilyArrow.visibility = if (isDelete) View.GONE else View.VISIBLE
        viewHolder.itemView.tag = position
        val phoneList = item.phone_list
        var num = 0
        phoneList?.let {
            num = it.size
        }
        viewHolder.tvFamilyGroupName.text = host.getString(R.string.group_name_num, item.group_name, num)

    }

    fun removeSelectItem() {
        removeItems(selectItemList)
    }

    fun clearSelectItem() {
        selectItemList.forEach {
            it.isSelected = false
        }
    }
}