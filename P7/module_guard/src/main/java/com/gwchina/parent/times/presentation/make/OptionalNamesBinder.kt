package com.gwchina.parent.times.presentation.make

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.kotlin.*
import com.android.base.widget.recyclerview.MarginDecoration
import com.google.android.flexbox.FlexboxLayoutManager
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.utils.disableEmojiEntering
import com.gwchina.sdk.base.widget.text.ClearableEditTextLayout
import kotlinx.android.synthetic.main.times_names_layout.*
import me.drakeet.multitype.ItemViewBinder

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-12-03 11:43
 */
class OptionalNamesBinder(val context: Context) : ItemViewBinder<MakingPlanInfo, KtViewHolder>() {

    var checkPlansStatus: (() -> Unit)? = null
    var cetTimesPlanName: ClearableEditTextLayout? = null
    var tvTimesPlanNameDuplicateTips: TextView? = null
    var rvTimesPlanNameSuggestions: RecyclerView? = null

    private val itemDecoration = MarginDecoration(0, 0, dip(10), dip(10))

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): KtViewHolder {
        return KtViewHolder(inflater.inflate(R.layout.times_names_layout, parent, false))
    }

    override fun onBindViewHolder(holder: KtViewHolder, item: MakingPlanInfo) {
        setupOptionalNames(holder, item)
    }


    /*备用计划名称*/
    private fun setupOptionalNames(holder: KtViewHolder, makingPlanInfo: MakingPlanInfo) {
        cetTimesPlanName = holder.cetTimesPlanName
        tvTimesPlanNameDuplicateTips = holder.tvTimesPlanNameDuplicateTips
        rvTimesPlanNameSuggestions = holder.rvTimesPlanNameSuggestions
        val optionalNameAdapter = OptionalNameAdapter(context)
        holder.rvTimesPlanNameSuggestions.layoutManager = FlexboxLayoutManager(context)
        holder.rvTimesPlanNameSuggestions.removeItemDecoration(itemDecoration)
        holder.rvTimesPlanNameSuggestions.addItemDecoration(itemDecoration)
        holder.rvTimesPlanNameSuggestions.adapter = optionalNameAdapter
        val usedNames = makingPlanInfo.usedName
        val names = context.resources.getStringArray(R.array.spare_plan_name_optional).run {
            if (!usedNames.isNullOrEmpty()) {
                filter { !usedNames.contains(it) }
            } else {
                this.toList()
            }
        }.map {
            Name(it, false)
        }
        holder.cetTimesPlanName.editText.disableEmojiEntering()
        optionalNameAdapter.onNameSelected = {
            holder.cetTimesPlanName.editText.setText(it.value)
        }
        holder.cetTimesPlanName.editText.textWatcher {
            afterTextChanged {
                if (it.toString().isEmpty()) {
                    optionalNameAdapter.enableAllItems()
                    holder.rvTimesPlanNameSuggestions.visible()
                    holder.tvTimesPlanNameDuplicateTips.gone()
                }
                checkPlansStatus?.invoke()
            }
        }

        if (names.isEmpty()) {
            holder.rvTimesPlanNameSuggestions.gone()
        } else {
            optionalNameAdapter.replaceAll(names)
        }
    }

    fun isPlanNameNotEmpty(): Boolean {
        return cetTimesPlanName?.editText?.text.toString().isNotEmpty()
    }
}