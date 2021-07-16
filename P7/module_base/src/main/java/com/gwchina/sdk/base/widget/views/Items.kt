package com.gwchina.sdk.base.widget.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.support.annotation.CallSuper
import android.support.annotation.ColorInt
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.base.adapter.DataManager
import com.android.base.adapter.recycler.MultiTypeAdapter
import com.android.base.adapter.recycler.SimpleItemViewBinder
import com.android.base.imageloader.ImageLoaderFactory
import com.android.base.imageloader.Source
import com.android.base.kotlin.KtViewHolder
import com.android.base.kotlin.clearComponentDrawable
import com.android.base.kotlin.leftDrawable
import com.android.base.kotlin.visibleOrGone
import com.android.base.utils.android.UnitConverter
import com.app.base.R
import com.gwchina.sdk.base.AppContext
import kotlinx.android.synthetic.main.widget_item_avatar.*
import kotlinx.android.synthetic.main.widget_item_normal.*
import me.drakeet.multitype.register


/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-04-12 16:07
 */
class ItemManager(context: Context, private val baseConfig: ItemConfiguration) {

    private val textItemItemBinder = TextItemViewBinder()
    private val avatarItemItemBinder = AvatarItemViewBinder()
    private val itemAdapter = MultiTypeAdapter(context)
    private var recyclerView: RecyclerView? = null
    private var onItemClickedListener: ((View, BaseItem) -> Unit)? = null

    fun setOnItemClickedListener(onItemClickedListener: ((View, BaseItem) -> Unit)) {
        this.onItemClickedListener = onItemClickedListener
        textItemItemBinder.onItemClicked = onItemClickedListener
        avatarItemItemBinder.onItemClicked = onItemClickedListener
    }

    init {
        itemAdapter.register(textItemItemBinder)
        itemAdapter.register(avatarItemItemBinder)
    }

    fun <T : BaseItem> registerItem(clazz: Class<T>, baseItemViewBinder: BaseItemViewBinder<T>) {
        itemAdapter.register(clazz, baseItemViewBinder)
        baseItemViewBinder.onItemClicked = onItemClickedListener
    }

    fun setup(recyclerView: RecyclerView, list: List<BaseItem>) {
        if (this.recyclerView != recyclerView) {
            this.recyclerView = recyclerView
            recyclerView.addItemDecoration(EntranceItemDecoration(baseConfig))
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
            itemAdapter.setDataSource(list, false)
            recyclerView.adapter = itemAdapter
        }
    }

    fun replaceItems(list: List<BaseItem>) {
        itemAdapter.setDataSource(list, true)
    }

    fun <T : BaseItem> updateItem(id: Int, logic: (T) -> Unit) {
        itemAdapter.baseItem().find {
            id == it.id
        }?.let {
            @Suppress("UNCHECKED_CAST")
            logic(it as T)
            itemAdapter.notifyItemChanged(itemAdapter.getItemPosition(it))
        }
    }

    fun updateTextItem(id: Int, logic: (TextItem) -> Unit) {
        itemAdapter.baseItem().find {
            id == it.id
        }?.let {
            logic(it as TextItem)
            itemAdapter.notifyItemChanged(itemAdapter.getItemPosition(it))
        }
    }

    fun updateAvatarItem(id: Int, logic: (AvatarItem) -> Unit) {
        itemAdapter.baseItem().find {
            id == it.id
        }?.let {
            logic(it as AvatarItem)
            itemAdapter.notifyItemChanged(itemAdapter.getItemPosition(it))
        }
    }

