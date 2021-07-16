package com.gwchina.parent.member.presentation.purchase.info

import android.view.View
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.ifNonNull
import com.gwchina.parent.member.presentation.purchase.PurchaseInteractor
import com.gwchina.parent.member.presentation.purchase.BaseItemViewHolder
import com.gwchina.parent.member.presentation.purchase.Cards
import com.gwchina.sdk.base.utils.displayHeadPhotoIcon
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.mapParentAvatarBig
import kotlinx.android.synthetic.main.member_purchase_item_info.*

/**
 * 会员信息
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 11:01
 */
internal class PurchaseUserInfoViewHolder(private val interactor: PurchaseInteractor, itemView: View) : BaseItemViewHolder<Cards.MemberInfoCard>(interactor, itemView) {
    fun showData() {
        interactor.purchaseDataVO.ifNonNull {
            userInfo.ifNonNull {
                tvPurchaseNickName.text = this.nick_name.foldText(10)
                tvPurchaseDesc.text = this.desc
                if (this.head_photo_path.isNullOrEmpty()) {
                    civPurchasePic.setImageResource(mapParentAvatarBig(user?.currentChild?.p_relationship_code))
                } else {
                    ImageLoaderFactory.getImageLoader().displayHeadPhotoIcon(interactor.host, civPurchasePic, this.head_photo_path)
                }
            }
        }
    }
}
