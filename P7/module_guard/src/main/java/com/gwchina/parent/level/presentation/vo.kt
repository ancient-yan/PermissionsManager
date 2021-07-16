package com.gwchina.parent.level.presentation

data class GuardLevelVO(
        val id: String,
        val guardLevel: Int,
        val name: String,
        val desc: CharSequence,
        val items: List<GuardGroupItemVO>
)

data class GuardGroupItemVO(
        val group_name: String? = null,
        val isRequireMember: Boolean = false,
        val item_list: List<GuardItemVO> = emptyList()
)

data class GuardItemVO(
        val id: String,
        val code: String,//用于映射图片
        val name: String,
        val desc: CharSequence,
        val normalIcon: String?,//未选中图标
        var isSelected: Boolean = true,
        var must: Boolean = true
)
