package com.gwchina.parent.member.presentation.purchase

import android.support.annotation.CallSuper
import android.view.View
import com.android.base.kotlin.KtViewHolder

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 10:54
 */
abstract class BaseItemViewHolder<T>(purchaseInteractor: PurchaseInteractor, itemView: View) : KtViewHolder(itemView) {
    @CallSuper
    open fun onAttachedToWindow() {
        initOnce()
    }

    open fun onDetachedFromWindow() {}

    protected open fun initOnce() {}
}