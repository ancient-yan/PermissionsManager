package com.gwchina.parent.member.presentation.center

import android.view.View
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.gone
import com.android.base.kotlin.ifNonNull
import com.android.base.kotlin.visible
import com.android.base.kotlin.visibleOrGone
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.data.MemberItem
import com.gwchina.sdk.base.data.api.isNo
import kotlinx.android.synthetic.main.member_center_item_privilege.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 11:01
 */
internal class MemberPrivilegeItemViewHolder(private val interactor: MemberCenterInteractor, itemView: View) : BaseItemViewHolder<Cards.Privileges>(interactor, itemView) {
    fun showData(memberItem: MemberItem?) {
        memberItem.ifNonNull {
            tvMemberItemName.text = member_item_name
            tvMemberItemDesc.text = member_item_desc
            if (isNo(memberItem?.member_item_flag)) {
                ivDeveloping.visible()
            } else {
                ivDeveloping.gone()
            }
            tvCustomMachine.visibleOrGone((memberItem?.member_item_name == "远程截屏"))
            if (member_item_icon.isNullOrEmpty()) {
                ivMemberItemIcon.setImageResource(R.drawable.icon_more_vip)
            } else {
                ImageLoaderFactory.getImageLoader().display(interactor.host, ivMemberItemIcon, this.member_item_icon)
            }
        }
    }
}
