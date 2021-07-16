package com.gwchina.parent.daily.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.android.base.kotlin.colorFromId
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.home.R

/**
 * 文本预渲染
 *
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-19 16:52
 */
class DiaryTextBuilder constructor(private val context: Context) {

    private val maxLine = 10
    private val expandText = "   " + context.getString(R.string.expand_all) + " "
    private val collapseText = "   " + context.getString(R.string.fold)
    private val ellipsizeText = "..."

    private val textPaint: TextPaint
    private val viewWidth: Int

    private val drawableWidth: Int
    private val expandContentWidth: Float

    var onExpandContent: ((position: Int) -> Unit)? = null
    var onCollapseContent: ((position: Int) -> Unit)? = null

    private val onExpandClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            val tag = widget.tag
            if (tag != null && tag is Int) {
                onExpandContent?.invoke(tag)
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            ds.color = context.colorFromId(R.color.gray_level2)
        }
    }

    private val onCollapseClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            val tag = widget.tag
            if (tag != null && tag is Int) {
                onCollapseContent?.invoke(tag)
            }
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            ds.color = context.colorFromId(R.color.gray_level2)
        }
    }

    init {
        //measure view
        @SuppressLint("InflateParams")
        val itemView = LayoutInflater.from(context).inflate(R.layout.daily_adapter_item_layout, null, false)
        val measureWidthSpec = View.MeasureSpec.makeMeasureSpec(ScreenUtils.getScreenWidth(), View.MeasureSpec.EXACTLY)
        val measureHeightSpec = View.MeasureSpec.makeMeasureSpec(ScreenUtils.getScreenHeight(), View.MeasureSpec.AT_MOST)
        itemView.measure(measureWidthSpec, measureHeightSpec)

        //get width and paint
        val tvContent = itemView.findViewById<TextView>(R.id.tv_content)

        textPaint = tvContent.paint
        viewWidth = tvContent.measuredWidth - tvContent.paddingLeft - tvContent.paddingRight /*布局中的间距*/

        //drawable
        val drawable = ContextCompat.getDrawable(context, R.drawable.diary_icon_blue_right)!!
        textPaint.isFakeBoldText = true
        drawableWidth = drawable.intrinsicWidth

        //text width
        val expandTextWidth = textPaint.measureText(expandText)
        textPaint.isFakeBoldText = false
        val ellipsizeTextWidth = textPaint.measureText(ellipsizeText)

        //点击展开部分的总长度
        expandContentWidth = drawableWidth.toFloat() * 2 + expandTextWidth + ellipsizeTextWidth /*mDrawableWidth * 2 防止某些机型最后的图标换行*/
    }

    fun createExpandableContent(content: String, colorId: Int): Array<CharSequence> {

        @Suppress("DEPRECATION")
        val staticLayout = StaticLayout(
                content,
                textPaint,
                viewWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                false
        )

        val lineCount = staticLayout.lineCount

        when {
            lineCount > maxLine -> { //大于10行的情况
                return arrayOf(createExpandableContent(content, staticLayout, colorId), createCollapseAbleContent(content, colorId))
            }

            lineCount == maxLine -> {//等于10行的情况

                val lineStart = staticLayout.getLineStart(9/*第九行*/)
                val lineEnd = staticLayout.getLineEnd(9)

                val lastLineWidth = textPaint.measureText(content.substring(lineStart, lineEnd))

                return if (lastLineWidth + drawableWidth < viewWidth) {
                    val drawableContent = createDrawableContent(content, colorId)
                    arrayOf(drawableContent, drawableContent)
                } else {
                    arrayOf(createExpandableContent(content, staticLayout, colorId), createCollapseAbleContent(content, colorId))
                }
            }

            else -> { //小于10行的情况
                val drawableContent = createDrawableContent(content, colorId)
                return arrayOf(drawableContent, drawableContent)
            }
        }
    }

    private fun createCollapseAbleContent(content: String, colorId: Int): CharSequence {
        return SpanUtils()
                .append(content)
                .append(collapseText)
                .setForegroundColor(ContextCompat.getColor(context,R.color.green_level1))
                .setBold()
                .setClickSpan(onCollapseClickableSpan)
                .appendImage(getRightDrawableWithSameColor(colorId), SpanUtils.ALIGN_CENTER)
                .create()
    }

    private fun createDrawableContent(content: String, colorId: Int): CharSequence {
        return SpanUtils()
                .append(content)
                .appendImage(getRightDrawableWithSameColor(colorId), SpanUtils.ALIGN_CENTER)
                .create()
    }

    private fun getRightDrawableWithSameColor(colorId: Int): Drawable {
        return when (colorId) {
            R.color.red_level1 -> ContextCompat.getDrawable(context, R.drawable.diary_icon_red_right)!!
            R.color.blue_level2 -> ContextCompat.getDrawable(context, R.drawable.diary_icon_blue_right)!!
            R.color.green_main -> ContextCompat.getDrawable(context, R.drawable.diary_icon_green_right)!!
            else -> throw IllegalStateException("unsupported color, color id is $colorId")
        }
    }

    private fun createExpandableContent(content: String, staticLayout: StaticLayout, colorId: Int): CharSequence {
        val offsetForHorizontal = staticLayout.getOffsetForHorizontal(9/*第九行*/, viewWidth - expandContentWidth)

        return SpanUtils()
                .append(content.substring(0, offsetForHorizontal))
                .append(ellipsizeText)
                .append(expandText)
                .setForegroundColor(ContextCompat.getColor(context,R.color.green_level1))
                .setBold()
                .setClickSpan(onExpandClickableSpan)
                .appendImage(getRightDrawableWithSameColor(colorId), SpanUtils.ALIGN_CENTER)
                .create()
    }

}
