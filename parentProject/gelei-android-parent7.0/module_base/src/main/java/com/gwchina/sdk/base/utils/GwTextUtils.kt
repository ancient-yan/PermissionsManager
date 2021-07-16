package com.gwchina.sdk.base.utils

import android.content.Context
import android.text.TextUtils
import com.android.base.utils.android.ResourceUtils
import com.app.base.R
import com.gwchina.sdk.base.data.models.User
import com.gwchina.sdk.base.data.models.deviceCount
import com.gwchina.sdk.base.data.models.findChildByDeviceId
import com.gwchina.sdk.base.data.models.findDevice
import java.nio.charset.Charset
import java.util.*
import java.util.regex.Pattern

/*适用于接口或者界面中的一些文本格式化*/

/**中文冒号*/
const val COLON_EN = ":"
/**英文冒号*/
const val COLON_CN = "："
/**杠*/
const val WHIPPLETREE = "-"
/**杠杠*/
const val DOUBLE_WHIPPLETREE = "--"
/**空格*/
const val BLANK = " "
/**顿号*/
const val COMMA = "、"

fun addCurrencySymbol(content: String): String {
    return ResourceUtils.getString(R.string.currency_mask, content)
}

fun addBrackets(content: String): String {
    return if (TextUtils.isEmpty(content)) "" else ResourceUtils.getString(R.string.brackets_mask, content)
}

/**浮点数值 格式化成金额表示,比如：1365.00 -->  ¥ 1,365*/
fun formatFloatToCurrency(floatValue: Float): String {
    val length = getLength(floatValue)
    return addCurrencySymbol(formatDecimal(floatValue, length))
}

/** 浮点数值 格式化成金额表示,比如：1365.00 -->  1,365*/
fun formatFloat(floatValue: Float): String {
    val length = getLength(floatValue)
    return formatDecimal(floatValue, length)
}

private fun getLength(floatValue: Float): Int {
    var length: Int
    val value = floatValue.toString()
    val i = value.lastIndexOf(".")
    if (i == -1) {
        length = 0
    } else {
        val decimals = java.lang.Float.parseFloat(value.substring(i + 1))
        if (decimals == 0f) {
            length = 0
        } else {
            length = value.length - i - 1
            length = if (length < 0) 0 else if (length > 2) 2 else length
        }
    }
    return length
}

fun CharSequence?.foldText(maxSize: Int, replace: String = "..."): CharSequence {
    return if (this == null || this.length <= maxSize) {
        ""
    } else {
        "${this.subSequence(0, maxSize)}$replace"
    }
}

/**超过[maxSize] 显示 [maxSize] 个字符 + [replace]*/
fun String?.foldText(maxSize: Int, replace: String = "..."): String {
    return if (this.isNullOrEmpty() || this.length <= maxSize) {
        this ?: ""
    } else {
        "${this.subSequence(0, maxSize)}$replace"
    }
}

data class Birthday(
        val year: Int,
        val monty: Int,
        val day: Int
) {

    fun joinToString(separator: String = "-"): String {
        return arrayOf(year.toString(), monty.toString(), day.toString()).joinToString(separator)
    }

    fun toList() = listOf(year, monty, day)

    fun toStringList() = listOf(year.toString(), monty.toString(), day.toString())

    fun toCalendar(): Calendar {
        val instance = Calendar.getInstance()
        if (year != 0) {
            instance.set(Calendar.YEAR, year)
        }
        if (monty != 0) {
            instance.set(Calendar.MONTH, monty - 1)
        }
        if (day != 0) {
            instance.set(Calendar.DAY_OF_MONTH, day)
        }
        return instance
    }

}

fun String?.emptyElse(elseStr: String) = if (this.isNullOrEmpty()) {
    elseStr
} else {
    this
}

/**拆分类似 20190202 的日期格式，返回年、月、日*/
fun splitBirthday(birthday: String?): Birthday {
    if (birthday == null || birthday.length != 8) {
        return Birthday(0, 0, 0)
    }
    val year = birthday.substring(0, 4).toIntOrNull() ?: 0
    val month = birthday.substring(4, 6).toIntOrNull() ?: 0
    val day = birthday.substring(6, birthday.length).toIntOrNull() ?: 0
    return Birthday(year, month, day)
}

