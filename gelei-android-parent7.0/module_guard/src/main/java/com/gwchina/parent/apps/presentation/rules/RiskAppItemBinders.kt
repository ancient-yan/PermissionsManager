package com.gwchina.parent.apps.presentation.rules

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.gone
import com.android.base.kotlin.visible
import com.gwchina.lssw.parent.guard.R
import kotlinx.android.synthetic.main.apps_item_app_approval.*
import kotlinx.android.synthetic.main.apps_item_limited_add_app.*

internal class RiskAppItemViewBinder(host: Fragment) : AppApprovalItemViewBinder(host) {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup) =
            super.onCreateViewHolder(inflater, parent).apply {
                tvAppsItemProhibit.gone()
//                tvAppsItemAllow.gone()
            }

}

internal class EmptyRiskAppItemViewBinder : ItemViewBinder<String, KtViewHolder>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup) =
            KtViewHolder(inflater.inflate(R.layout.apps_item_limited_add_app, parent, false)).apply {

                groupAppsItemAddingViews.gone()
                ivAppsAddAppDivider.gone()
                tvAppsItemEmptyAdd.gone()

                tvAppsItemEmptyIcon.setImageResource(R.drawable.app_img_no_risk)
                tvAppsItemEmptyTips.setText(R.string.no_risk_apps_tips)
                groupAppsItemEmptyViews.visible()

            }

    override fun onBindViewHolder(holder: KtViewHolder, item: String) = Unit

}