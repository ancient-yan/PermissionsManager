package com.gwchina.parent.family.presentation.group.adapter

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.data.model.GroupPhone
import com.gwchina.parent.family.presentation.group.GroupRangeSettingFragment
import com.kyleduo.switchbutton.SwitchButton
import kotlinx.android.synthetic.main.family_item_group_range_setting.*
import kotlinx.android.synthetic.main.family_item_group_range_setting.ivFamilyGroupIcon
import kotlinx.android.synthetic.main.family_item_group_range_setting.sbFamilyGroupRange
import kotlinx.android.synthetic.main.family_item_group_range_setting.view.*
import kotlinx.android.synthetic.main.family_item_group_range_setting_head.*

class GroupRangeSettingAdapter(host: Fragment,
                               private val pageType: String = GroupRangeSettingFragment.CALL,
                               private var isUnlimited: Boolean) : RecyclerAdapter<GroupPhone, KtViewHolder>(host.requireContext()) {

    companion object {
        const val TYPE_HEAD = -1
        const val TYPE_ALL = -2
        private const val TYPE_NORMAL = 1
    }

//    var isSVipMember = false

    var onItemCheckedChangeListener: ((position: Int, isChecked: Boolean, switchButton: SwitchButton) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        val contentView = if (viewType == TYPE_HEAD) {
            LayoutInflater.from(mContext).inflate(R.layout.family_item_group_range_setting_head, parent, false)
        } else {
            LayoutInflater.from(mContext).inflate(R.layout.family_item_group_range_setting, parent, false)
        }
        contentView.setOnClickListener {
            val btn = contentView.sbFamilyGroupRange
            onItemCheckedChangeListener?.invoke(btn.tag as Int, !btn.isChecked, btn)
        }

        contentView.sbFamilyGroupRange.setOnCheckedChangeListener { buttonView, isChecked ->
            onItemCheckedChangeListener?.invoke(buttonView.tag as Int, isChecked, contentView.sbFamilyGroupRange)
        }
        return KtViewHolder(contentView)
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        if (getItemViewType(position) == TYPE_HEAD) {
            viewHolder.sbFamilyGroupRange.tag = TYPE_HEAD
            viewHolder.sbFamilyGroupRange.setCheckedImmediatelyNoEvent(isUnlimited)
            viewHolder.viewFamilySeg.visibility = if (itemCount == 1) View.INVISIBLE else View.VISIBLE
//            viewHolder.tvFamilyCallLimitName.text = if (pageType == GroupRangeSettingFragment.CALL) mContext.getString(R.string.all_phone_call_out) else mContext.getString(R.string.all_phone_call_in)
            viewHolder.tvFamilyCallLimitName.text = mContext.getString(R.string.all_phone_no_limit)
        } else if (getItemViewType(position) == TYPE_ALL) {
                viewHolder.sbFamilyGroupRange.tag = TYPE_ALL
                val filter = items.filter {
                    if (pageType == GroupRangeSettingFragment.CALL) {
                        it.is_call_out == "0"
                    } else {
                        it.is_call_in == "0"
                    }
                }

                viewHolder.sbFamilyGroupRange.setCheckedImmediatelyNoEvent(filter.isEmpty())
                viewHolder.tvFamilyAllGroup.visibility = View.VISIBLE
                viewHolder.ivFamilyGroupIcon.visibility = View.GONE
                viewHolder.tvFamilyGroupName.visibility = View.GONE
            } else {
                val dataPosition = position - getAllItemCount() - getHeaderItemCount()
                val item = getItem(dataPosition)
                viewHolder.sbFamilyGroupRange.tag = dataPosition
                viewHolder.tvFamilyAllGroup.visibility = View.GONE
                viewHolder.ivFamilyGroupIcon.visibility = View.VISIBLE
                viewHolder.tvFamilyGroupName.visibility = View.VISIBLE
                viewHolder.tvFamilyGroupName.text = item.group_name
                if (pageType == GroupRangeSettingFragment.CALL) {
                    viewHolder.sbFamilyGroupRange.setCheckedImmediatelyNoEvent(item.is_call_out == "1")
                } else {
                    viewHolder.sbFamilyGroupRange.setCheckedImmediatelyNoEvent(item.is_call_in == "1")
                }
            }
//        if (!isSVipMember) {
//            viewHolder.sbFamilyGroupRange.setCheckedImmediatelyNoEvent(getItemViewType(position) != TYPE_HEAD)
//        }
//        viewHolder.sbFamilyGroupRange.isEnabled = isSVipMember
    }

    override fun getItemCount(): Int =
            if (super.getItemCount() == 0) {
                getHeaderItemCount()
            } else {
                getHeaderItemCount() + getAllItemCount() + super.getItemCount()
            }

    fun notifyNormalItemChanged(position: Int) {
        notifyItemChanged(position + getHeaderItemCount() + getAllItemCount())
    }

    fun notifyAllItemChanged() {
        notifyItemChanged(1)
    }


    private fun getAllItemCount(): Int = 1
    private fun getHeaderItemCount(): Int = 1

    override fun getItemViewType(position: Int): Int =
            when (position) {
                0 -> TYPE_HEAD
                1 -> TYPE_ALL
                else -> TYPE_NORMAL
            }


    /**
     * isUnlimited: 不限制 开放
     */
    fun changeLimitState(isUnlimited: Boolean) {
        this@GroupRangeSettingAdapter.isUnlimited = isUnlimited
        notifyItemChanged(0)
    }


}