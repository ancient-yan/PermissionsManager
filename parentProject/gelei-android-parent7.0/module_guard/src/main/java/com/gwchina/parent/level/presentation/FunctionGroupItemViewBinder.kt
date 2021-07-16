package com.gwchina.parent.level.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleItemViewBinder
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.guard.R
import kotlinx.android.synthetic.main.level_item_function_group.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-30 16:21
 */
internal class FunctionGroupItemViewBinder : SimpleItemViewBinder<GuardGroupItemVO>() {

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup): Any {
        return inflater.inflate(R.layout.level_item_function_group, parent, false)
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: GuardGroupItemVO) {
        holder.levelIvFunctionGroupName.text = item.group_name
        if (item.isRequireMember) {
            holder.levelIvFunctionGroupName.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.level_icon_vip_grade,0)
        }else{
            holder.levelIvFunctionGroupName.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
        }
    }

    override fun getItemId(item: GuardGroupItemVO): Long {
        return dataManager.getItemPosition(item).toLong()
    }

}