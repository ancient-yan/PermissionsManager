package com.gwchina.parent.member.presentation.center

import android.support.annotation.CallSuper
import android.view.View
import com.android.base.kotlin.KtViewHolder

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 10:54
 */
internal abstract class BaseItemViewHolder<T>(memberCenterInteractor: MemberCenterInteractor, itemView: View) : KtViewHolder(itemView) {

    @CallSuper
    open fun onAttachedToWindow() {
    }

    open fun onDetachedFromWindow() {}

}