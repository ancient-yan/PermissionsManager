package com.gwchina.parent.main.data

import android.os.Parcelable
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.Member
import kotlinx.android.parcel.Parcelize


data class HomeResponse(
        val approval_info: ApprovalInfo? = null,
        val child_location: ChildLocation? = null,
        val device_guard_item: Device? = null,
        val same_age_rec_install: List<Soft>? = null,
        val time_line_record: List<TimeLineRecord>? = null,
        val top_child_info: TopChildInfo? = null,
        val use_overview: UseOverview? = null,
        val last_week_guard_report: Weekly? = null,
        val rec_soft_info: SoftRecommendInfo? = null,
        val member_info: Member? = null,
        val exist_tag: Int = 0
)

data class UseOverview(
        val to_day_preference_soft_type: String? = null,
        val to_day_step_sport: Int = 0,
        val to_day_use_time: Int = 0
)

data class SoftRecommendInfo(
        val grade: String = "",
        val group_name: String = "",
        val rec_desc: String = "",
        val rec_group_id: String = "",
        val rec_type: String = ""
)

data class ApprovalInfo(
        val phone_approval: PhoneApproval? = null,
        val soft_approval: SoftApproval? = null
)

data class PhoneApproval(
        val phone_approval_list: List<PhoneApprovalInfo>? = null,
        val phone_approval_count: Int = 0
)

data class PhoneApprovalInfo(
        val approval_reason: String = "",
        val phone: String = "",
        val phone_remark: String? = null,
        val record_id: String = "",
        val user_name: String = "",
        val user_id: String = "",
        val device_id: String = "",
        val update_time: Long = 0
)

data class SoftApproval(
        val child_count: Int = 0,
        val device_count: Int = 0,
        val record_approval: List<SoftApprovalInfo> = emptyList(),
        val record_count: Int = 0
)

data class SoftApprovalInfo(
        val child_create_time: String = "",
        val child_user_id: String = "",
        val device_id: String = "",
        val device_name: String = "",
        val nick_name: String = "",
        val rule_soft: List<Soft> = emptyList()
)

data class Soft(
        val bundle_id: String = "",
        val rule_id: String = "",
        val rule_type: String? = null,
        val soft_icon: String? = null,
        val soft_name: String = "",
        val type_code: String? = null,
        val type_name: String? = null,
        val softtype_name: String? = null,
        val install_count: Int = 0,
        val update_time: Long = 0,
        val install_flag: Int = 0
) {

    fun getTypeName(): String? {
        if (type_name.isNullOrEmpty()) {
            return softtype_name
        }
        return type_name
    }

}

data class TimeLineRecord(
        val group_data: List<GroupData>? = null,
        val group_time: Long = 0
)

data class GroupData(
        val use_title: String = ""
)

data class TopChildInfo(
        val age: Int,
        val child_user_id: String,
        val grade: Int,
        val head_photo_path: String,
        val nick_name: String,
        val sex: String,
        val total_growth: Int,
        //背景图
        val home_top_img: String = ""
)

data class Weekly(
        val week_start_date: String? = null,
        val week_end_date: String? = null,
        val week_report_count: Int = 0,
        val week_describe: String? = null,
        val week_guard_report: ReportInfo? = null
)

/**孩子位置*/
@Parcelize
data class ChildLocation(
        val accurate: Float = 0F,
        val csys: String? = null,
        val formatted_address: String? = null,
        val lat: Double = 0.0,
        val lng: Double = 0.0,
        val upload_time: Long = 0,
        val battery_level: Int = 0
) : Parcelable

/**添加临时可用响应*/
data class TempUsable(
        val device_id: String,
        val enabled: String,
        val rule_id: String,
        val update_time: Any,
        val usable_begin_time: Long,
        val usable_end_time: Long,
        val user_id: String
)

/**我的界面信息*/
data class MineResponse(
        val phone: String? = null,
        val user_id: String? = null,
        val nick_name: String? = null,
        val head_photo_path: String? = null,
        val exist_notification: Int = 0,
        val exist_report: Int = 0,
        val is_member: Int = 0,
        val member_word: String? = null,
        val end_time: Long = 0,
        val level_list: List<ChildGrowthValue>? = null,
        val child_list: List<ChildInfo>? = null
)

/**孩子信息*/
data class ChildInfo(
        val age: Int,
        val child_growth: Int,
        val child_head_photo_path: String,
        val child_level: Int,
        val child_nick_name: String,
        val child_user_id: String,
        val device_count: Int,
        val grade: Int,
        val status: String
)

/**孩子成长值*/
data class ChildGrowthValue(
        val level: Int = 0,
        val begin_growth: Int = 0,
        val end_growth: Int = 0
)

data class WeeklyInfo(
        val month: String? = null,
        val report_list: List<ReportInfo>? = null
)

data class ReportInfo(
        val record_id: String? = null,
        val url: String? = null,
        val begin_date: String? = null,
        val end_date: String? = null,
        val record_time: String? = null,
        val create_time: Long = 0,
        val user_id: String? = null,
        val nick_name: String? = null,
        val comment: String? = null
)

/**
 * 手机使用记录
 */
data class UsingRecord(
        val time_line_record: List<TimeLineRecord>? = null
)
