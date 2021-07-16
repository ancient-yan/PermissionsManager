package com.gwchina.parent.family.data.model

data class FamilyPhoneInfo(
        var enabled: String = "0",//是否开启
        var group_phone_list: List<GroupPhone> = listOf(),
        var has_no_approved: String = "0",//是否存在待审批
        var is_call_in: String = "0",//是否限制呼入（可接听范围受限制）
        var is_call_out: String = "0"//是否限制呼出（可拨打范围受限制）
)