/** 隐藏 11 位手机号的 4-7 位*/
fun hidePhoneNumber(phoneNumber: String?): String {
    if (phoneNumber == null) {
        return ""
    }
    if (phoneNumber.length != 11) {
        return phoneNumber
    }
    return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7)
}

/**
 * 根据设备和孩子数量，生成对应的设备所属提示：
 *
 *- （1）只有一个小孩一台设备：不在相关页面显示设备名；
 *- （2）只有一个小孩有多台设备：在相关页面显示设备名；
 *- （3）超过一个小孩且有多台设备：在相关页面显示XX（孩子昵称）的yyy（设备名）；
 *- （4）超过一个小孩但只有一台设备：不在相关页面显示设备名；
 *- （5）超过一个小孩但只有一个小孩有多台设备：在相关页面显示设备名；
 *
 */
fun generateDeviceFlag(context: Context, user: User?, deviceId: String?, foldChildName: Boolean = true): String {

    if (user == null || deviceId.isNullOrEmpty() || user.deviceCount() <= 1) {
        return ""
    }

    val multiChildHasDevice = user.childList?.filter {
        !it.device_list.isNullOrEmpty()
    }?.size ?: 0 > 1

    return if (multiChildHasDevice) {

        val childName = user.findChildByDeviceId(deviceId)?.nick_name.also {
            if (foldChildName) {
                it.foldText(10)
            } else {
                it
            }
        }

        context.getString(R.string.x_of_y, childName, user.findDevice(deviceId)?.device_name ?: "")
    } else {
        return user.findDevice(deviceId)?.device_name ?: ""
    }

}

/**超过[maxSize]个中文字符 显示 [maxSize] 个字符 + [replace] , 数字和字母占0.5个字符*/
fun String?.subText(maxSize: Int, replace: String = "..."): String {
    when {
        this.isNullOrEmpty() -> return ""
        this.length <= maxSize -> return this
        else -> {
            val toByteArray = this.toByteArray(Charset.forName("GB2312"))
            if (toByteArray.size > maxSize * 2) {
                val copyOfRange = toByteArray.copyOfRange(0, maxSize * 2)
                var gbToString = String(copyOfRange, Charset.forName("GB2312"))
                val lastString = gbToString.last().toString()
                if (!isContainChinese(lastString) && !isContainLetters(lastString)) {
                    gbToString = gbToString.substring(0, gbToString.length - 1)
                }
                return gbToString + replace
            }
            return this
        }
    }
}

/**判断字符是否含有表情符号*/
fun String.isContainEmoji(): Boolean {
    for (item: Char in this) {
        if (getIsEmoji(item)) {
            return true
        }
    }
    return false
}


/**
 * 判断字符串是否包含中文
 *
 * @param str
 * @return
 */
fun isContainChinese(str: String): Boolean {
    val p = Pattern.compile("[\u4e00-\u9fa5]")
    val m = p.matcher(str)
    return m.find()
}

/**
 * 判断字符串是否包含字母和数字
 * @param str
 * @return
 */
fun isContainLetters(str: String): Boolean {
    var i = 'A'
    while (i <= 'Z') {
        if (str.contains(i.toString())) {
            return true
        }
        i++
    }

    var t = 'a'
    while (t <= 'z') {
        if (str.contains(t.toString())) {
            return true
        }
        t++
    }

    var n = '0'
    while (n <= '9') {
        if (str.contains(n.toString())) {
            return true
        }
        n++
    }
    return false
}

/**
 * 判断字符是否是表情符号
 * @param char
 * @return
 */
fun getIsEmoji(char: Char): Boolean {
    val codePoint = char.toInt()
    if ((codePoint == 0) || (codePoint == 9) || (codePoint == 10)
            || (codePoint == 13)
            || ((codePoint >= 32) && (codePoint <= 55295))
            || ((codePoint >= 57344) && (codePoint <= 65533))
            || ((codePoint >= 65536) && (codePoint <= 1114111)))
        return false
    return true
}

