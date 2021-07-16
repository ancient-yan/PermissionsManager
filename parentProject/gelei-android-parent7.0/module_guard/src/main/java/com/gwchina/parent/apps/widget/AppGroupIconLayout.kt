package com.gwchina.parent.apps.widget

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.kotlin.setPaddingAll
import com.android.base.utils.android.UnitConverter
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.utils.displayAppIcon
import kotlinx.android.synthetic.main.apps_widget_app_group_icon.view.*


class AppGroupIconLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val iconViews: Array<ImageView>

    private val imageLoader = ImageLoaderFactory.getImageLoader()

    init {
        View.inflate(context, R.layout.apps_widget_app_group_icon, this)

        setBackgroundResource(R.drawable.guard_shape_group_icon)

        setPaddingAll(UnitConverter.dpToPx(7))

        iconViews = arrayOf(
                ivAppsWidgetGroupIcon1,
                ivAppsWidgetGroupIcon2,
                ivAppsWidgetGroupIcon3,
                ivAppsWidgetGroupIcon4
        )
    }

    fun display(fragment: Fragment, urlList: List<String?>) {
        val size = urlList.size
        iconViews.forEachIndexed { index, imageView ->
            if (index >= size) {
                imageLoader.display(fragment, imageView, "")
            } else {
                imageLoader.displayAppIcon(fragment, imageView, urlList[index])
            }
        }
    }

}
