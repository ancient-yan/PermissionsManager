package com.gwchina.sdk.base.utils

import android.graphics.Typeface
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.text.InputFilter
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.widget.EditText
import android.widget.TextView
import com.android.base.kotlin.colorFromId

fun TextInputLayout.textStr(): String {
    val editText = this.editText
    return editText?.text?.toString() ?: ""
}

fun EditText.textStr(): String {
    return this.text?.toString() ?: ""
}

fun TextView.enableSpanClickable() {
    // 响应点击事件的话必须设置以下属性
    movementMethod = LinkMovementMethod.getInstance()
    // 去掉点击事件后的高亮
    highlightColor = ContextCompat.getColor(context, android.R.color.transparent)
}

fun TextView.setTextColorResId(colorId: Int) {
    setTextColor(context.colorFromId(colorId))
}

/**设置自定义字体*/
fun TextView.setTextWithTypeface(content: CharSequence, typefacePath: String? = null) {
    var path = typefacePath
    if (path.isNullOrEmpty()) {
        path = "fonts/DINCondensedBold.woff.ttf"
    }
    val font = Typeface.createFromAsset(context?.assets, path)
    typeface = font
    text = content
}


fun EditText.disableEmojiEntering() {
    val filter = EmojiExcludeFilter()
    val oldFilters = filters
    val oldFiltersLength = oldFilters.size
    val newFilters = arrayOfNulls<InputFilter>(oldFiltersLength + 1)
    if (oldFiltersLength > 0) {
        System.arraycopy(oldFilters, 0, newFilters, 0, oldFiltersLength)
    }
    //添加新的过滤规则
    newFilters[oldFiltersLength] = filter
    filters = newFilters
}

private class EmojiExcludeFilter : InputFilter {
    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        for (i in start until end) {
            val type = Character.getType(source[i])
            if (type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()) {
                return ""
            }
        }
        return null
    }
}