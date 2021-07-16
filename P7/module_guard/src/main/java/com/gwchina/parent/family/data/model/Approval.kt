package com.gwchina.parent.family.data.model

data class Approval(
        var approval_reason: String = "",
        var device_id: String = "",
        var group_id: String = "",
        var group_name: String = "",
        var phone: String = "",
        var phone_remark: String = "",
        var record_id: String = "",
        var rule_type: String = "",
        var update_time: Long = 0,
        var user_id: String = "",
        var user_name: String = ""
)