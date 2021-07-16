package com.gwchina.parent.apps.common

import android.content.Context
import com.android.base.kotlin.removeWhich
import com.android.base.utils.android.ResourceUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.data.App
import com.gwchina.parent.times.common.NOT_SET_ENABLE_TIME
import com.gwchina.parent.times.common.TimeGuardDailyPlanVO
import com.gwchina.parent.times.common.calculateUsablePeriodTotalSeconds
import com.gwchina.sdk.base.data.api.*
import com.gwchina.sdk.base.utils.*
import com.gwchina.sdk.base.widget.dialog.showSelectDurationDialog
import kotlin.math.min

/**分组名最长长度*/
private const val MAX_GROUP_LETTER_COUNT = 18
/**分组最大图标数*/
internal const val MAX_GROUP_ICON_COUNT = 4
/**一天的秒数，限时可用没有设置限制时长的app的默认时长*/
internal const val MAX_USABLE_DURATION_SECOND = TOTAL_SECONDS_OF_ONE_DAY

/**待批准*/
internal const val RULE_TYPE_PENDING_APPROVAL = APP_RULE_TYPE_PENDING_APPROVAL
/**缺省*/
internal const val RULE_TYPE_DEFAULT = APP_RULE_TYPE_DEFAULT
/**自由可用*/
internal const val RULE_TYPE_FREE = APP_RULE_TYPE_FREE
/**限时可用*/
internal const val RULE_TYPE_LIMITED = APP_RULE_TYPE_LIMITED
/**禁用*/
internal const val RULE_TYPE_DISABLE = APP_RULE_TYPE_DISABLE
/**禁用*/
internal const val RULE_TYPE_RISK = APP_RULE_TYPE_RISK

internal const val FREELY_SYS_LIST = "FREELY_SYS_LIST_COMMON"
internal const val MUST_FREE_USABLE = "1"

internal fun App.isMustFreeUsable(): Boolean {
    return FREELY_SYS_LIST == this.p_type || MUST_FREE_USABLE == this.tag
}

/**待批准*/
internal fun Int.isPendingApproval(): Boolean {
    return this == RULE_TYPE_PENDING_APPROVAL
}

/**自由可用*/
internal fun Int.isFreeUsable(): Boolean {
    return this == RULE_TYPE_FREE
}

/**限时可用*/
internal fun Int.isLimitedUsable(): Boolean {
    return this == RULE_TYPE_LIMITED
}

/**禁用*/
internal fun Int.isDisabled(): Boolean {
    return this == RULE_TYPE_DISABLE
}

/**高危*/
internal fun Int.isHighRisk(): Boolean {
    return this == RULE_TYPE_RISK
}

internal fun generateAppGroupName(list: List<App>): String {
    //x_count_of_app_mask 本身占用 4 个字符
    val caesuraSign = ResourceUtils.getString(R.string.caesura_sign)
    val maxAppsName = MAX_GROUP_LETTER_COUNT - 4
    val stringBuilder = StringBuilder()
    list.forEachIndexed { index, app ->
        if (index == 0) {
            stringBuilder.append(app.soft_name)
        } else if ((stringBuilder.length + (app.soft_name ?: "").length) < maxAppsName) {
            stringBuilder.append(caesuraSign).append(app.soft_name)
        }
    }
    return ResourceUtils.getString(R.string.x_count_of_app_mask, stringBuilder.toString(), list.size)
}

/**被分好组的[list]需要要给组icon，通过此方法从[list]抽取最多包含四个元素的app icon地址列表*/
internal fun generateAppGroupIconList(list: List<App>): List<String?> {
    return list.take(min(MAX_GROUP_ICON_COUNT, list.size)).map { it.soft_icon }
}

/**根据秒值生成具体描述文本，格式为 `{x小时x分钟/天}`*/
internal fun generateDetailTimePerDayTextFromSecond(second: Int): String {
    return ResourceUtils.getString(R.string.x_per_day_mask, formatSecondsToTimeText(second))
}

/**从列表中提取 app 的 rule_id 组合，使用 , 分隔*/
internal fun List<App>.generateRuleIds(): String {
    return if (this.isEmpty()) "" else this.joinToString(",") { it.rule_id }
}

internal fun showSelectAppGuardDurationDialog(context: Context, selectedDuration: Int, showTips: Boolean, onSelected: (second: Int) -> Unit) {
    showSelectDurationDialog(context) {
        initDuration = if (selectedDuration != 0) {
            Time.fromSeconds(selectedDuration)
        } else {
            Time(0, 0)
        }
        if (showTips) {
            tips = context.getString(R.string.set_app_usable_duration_tips)
        }
        positiveListener = { _, guardTime ->
            onSelected(guardTime.toSeconds())
        }
    }
}

internal fun App.checkedTypeName(): String {
    return type_name.emptyElse(ResourceUtils.getString(R.string.unknow))
}


internal fun checkUsableDurationWhenSettingAppGroup(usableDurationSeconds: Int, selectPeriod: List<TimePeriod>, enableUsableTime: Boolean): Int {

    val totalSegmentDurationSeconds = calculateUsablePeriodTotalSeconds(selectPeriod)

    if (!enableUsableTime) {
        return totalSegmentDurationSeconds
    }

    return if (totalSegmentDurationSeconds == 0) {
        if (usableDurationSeconds == NOT_SET_ENABLE_TIME) {
            MAX_USABLE_DURATION_SECOND
        } else {
            usableDurationSeconds
        }
    } else {
        if (usableDurationSeconds > totalSegmentDurationSeconds || usableDurationSeconds == NOT_SET_ENABLE_TIME) {
            totalSegmentDurationSeconds
        } else {
            usableDurationSeconds
        }
    }
}

internal fun checkUsableDurationWhenApprovalApp(usableDurationSeconds: Int, selectPeriod: List<TimePeriod>, enableUsableTime: Boolean): Int {

    val totalSegmentDurationSeconds = calculateUsablePeriodTotalSeconds(selectPeriod)
    if (!enableUsableTime) {
        return totalSegmentDurationSeconds
    }

    return if (totalSegmentDurationSeconds == 0) {
        if (usableDurationSeconds == NOT_SET_ENABLE_TIME) {
            NOT_SET_ENABLE_TIME
        } else {
            usableDurationSeconds
        }
    } else {
        if (usableDurationSeconds > totalSegmentDurationSeconds || usableDurationSeconds == NOT_SET_ENABLE_TIME) {
            totalSegmentDurationSeconds
        } else {
            usableDurationSeconds
        }
    }
}
