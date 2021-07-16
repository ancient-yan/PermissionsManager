package com.gwchina.sdk.base.utils

import com.android.base.utils.android.ResourceUtils
import com.gwchina.sdk.base.AppContext
import java.util.*

/*
8月 是指当前时间的八月份：

0周岁至6周岁间，8月份前，一年级以下，8月份后，一年级；
6周岁至7周岁间，8月份前，一年级，8月份后，二年级；
7周岁至8周岁间，8月份前，二年级，8月份后，三年级；
8周岁至9周岁间，8月份前，三年级，8月份后，四年级；
9周岁至10周岁间，8月份前，四年级，8月份后，五年级；
10周岁至11周岁间，8月份前，五年级，8月份后，六年级；
11周岁至12周岁间，8月份前，六年级，8月份后，七年级（初一）；
12周岁至13周岁间，8月份前，七年级（初一），8月份后，八年级（初二）；
13周岁至14周岁间，8月份前，八年级（初二），8月份后，九年级（初三）；
14周岁至15周岁间，8月份前，九年级（初三），8月份后，十年级（高一）；
15周岁至16周岁间，8月份前，十年级（高一），8月份后，十一年级（高二）；
16周岁至17周岁间，8月份前，十一年级（高二），8月份后，十二年级（高三）；
17周岁至18周岁间，8月份前，十二年级（高三），8月份后，大学；
18周岁后，统一为大学
 */

fun getRecommendedGradeByBirthday(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance()
    val curYear = calendar.get(Calendar.YEAR)
    var yearOffset = curYear - year
    //不满周岁
    if (month > 8) {
        yearOffset--
    }
    return when {
        yearOffset < 5 -> 0
        yearOffset > 17 -> 13
        else -> yearOffset - 5
    }
}

fun getGradeDescFromGrade(grade: Int): String {
    return if (grade in 0..13) {
        val resource = ResourceUtils.getResource("grade_name_$grade", "string", AppContext.getContext().packageName)
        ResourceUtils.getString(resource)
    } else {
        ""
    }
}