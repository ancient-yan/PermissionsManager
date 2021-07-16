package com.gwchina.sdk.base.utils

import java.text.NumberFormat

/**
 * 格式化浮点数。
 *
 * @param number 格式化数字
 * @param minimumFraction   最小保留的分数
 * @param maximumFraction   最大保留的分数
 * @return String
 */
fun formatDecimal(number: Float, minimumFraction: Int, maximumFraction: Int): String {
    val numberInstance = NumberFormat.getNumberInstance()
    numberInstance.minimumFractionDigits = minimumFraction
    numberInstance.maximumFractionDigits = maximumFraction
    return numberInstance.format(number.toDouble())
}

/**
 * 格式化浮点数。
 *
 * @param number 格式化数字
 * @param keep   保留的分数
 * @return String
 */
fun formatDecimal(number: Float, keep: Int): String {
    return formatDecimal(number, keep, keep)
}

/**
 * 格式化浮点数，不保留小数。
 *
 * @param number 格式化数字
 * @return String
 */
fun formatDecimalDefault(number: Float): String {
    return formatDecimal(number, 0)
}

