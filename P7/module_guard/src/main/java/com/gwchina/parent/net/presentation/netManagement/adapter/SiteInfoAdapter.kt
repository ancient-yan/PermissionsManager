package com.gwchina.parent.net.presentation.netManagement.adapter

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.net.data.model.SiteInfo
import com.gwchina.sdk.base.utils.subText
import kotlinx.android.synthetic.main.net_item_site_info.*

class SiteInfoAdapter(private val host: Fragment, val listType: String, val isDelete: Boolean = false) : RecyclerAdapter<SiteInfo, KtViewHolder>(host.requireContext()) {

    companion object {
        const val BLACKLIST = "blacklist"
        const val WHITELIST = "whitelist"
    }

    val selectIdList = mutableListOf<String>()
    private val selectItemList = mutableListOf<SiteInfo>()
    private val _selectListID = MutableLiveData<List<String>>()
    internal val selectListID: LiveData<List<String>>
        get() = _selectListID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.net_item_site_info, parent, false)

        view.setOnClickListener {
            if (::onClickListener.isInitialized) {
                onClickListener.invoke(it)
                return@setOnClickListener
            }

            val position = it.tag as Int
            val item = getItem(position)
            if (item.isSelected) {
                item.isSelected = false
                selectIdList.remove(item.rule_id)
                selectItemList.remove(item)
            } else {
                item.isSelected = true
                selectIdList.add(item.rule_id)
                selectItemList.add(item)
            }
            _selectListID.postValue(selectIdList)
            notifyItemChanged(position)
        }
        return KtViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.net_cb_delete.visibility = if (isDelete) View.VISIBLE else View.GONE
        viewHolder.net_cb_delete.isChecked = item.isSelected
        viewHolder.net_tv_website.text = item.url
        viewHolder.net_tv_label.text = if (item.url_name.isNotBlank()) item.url_name.subText(10) else host.getString(R.string.unknow)
        viewHolder.itemView.tag = position
        if (listType == BLACKLIST) {
            viewHolder.net_tv_label.setBackgroundResource(R.drawable.shape_red_op10_round3)
            viewHolder.net_tv_label.setTextColor(host.resources.getColor(R.color.red_level1))
        } else {
            viewHolder.net_tv_label.setBackgroundResource(R.drawable.shape_green_op10_round3)
            viewHolder.net_tv_label.setTextColor(host.resources.getColor(R.color.green_level1))
        }
    }

    fun removeSelectItem() {
        removeItems(selectItemList)
        clearSelectItem()
    }

    fun clearSelectItem() {
        selectItemList.clear()
        selectIdList.clear()
    }


    lateinit var onClickListener: (v: View) -> Unit
}