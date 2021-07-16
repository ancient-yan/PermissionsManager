package com.gwchina.parent.migration.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.utils.calculateAgeByBirthday
import com.gwchina.sdk.base.utils.getGradeDescFromGrade
import com.gwchina.sdk.base.utils.mapChildAvatarBig
import com.gwchina.sdk.base.utils.splitBirthday
import kotlinx.android.synthetic.main.migration_item_child.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 19:00
 */

internal class ChildAdapter(context: Context) : SimpleRecyclerAdapter<UploadingChild>(context) {

    var onSelectedChildListener: ((childUser: UploadingChild) -> Unit)? = null

    private val onItemClickListener = View.OnClickListener {
        onSelectedChildListener?.invoke((it.tag as UploadingChild))
    }

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.migration_item_child

    @SuppressLint("SetTextI18n")
    override fun bind(viewHolder: KtViewHolder, item: UploadingChild) {
        viewHolder.itemView.tag = item
        viewHolder.itemView.setOnClickListener((onItemClickListener))
        viewHolder.ivMigrationItemChildAvatar.setImageResource(mapChildAvatarBig(item.sex))
        viewHolder.tvMigrationItemChildName.text = item.name
        val (year, month, day) = splitBirthday(item.birthday)
        viewHolder.tvMigrationItemInfo.text = mContext.getString(R.string.x_year_old, calculateAgeByBirthday(year, month, day)) + mContext.getString(R.string.comma) + getGradeDescFromGrade(item.grade)
    }

}