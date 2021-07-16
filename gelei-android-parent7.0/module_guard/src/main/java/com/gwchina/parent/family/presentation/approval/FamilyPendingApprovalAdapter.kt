package com.gwchina.parent.family.presentation.approval

import android.support.v4.app.Fragment
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.imageloader.DisplayConfig
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.family.data.model.Approval
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.SEX_FEMALE
import kotlinx.android.synthetic.main.family_item_pending_approval.*
import kotlinx.android.synthetic.main.family_item_pending_approval.view.*


class FamilyPendingApprovalAdapter(private val host: Fragment) : RecyclerAdapter<Approval, KtViewHolder>(host.requireContext()) {

    val childList = AppContext.appDataSource().user().childList

    companion object {
        const val REFUSE = "Refuse"
        const val AGREE = "Agree"
    }

    var onAgreeOrRefuseClickListener: ((v: View, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        var contentView = LayoutInflater.from(mContext).inflate(R.layout.family_item_pending_approval, parent, false)
        contentView.btnFamilyAgree.tag = AGREE
        contentView.btnFamilyRefuse.tag = REFUSE

        contentView.btnFamilyAgree.setOnClickListener {
            onAgreeOrRefuseClickListener?.invoke(it, contentView.tag as Int)
        }

        contentView.btnFamilyRefuse.setOnClickListener {
            onAgreeOrRefuseClickListener?.invoke(it, contentView.tag as Int)
        }
        return KtViewHolder(contentView)
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val item = getItem(position)
        val child = childList?.filter { it.child_user_id == item.user_id }
        if (child != null && child.isNotEmpty()) {
            val sex = child[0].sex
            val path = child[0].head_photo_path
            val displayConfig = DisplayConfig.create().setErrorPlaceholder(if (sex == SEX_FEMALE) R.drawable.img_head_girl_38 else R.drawable.img_head_boy_38)
            ImageLoaderFactory.getImageLoader().display(viewHolder.ivFamilyIcon, path, displayConfig)
        }
        val content = host.getString(R.string.reason_content, item.user_name, item.phone_remark, item.phone)
        val stringBuilder = SpannableStringBuilder(content)
        val colorSpan = ForegroundColorSpan(host.resources.getColor(R.color.green_level1))
        stringBuilder.setSpan(colorSpan, content.indexOf(item.phone_remark), content.indexOf(item.phone_remark) + item.phone_remark.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        viewHolder.tvFamilyApprovalTitle.text = stringBuilder
        viewHolder.tvFamilyReasonContent.text = item.approval_reason
        viewHolder.itemView.tag = position
        if (position == dataSize - 1) viewHolder.line.gone() else viewHolder.line.visible()
    }
}