    fun updateItems(logic: (BaseItem) -> Unit) {
        itemAdapter.baseItem().let {
            it.forEach(logic)
            itemAdapter.notifyDataSetChanged()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun MultiTypeAdapter.baseItem(): List<BaseItem> {
        return (items as? List<BaseItem>) ?: emptyList()
    }

}

private const val TYPE_NORMAL = 1
private const val TYPE_AVATAR = 2

interface BaseItem {
    val id: Int
    val type: Int
    var hasTopCuttingLine: Boolean
    var hasBottomCuttingLine: Boolean
    var topMargin: Int
}

data class TextItem(
        override val id: Int,
        override var hasTopCuttingLine: Boolean = false,
        override var hasBottomCuttingLine: Boolean = false,
        override var topMargin: Int = 0,
        var hasNotification: Boolean = false,
        val title: String? = null,
        var subtitle: String? = null,
        var hasArrow: Boolean = true,
        var subtitleColor: Int = 0,
        var subtitleSize: Int = 0,
        var backgroundColorIdRes: Int = 0,
        val leftImgId: Int = 0
) : BaseItem {
    override val type: Int = TYPE_NORMAL
}

data class AvatarItem(
        override val id: Int,
        override var hasTopCuttingLine: Boolean = false,
        override var hasBottomCuttingLine: Boolean = false,
        override var topMargin: Int = 0,
        val title: String,
        var hasArrow: Boolean = false,
        val avatarSize: Int = UnitConverter.dpToPx(40),
        var showAvatar: ((ImageView) -> Unit)? = null,
        var source: Source? = null
) : BaseItem {
    override val type: Int = TYPE_AVATAR
}

data class ItemConfiguration(
        @ColorInt var cuttingLineColor: Int = Color.BLACK
)

abstract class BaseItemViewBinder<T : BaseItem> : SimpleItemViewBinder<T>() {

    var onItemClicked: ((View, BaseItem) -> Unit)? = null

    protected open var clickable = true

    private val onItemListener = View.OnClickListener {
        onItemClicked?.invoke(it, it.tag as BaseItem)
    }

    @CallSuper
    override fun onBindViewHolder(viewHolder: KtViewHolder, item: T) {
        if (clickable) {
            viewHolder.itemView.tag = item
            viewHolder.itemView.setOnClickListener(onItemListener)
        }
    }

}

class TextItemViewBinder : BaseItemViewBinder<TextItem>() {

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.widget_item_normal

    override fun onBindViewHolder(viewHolder: KtViewHolder, item: TextItem) {
        super.onBindViewHolder(viewHolder, item)
        viewHolder.viewWidgetItemRedDot.visibleOrGone(item.hasNotification)
        viewHolder.ivWidgetItemArrow.visibleOrGone(item.hasArrow)
        viewHolder.tvWidgetItemTitle.text = item.title
        viewHolder.tvWidgetItemSubtitle.text = item.subtitle

        if (item.subtitleColor > 0) {
            viewHolder.tvWidgetItemSubtitle.setTextColor(AppContext.getContext().resources.getColor(item.subtitleColor))
        }
        if (item.subtitleSize > 0) {
            viewHolder.tvWidgetItemSubtitle.textSize = item.subtitleSize.toFloat()
        }
        if (item.backgroundColorIdRes > 0) {
            viewHolder.itemView.setBackgroundResource(item.backgroundColorIdRes)
        }
        if (item.leftImgId > 0) {
            viewHolder.tvWidgetItemTitle.leftDrawable(item.leftImgId)
        } else {
            viewHolder.tvWidgetItemTitle.clearComponentDrawable()
        }
    }
}

class AvatarItemViewBinder : BaseItemViewBinder<AvatarItem>() {

    override fun provideLayout(inflater: LayoutInflater, parent: ViewGroup) = R.layout.widget_item_avatar

    override fun onBindViewHolder(viewHolder: KtViewHolder, item: AvatarItem) {
        super.onBindViewHolder(viewHolder, item)

        viewHolder.ivWidgetAvatarItemArrow.visibleOrGone(item.hasArrow)
        viewHolder.tvWidgetAvatarItemTitle.text = item.title

        val showAvatar = item.showAvatar
        val source = item.source
        if (showAvatar != null) {
            showAvatar.invoke(viewHolder.ivWidgetItemAvatar)
        } else if (source != null) {
            ImageLoaderFactory.getImageLoader().display(viewHolder.ivWidgetItemAvatar, source)
        }
    }

}

private class EntranceItemDecoration(cuttingLineColor: ItemConfiguration) : RecyclerView.ItemDecoration() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.strokeWidth = 1F
        paint.color = cuttingLineColor.cuttingLineColor
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        val childAdapterPosition = parent.getChildAdapterPosition(view)
        @Suppress("UNCHECKED_CAST")
        val adapter = parent.adapter as DataManager<BaseItem>
        val item: BaseItem? = adapter.getItem(childAdapterPosition)
        if (item != null) {
            outRect.top = item.topMargin
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        @Suppress("UNCHECKED_CAST")
        val adapter = parent.adapter as DataManager<BaseItem>
        val childCount = parent.childCount
        var childView: View
        var textItem: BaseItem?
        for (index in 0 until childCount) {
            childView = parent.getChildAt(index)
            textItem = adapter.getItem(parent.getChildAdapterPosition(childView))

            if (textItem == null) {
                continue
            }

            if (textItem.hasTopCuttingLine) {
                c.drawLine(childView.left.toFloat(), childView.top.toFloat(), childView.right.toFloat(), childView.top.toFloat(), paint)
            }
            if (textItem.hasBottomCuttingLine) {
                c.drawLine(childView.left.toFloat(), childView.bottom.toFloat(), childView.right.toFloat(), childView.bottom.toFloat(), paint)
            }
        }
    }

}