package com.gwchina.parent.level.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleItemViewBinder
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.invisible
import com.android.base.kotlin.visible
import com.android.base.utils.android.ResourceUtils
import com.gwchina.lssw.parent.guard.R
import kotlinx.android.synthetic.main.level_item_function.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-30 16:10
 */
internal class FunctionItemItemViewBinder(private val host: SetGuardLevelFragment) : SimpleItemViewBinder<GuardItemVO>() {

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup): Any {
        return inflater.inflate(R.layout.level_item_function, parent, false)
    }

    private val mOnItemClickListener = View.OnClickListener {
        val functionItem = it.tag as? GuardItemVO
        if (functionItem != null) {
            functionItem.isSelected = !functionItem.isSelected
            dataManager.notifyItemChanged(dataManager.getItemPosition(functionItem))
        }
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, item: GuardItemVO) {
        if (item.isSelected) {
            viewHolder.levelIvFunctionStatus.setImageResource(R.drawable.icon_choose_small_white_broder)
        } else {
            viewHolder.levelIvFunctionStatus.setImageResource(R.drawable.icon_choose_default_small_white_broder)
        }

        ImageLoaderFactory.getImageLoader().display(host, viewHolder.levelIvFunctionIcon, item.normalIcon)

        viewHolder.levelIvFunctionDesc.text = item.desc
        viewHolder.itemView.tag = item

        if (item.must) {
            viewHolder.levelIvFunctionStatus.invisible()
            viewHolder.itemView.setOnClickListener(null)
            viewHolder.levelIvFunctionName.text = item.name
        } else {
            viewHolder.itemView.setOnClickListener(mOnItemClickListener)
            viewHolder.levelIvFunctionStatus.visible()
            viewHolder.levelIvFunctionName.text = ResourceUtils.getString(R.string.optional_functions, item.name)
        }
    }

    override fun getItemId(item: GuardItemVO): Long {
        return dataManager.getItemPosition(item).toLong()
    }

}