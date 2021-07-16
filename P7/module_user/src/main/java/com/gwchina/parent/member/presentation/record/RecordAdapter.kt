package com.gwchina.parent.member.presentation.record

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.kotlin.KtViewHolder
import com.android.base.utils.android.ResourceUtils
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.data.PurchaseRecord
import com.gwchina.sdk.base.utils.formatMilliseconds
import kotlinx.android.synthetic.main.member_record_item.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-26 17:48
 */
class RecordAdapter(context: Context) : RecyclerAdapter<PurchaseRecord, KtViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        return KtViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.member_record_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val purchaseRecord = getItem(position)
        viewHolder.tvMemberRecordPayTypeValue.text = purchaseRecord.pay_type_name
        viewHolder.tvMemberRecordPlanName.text = purchaseRecord.plan_name
        viewHolder.tvMemberRecordCostValue.text = ResourceUtils.getString(R.string.currency_mask, purchaseRecord.order_amount.toString())
        viewHolder.tvMemberRecordCreateTimeValue.text = formatMilliseconds(purchaseRecord.create_time, "yyyy-MM-dd HH:mm")
        viewHolder.tvMemberRecordOrderNoValue.text = purchaseRecord.order_no
    }

}