package com.gwchina.parent.member.presentation.purchase.privilege

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import com.android.base.adapter.recycler.RecyclerAdapter
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.ifNonNull
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.data.MemberItem
import com.gwchina.parent.member.presentation.purchase.PurchaseInteractor
import kotlinx.android.synthetic.main.member_purchase_privilege_item.*

/**
 * 会员特权
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-24 13:49
 */
internal class PrivilegeAdapter(context: Context, private val interactor: PurchaseInteractor) : RecyclerAdapter<MemberItem, KtViewHolder>(context) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KtViewHolder {
        return KtViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.member_purchase_privilege_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: KtViewHolder, position: Int) {
        val memberItem = getItem(position)
        val layoutParams = viewHolder.itemView.layoutParams as RecyclerView.LayoutParams
        if (itemCount >= 4) {
            layoutParams.width = getScreenWidth() / 4
        } else {
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        memberItem.ifNonNull {
            viewHolder.tvPurchasePrivilegeName.text = this.member_item_name
            ImageLoaderFactory.getImageLoader().display(interactor.host, viewHolder.ivPurchasePrivilegeIcon, this.member_item_icon)
        }
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        interactor.host.activity?.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        return displayMetrics.widthPixels
    }

}
