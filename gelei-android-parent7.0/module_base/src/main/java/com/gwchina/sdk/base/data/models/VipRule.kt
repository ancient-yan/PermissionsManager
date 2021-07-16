package com.gwchina.sdk.base.data.models

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2020-03-05 11:09
 *      会员规则
 */
data class VipRule(
        val member_type: String = "01",
        val app_forbidden_timelimit_count: Int = 20,
        val app_unlimited_minimum_level: String = "高级会员",
        val family_call_out_in_enabled: String = "0",
        val family_call_out_in_enabled_minimum_level: String = "高级会员",
        val home_app_enter: String = "1",
        val home_app_enter_minimum_level: String = "会员",
        val home_family_enter: String = "1",
        val home_family_enter_minimum_level: String = "会员",
        val home_mine_add_device_enabled: String = "0",
        val home_mine_add_device_enabled_minimum_level: String = "高级会员",
        val home_net_enter: String = "0",
        val home_net_enter_minimum_level: String = "高级会员",
        val home_screenshot_enter: String = "0",
        val home_screenshot_enter_minimum_level: String = "高级会员",
        val home_time_enter: String = "1",
        val home_time_enter_minimum_level: String = "会员",
        val temp_lock_enabled: String = "0",
        val temp_lock_enabled_minimum_level: String = "高级会员",
        val temp_use_enabled: String = "0",
        val temp_use_enabled_minimum_level: String = "高级会员",
        val time_backup_plan_enabled: String = "1",
        val time_backup_plan_enabled_minimum_level: String = "高级会员",
        val time_backup_plan_enter: String = "1",
        val time_defend_available_time_enabled: String = "1",
        val time_defend_available_time_enabled_minimum_level: String = "高级会员"
)