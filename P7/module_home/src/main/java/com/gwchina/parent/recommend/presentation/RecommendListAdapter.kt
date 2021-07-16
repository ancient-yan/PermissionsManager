package com.gwchina.parent.recommend.presentation

import android.support.v4.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.android.base.adapter.recycler.SimpleRecyclerAdapter
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.colorFromId
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.recommend.data.SoftItem
import com.gwchina.sdk.base.utils.displayAppIcon
import com.gwchina.sdk.base.utils.formatDecimal
import kotlinx.android.synthetic.main.reco_item_soft.*

class RecommendListAdapter(private val fragment: Fragment) : SimpleRecyclerAdapter<SoftItem>(fragment.requireContext()) {

    private val _onInstallListener = View.OnClickListener {
        onInstallListener?.invoke(it.tag as SoftItem)
    }

    private val _onItemClickListener = View.OnClickListener {
        onItemClickListener?.invoke(it.tag as SoftItem)
    }

    var onInstallListener: ((SoftItem) -> Unit)? = null
    var onItemClickListener: ((SoftItem) -> Unit)? = null

    override fun provideLayout(parent: ViewGroup, viewType: Int) = R.layout.reco_item_soft

    override fun bind(viewHolder: KtViewHolder, item: SoftItem) {

        ImageLoaderFactory.getImageLoader().displayAppIcon(fragment, viewHolder.ivItemSoftIcon, item.soft_icon)
        viewHolder.tvItemName.text = item.soft_name
        viewHolder.tvItemPhrase.text = item.rec_phrase
        viewHolder.rbRecoItemRating.star = item.rec_level
        viewHolder.tvRecoItemScoring.text = fragment.getString(R.string.scoring_mask, formatDecimal(item.rec_level.toFloat(), 1))

        viewHolder.itemView.tag = item
        viewHolder.itemView.setOnClickListener(_onItemClickListener)

        when {
            item.isInstalling() -> {
                viewHolder.tvItemInstallStatus.showInstalling()
                viewHolder.tvItemInstallStatus.isClickable = false
            }
            item.isInstalled() -> {
                viewHolder.tvItemInstallStatus.showInstalled()
                viewHolder.tvItemInstallStatus.isClickable = false
            }
            else -> {
                viewHolder.tvItemInstallStatus.showNotInstall()
                viewHolder.tvItemInstallStatus.tag = item
                viewHolder.tvItemInstallStatus.setOnClickListener(_onInstallListener)
            }
        }
    }

    private fun TextView.showNotInstall() {
        isEnabled = true
        setText(R.string.install_for_child)
        setTextColor(colorFromId(R.color.green_level1))
    }

    private fun TextView.showInstalled() {
        isEnabled = false
        setTextColor(colorFromId(R.color.gray_level3))
        setText(R.string.installed)
    }

    private fun TextView.showInstalling() {
        isEnabled = false
        setTextColor(colorFromId(R.color.gray_level2))
        setText(R.string.installing)
    }

}