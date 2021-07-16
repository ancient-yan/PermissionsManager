package com.gwchina.parent.main.presentation.home.card

import android.view.View
import android.view.ViewGroup
import com.android.base.kotlin.dip
import com.android.base.kotlin.realContext
import com.android.base.utils.android.UnitConverter
import com.android.base.utils.android.WindowUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.widget.ChildrenListLayout
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.data.models.currentChildId
import com.zyyoona7.popup.EasyPopup
import com.zyyoona7.popup.XGravity
import com.zyyoona7.popup.YGravity

class SwitchChildListeners(
        val onSelectNewChild: (Child) -> Unit
)

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-16 10:37
 */
fun showSwitchChildPopup(anchor: View, user: User, listeners: SwitchChildListeners) {

    val children: List<Child> = user.childList ?: return
    val selectChildId: String = user.currentChildId ?: return
    val location = IntArray(2).apply { anchor.getLocationInWindow(this) }

    val popupWidth = WindowUtils.getScreenWidth() - 2 * location[0]

    val realContext = anchor.realContext()

    EasyPopup.create()
            .setContentView(realContext, R.layout.home_switch_child_popup)
            .setFocusAndOutsideEnable(true)
            .setDimValue(0.5F)
            .setBackgroundDimEnable(true)
            .apply {
                if (realContext != null) {
                    setDimView(realContext.findViewById(android.R.id.content))
                }
            }
            .setWidth(popupWidth)
            .setOnViewListener { view, pop ->
                setupPopupWindowLayout(view, children, selectChildId, listeners, pop)
                val triangle = view.findViewById<View>(R.id.triangleViewHomeSwitchChildIndicator)
                triangle.layoutParams = (triangle.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    leftMargin = (anchor.measuredWidth / 2F).toInt() - dip(3/*half of triangle width*/)
                }
            }
            .apply()
            .showAtAnchorView(anchor, YGravity.BELOW, XGravity.ALIGN_LEFT, 0, UnitConverter.dpToPx(5))
}

private fun setupPopupWindowLayout(view: View, children: List<Child>, selectChildId: String, listeners: SwitchChildListeners, pop: EasyPopup) {
    val listView: ChildrenListLayout = view.findViewById(R.id.cllHomeChildList)

    listView.showChildrenAndSelectingOne(children, selectChildId) {
        if (it.child_user_id != selectChildId) {
            listeners.onSelectNewChild(it)
        }
        pop.dismiss()
    }

}