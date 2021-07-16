package com.gwchina.parent.member.presentation.purchase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.base.adapter.recycler.ItemViewBinder
import com.gwchina.lssw.parent.user.R
import com.gwchina.parent.member.presentation.purchase.ad.AdViewHolder
import com.gwchina.parent.member.presentation.purchase.info.PurchaseUserInfoViewHolder
import com.gwchina.parent.member.presentation.purchase.plan.PurchasePlanViewHolder
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 10:49
 */
internal sealed class Cards(val id: Long) {
    class MemberInfoCard : Cards(1)
    class AdCard : Cards(2)
    class PlanCard(val memberPlanitem: MemberPlanVO) : Cards(3)
}

internal abstract class BaseItemViewBinder<T : Cards, VH : BaseItemViewHolder<T>>(private val interactor: PurchaseInteractor) : ItemViewBinder<T, VH>() {

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

/**会员信息*/
internal open class UserInfoItemViewBinder(interactor: PurchaseInteractor) : BaseItemViewBinder<Cards.MemberInfoCard, PurchaseUserInfoViewHolder>(interactor) {
    override fun provideLayout() = R.layout.member_purchase_item_info
    override fun onBindViewHolder(holder: PurchaseUserInfoViewHolder, item: Cards.MemberInfoCard) {
        holder.showData()
    }
}

/**购买项目*/
internal open class PlanViewBinder(val interactor: PurchaseInteractor) : BaseItemViewBinder<Cards.PlanCard, PurchasePlanViewHolder>(interactor) {
    override fun provideLayout() = R.layout.member_purchase_plan_item
    override fun onBindViewHolder(holder: PurchasePlanViewHolder, item: Cards.PlanCard) {
        holder.showData(item.memberPlanitem)
    }
}

/**广告*/
internal open class AdItemViewBinder(val interactor: PurchaseInteractor) : BaseItemViewBinder<Cards.AdCard, AdViewHolder>(interactor) {
    override fun provideLayout() = R.layout.member_purchase_item_ad
    override fun onBindViewHolder(holder: AdViewHolder, item: Cards.AdCard) {
        holder.showData()
    }
}

