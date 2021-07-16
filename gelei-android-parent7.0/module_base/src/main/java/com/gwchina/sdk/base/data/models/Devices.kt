package com.gwchina.sdk.base.data.models

import android.os.Parcelable
import com.gwchina.sdk.base.data.api.*
import kotlinx.android.parcel.Parcelize

/**孩子设备信息*/
data class Device(
        /**创建时间*/
        val create_time: Long = 0L,
        /**设备ID(服务器)*/
        val device_id: String = "",
        /**设备名称*/
        val device_name: String? = null,
        /**设备号*/
        val device_sn: String? = "",
        /**设备类型*/
        val device_type: String? = "",
        /**操作系统版本*/
        val os_version: String? = "",
        /**操作系统名称*/
        val os_name: String? = "",
        /**守护模式：1 轻度 2 中度 3 重度*/
        val guard_level: Int = INVALIDATE,
        /**该设备的守护项目*/
        val guard_item_list: List<GuardItem>? = null,
        /**设备可用总时长*/
        val enabled_time: Int = 0,
        /**设备状态：0 离线 1 在线 2 设备未注册 3 异常*/
        val on_line_flag: Int = 0,
        /**已使用时长*/
        val used_time: Int = 0,
        /**时间守护配置*/
        val rule_time_list: List<TimePeriodRule>? = null,
        /**临时可用*/
        val temp_usable_time: TempTimePeriodRule? = null,
        /**剩余可用时长(秒)*/
        val surplus_used_time: Int = 0,
        /**是否设置过应用守护*/
        val first_setting_soft_flag: String? = NO_FLAG,
        /**是否设置过时间守护*/
        val first_setting_time_flag: String? = NO_FLAG,
        /**是否设置了时间守护*/
        val setting_time_flag: String? = NO_FLAG,
        /**是否设置了应用守护*/
        val setting_soft_flag: String? = NO_FLAG,
        /**是否设置了亲情号码*/
        val setting_phone_flag: String? = NO_FLAG,
        /**是否设置了网址模式（智能拦截）*/
        var setting_url_pattern_flag: String? = NO_FLAG,
        /**Y/N , 当前孩子设备在时间守护允许使用范围内*/
        val rule_time_flag: String? = NO_FLAG,
        /**绑定状态：1-正常; 0-失效，守护过期*/
        val status: Int = MEMBER_GUARD_STATUS_NORMAL,
        /**0~100，没有数据则未null*/
        val battery_level: Int = 0,
        /**该设备在所属孩子设备列表中的索引，从 1 开始*/
        val index: Int = 0,
        /**ios监督模式：1-已开启、0-未开启、null-未知(非iOS孩子端为null)*/
        val ios_supervised_flag: Int? = null,
        /**ios描述文件：1-已安装、0-未安装(非iOS孩子端为null)*/
        val ios_description_flag: Int? = null,
        /**孩子端是否升级：0 未升级 、1 已升级 、2 未知*/
        val child_device_upgrade_flag: Int? = 0,
        /**是否为定制机：Y 定制机，N 非定制机*/
        val custom_device_flag: String? = NO_FLAG,
        /**定制机守护网址和亲情号码是否可用开关:表示被屏蔽的功能模块，如果对应模块没有在disabled_items 字段中，则不屏蔽*/
        val disabled_items: List<String?>?,
        /**是否锁屏中，非临时锁屏*/
        val is_screen_lock: Boolean = false
) {

    fun hasSetLevel(): Boolean {
        return guard_level.isSevereMode() || guard_level.isModerateMode() || guard_level.isMildMode()
    }

    /**守护网址不可用*/
    fun isGuardUrlDisabled(): Boolean {
        if (disabled_items.isNullOrEmpty()) return false
        return (disabled_items.contains(GUARD_URL_DISABLE))
    }

    /**亲情号码不可用*/
    fun isFamiliarityPhoneDisabled(): Boolean {
        if (disabled_items.isNullOrEmpty()) return false
        return (disabled_items.contains(FAMILIARITY_PHONE))
    }
}

/**守护时间段*/
data class TimePeriodRule(
        /**规则类型*/
        val rule_id: String? = null,
        /**范围可用时长*/
        val enabled_time: Int = 0,
        /**星期几*/
        val what_day: Int = 0,
        /**时间段*/
        val rule_time_fragment: List<TimePart>? = null
)

/**临时可用时间段*/
data class TempTimePeriodRule(
        /**规则类型*/
        val rule_id: String? = null,
        /**临时可用 1临时锁屏*/
        val mode: Int = 0,
        /**可用时间段，时间戳*/
        val begin_time: Long = 0,
        val end_time: Long = 0
)

/**守护时间片，格式：“HH:mm”, 24 小时制*/
@Parcelize
data class TimePart(val begin_time: String, val end_time: String) : Parcelable

/**守护项目*/
data class GuardItem(
        val guard_item_code: String = "",
        val guard_item_name: String = ""
)