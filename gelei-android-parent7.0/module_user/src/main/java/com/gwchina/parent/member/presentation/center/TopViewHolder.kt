package com.gwchina.parent.member.presentation.center

import android.view.View
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.ifNonNull
import com.gwchina.lssw.parent.user.R
import com.gwchina.sdk.base.data.models.isMember
import com.gwchina.sdk.base.utils.displayHeadPhotoIcon
import com.gwchina.sdk.base.utils.foldText
import com.gwchina.sdk.base.utils.formatMilliseconds
import com.gwchina.sdk.base.utils.mapParentAvatarBig
import kotlinx.android.synthetic.main.member_center_item_top.*

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 11:01
 */
internal class TopViewHolder(private val interactor: MemberCenterInteractor, itemView: View) : BaseItemViewHolder<Cards.Top>(interactor, itemView) {
    fun showData() {
        interactor.member.ifNonNull {
            tvMemberNickName.text = this.user.patriarch.nick_name.foldText(10)
            if (this.user.patriarch.head_photo_path.isNullOrEmpty()) {
                civMemberPic.setImageResource(mapParentAvatarBig(user.currentChild?.p_relationship_code))
            } else {
                ImageLoaderFactory.getImageLoader().displayHeadPhotoIcon(interactor.host, civMemberPic, this.user.patriarch.head_photo_path)
            }
            if (this.user.isMember) {
                tvMemberEndTime.visibility = View.VISIBLE
                tvMemberEndTime.text = context.getString(R.string.user_member_top_end_time, formatMilliseconds(endTime ?: 0))
            } else {
                tvMemberEndTime.visibility = View.GONE
            }
        }
    }
}
