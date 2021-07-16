package com.gwchina.parent.member.presentation.center

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.android.base.kotlin.dip
import com.blankj.utilcode.util.ScreenUtils
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.data.MemberItem
import com.gwchina.parent.member.presentation.purchase.MemberPlanVO
import kotlinx.android.synthetic.main.member_center_bottom_tip.*
import kotlinx.android.synthetic.main.member_center_item_group_header.view.*
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 10:49
 */
internal sealed class Cards(val id: Long) {
    class Top : Cards(1)
    class PrivilegeGroupHeader(val title: String) : Cards(2)
    class Privileges(val memberItem: MemberItem) : Cards(3)
    class BottomTip : Cards(4)
    class GoodsItemList(val memPlan: MemPlan) : Cards(5)
}

data class MemPlan(val memberPlan: List<MemberPlanVO>)

internal abstract class BaseItemViewBinder<T : Cards, VH : BaseItemViewHolder<T>>(private val interactor: MemberCenterInteractor) : ItemViewBinder<T, VH>() {

    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): VH {
        val layout = provideLayout()
        val itemView = if (layout is Int) {
            inflater.inflate(layout, parent, false)
        } else
            layout as View
        return provideViewHolder(itemView)
    }

    @Suppress("UNCHECKED_CAST")
    fun provideViewHolder(itemView: View): VH {
        return (this::class.supertypes[0].arguments[1].type?.classifier as? KClass<VH>)?.primaryConstructor?.call(interactor, itemView)
                ?: throw IllegalArgumentException("need primaryConstructor, and arguments is (CardInteractor, View)")
    }

    abstract fun provideLayout(): Any

    override fun onViewAttachedToWindow(holder: VH) {
        holder.onAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: VH) {
        holder.onDetachedFromWindow()
    }

    override fun getItemId(item: T) = item.id

    override fun onBindViewHolder(holder: VH, item: T) = Unit
}

/**顶部*/
internal open class TopItemViewBinder(interactor: MemberCenterInteractor) : BaseItemViewBinder<Cards.Top, TopViewHolder>(interactor) {
    override fun provideLayout() = R.layout.member_center_item_top
    override fun onBindViewHolder(holder: TopViewHolder, item: Cards.Top) {
        holder.showData()
    }
}

/**特权标题*/
internal open class PrivilegeGroupHeaderItemViewBinder(interactor: MemberCenterInteractor) : BaseItemViewBinder<Cards.PrivilegeGroupHeader, MemberGroupHeaderViewHolder>(interactor) {
    override fun provideLayout() = R.layout.member_center_item_group_header
    override fun onBindViewHolder(holder: MemberGroupHeaderViewHolder, item: Cards.PrivilegeGroupHeader) {
        holder.itemView.tvMemberTitle.text = item.title
    }
}

/**特权项*/
internal open class PrivilegeItemViewBinder(interactor: MemberCenterInteractor) : BaseItemViewBinder<Cards.Privileges, MemberPrivilegeItemViewHolder>(interactor) {
    override fun provideLayout() = R.layout.member_center_item_privilege
    override fun onBindViewHolder(holder: MemberPrivilegeItemViewHolder, item: Cards.Privileges) {
        holder.showData(item.memberItem)
    }
}

/**底部的提示*/
internal open class BottomTip(interactor: MemberCenterInteractor) : BaseItemViewBinder<Cards.BottomTip, BottomTipViewHolder>(interactor) {
    override fun provideLayout() = R.layout.member_center_bottom_tip
    override fun onBindViewHolder(holder: BottomTipViewHolder, item: Cards.BottomTip) {
        //图片尺寸 w:1035 h:1845  40：左右边距
        val radio = (ScreenUtils.getAppScreenWidth() - dip(40)) / 1035.0f
        val realHeight = 1845 * radio
        holder.ivDifference.layoutParams.height = realHeight.toInt()
    }
}

/**购买项目*/
internal open class GoodsItemViewBinder(interactor: MemberCenterInteractor) : BaseItemViewBinder<Cards.GoodsItemList, GoodsViewHolder>(interactor) {

    override fun provideLayout() = R.layout.member_center_goods_layout
    override fun onBindViewHolder(holder: GoodsViewHolder, item: Cards.GoodsItemList) {
        holder.showData(item.memPlan.memberPlan)
    }

}