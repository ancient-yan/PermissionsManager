package com.gwchina.parent.apps.data

import android.os.Parcelable
import com.gwchina.parent.apps.common.RULE_TYPE_DEFAULT
import com.gwchina.sdk.base.data.api.FLAG_NEGATIVE
import com.gwchina.sdk.base.data.models.TimePart
import kotlinx.android.parcel.Parcelize


data class GuideAppListResponse(
        val freely_list: List<App> = emptyList(),
        val soft_list: List<AppCategory> = emptyList()
)

data class AppListResponse(
        /**非分组限时可用列表*/
        val soft_group_list: List<AppGroup> = emptyList(),
        /**分组限时可用列表*/
        val soft_single_list: List<App> = emptyList(),
        /**是否有待审批记录*/
        val is_has_approved_record: Int = FLAG_NEGATIVE
)

@Parcelize
data class App(
        val bundle_id: String? = "",
        val device_id: String? = "",
        /**类别排序*/
        val row_order: Int = 0,
        val rule_id: String = "",
        /**守护模式，N 表示缺省状态，0-待批准、1-自由使用、2-限时可用、3-禁用。*/
        var rule_type: Int = RULE_TYPE_DEFAULT,
        /**所属分组ID*/
        val soft_group_id: String? = null,
        /**所属分组名称*/
        val soft_group_name: String? = null,
        val soft_icon: String? = null,
        val soft_name: String? = "",
        val type_code: String? = "",
        val type_name: String? = null,
        /**已使用时长，全部为null，没有统计*/
        val used_time: Int = 0,
        /**应用守护设置的可用时长，大于0的整数，不大于86400，单位秒*/
        var used_time_perday: Int = 0,
        /**表示是否自由可用不可修改*/
        var p_type: String? = null,
        var tag: String? = null,
        /**创建时间*/
        var create_time: Long = 0,
        /**可用时段*/
        var soft_fragments: List<TimePart>? = null
) : Parcelable

data class AppCategory(
        val type_code: String?,
        val type_name: String?,
        val group_list: List<App>,
        val row_order: Int
)

@Parcelize
data class AppGroup(
        var soft_group_id: String? = null,
        var soft_group_name: String? = null,
        var soft_list: List<App> = emptyList(),
        val update_time: String? = null,
        val used_time: Int = 0,
        var group_used_time_perday: Int = 0,
        var group_fragment: List<TimePart>? = null
) : Parcelable

class SetAppRulesRequest {
    var freelyRuleIds: String? = null
    var forbidRuleIds: String? = null
    var singleRuleList: List<SingleRule>? = null
    var groupRuleList: List<GroupRule>? = null

    data class SingleRule(val rule_id: String, val used_time_perday: Int)
    data class GroupRule(val soft_group_name: String?, val rule_ids: String, val used_time_perday: Int)
}

