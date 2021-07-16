package com.gwchina.parent.level.data


data class GuardLevel(
        val guard_level_code: Int = 0,//1-轻度； 2- 中度； 3-重度
        val guard_level_desc: String? = null,
        val guard_level_id: String = "",
        val guard_level_name: String? = "",
        val group_list: List<GroupItem>? = null
)

data class GroupItem(
        val group_name: String? = null,
        var member_only: Boolean = false,
        val item_list: List<GuardItem>? = null
)

data class GuardItem(
        val guard_item_desc: String? = "",
        val guard_item_id: String = "",
        val guard_item_code: String? = "",
        val guard_item_name: String? = "",
        val guard_item_icon: String? = null,
        /**“0”-不是必选; “1”-必选。*/
        val is_item_req: Int = 1
)