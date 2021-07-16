@file:JvmName("GwUIKit")

package com.gwchina.sdk.base.utils

import android.app.Activity
import android.content.Context
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.android.base.kotlin.colorFromId
import com.android.base.utils.android.ResourceUtils
import com.app.base.R
import com.gwchina.sdk.base.data.api.*
import com.qmuiteam.qmui.util.QMUIStatusBarHelper

/** 设置状态栏黑色字体图标，返回 true 表示设置成功 */
fun Activity?.setStatusBarLightMode(): Boolean {
    return QMUIStatusBarHelper.setStatusBarLightMode(this)
}

/** 设置状态栏白色字体图标，返回 true 表示设置成功 */
@Suppress("UNUSED")
fun Activity?.setStatusBarDarkMode(): Boolean {
    return QMUIStatusBarHelper.setStatusBarDarkMode(this)
}

fun mapParentAvatarBig(relationship_code: Int?): Int {
    return if (relationship_code != null) {
        when (relationship_code) {
            RELATIONSHIP_FATHER -> R.drawable.img_head_father_50
            RELATIONSHIP_MOTHER -> R.drawable.img_head_mother_50
            else -> R.drawable.img_head_other_50
        }
    } else {
        R.drawable.img_default_avatar
    }
}

fun mapParentAvatarSmall(relationship_code: Int?): Int {
    return if (relationship_code != null) {
        when (relationship_code) {
            RELATIONSHIP_FATHER -> R.drawable.img_head_father_38
            RELATIONSHIP_MOTHER -> R.drawable.img_head_mother_38
            else -> R.drawable.img_head_other_38
        }
    } else {
        R.color.gray_level4
    }
}

fun mapParentRelation(relationship_code: Int, sex : Int): Int {
    return when (relationship_code) {
            RELATIONSHIP_FATHER, RELATIONSHIP_MOTHER -> if (sex.isMale()) R.string.son else R.string.girl
            else -> R.string.relatives
        }
}

fun mapChildAvatarSmall(sex: Int?): Int {
    if (sex == null) {
        return R.color.gray_level4
    }
    return if (sex.isFemale()) {
        R.drawable.img_head_girl_38
    } else {
        R.drawable.img_head_boy_38
    }
}

fun mapChildAvatarBig(sex: Int?): Int {
    if (sex == null) {
        return R.color.gray_level4
    }
    return if (sex.isFemale()) {
        R.drawable.img_head_girl_50
    } else {
        R.drawable.img_head_boy_50
    }
}

fun mapGuardLevelName(guardLevel: Int): String {
    return when (guardLevel) {
        GUARD_LEVEL_MILD -> {
            ResourceUtils.getString(R.string.mild)
        }
        GUARD_LEVEL_MODERATE -> {
            ResourceUtils.getString(R.string.moderate)
        }
        GUARD_LEVEL_SEVERE -> {
            ResourceUtils.getString(R.string.severe)
        }
        else -> ResourceUtils.getString(R.string.not_set)
    }
}

val layoutCommonEdge = ResourceUtils.getDimensPixelSize(R.dimen.common_edge)
val layoutCommonBigEdge = ResourceUtils.getDimensPixelSize(R.dimen.common_edge_big)

fun getGradeNameByGrade(grade: Int): String {
    val gradeArray = ResourceUtils.getStringArray(R.array.child_grade_array)
    if (grade >= 0 && grade < gradeArray.size) {
        return gradeArray[grade]
    }
    return ""
}

fun newGwStyleClickSpan(context: Context, textColor: Int = 0, onClick: () -> Unit): ClickableSpan {
    //隐私协议
    return object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick()
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = if (textColor == 0) context.colorFromId(R.color.blue_level2) else textColor
            ds.isUnderlineText = false
        }
    }
}