package com.gwchina.parent.recommend.data

import android.os.Parcelable
import com.gwchina.sdk.base.data.api.APP_INSTALL_STATUS_NOT_INSTALLED
import kotlinx.android.parcel.Parcelize

data class RecommendResponse(
        val now_rec_info: RecommendInfo? = null,
        val system_grade_list: List<GradeInfo>? = null,
        val system_subject_soft_list: List<SoftList>? = null,
        val grade_rec_subject_list: List<SubjectInfo>? = null
)

data class SubjectDetailResponse(
        val rec_subject_info: SubjectInfo? = null,
        val rec_subject_soft_list: List<SoftItem> = emptyList()
)

data class RecommendInfo(
        val grade: String? = null,
        val group_name: String? = null,
        val rec_group_id: String? = null,
        val rec_type: String? = null
)

data class GradeInfo(
        val grade: String,
        val group_name: String = "",
        val rec_group_id: String = "",
        val rec_type: String? = null
)

data class SoftList(
        val subject_code: String = "",
        val subject_name: String = "",
        val soft_list: List<SoftItem> = emptyList()
)

data class SubjectInfo(
        val rec_subject_id: String = "",
        val subject_name: String = "",
        val subject_banner_url: String = "",
        val subject_details: String = "",
        val update_time: String = ""
)

@Parcelize
data class SoftItem(
        val bundle_id: String = "",
        //0:待接收 2已安装 1:安装中 3安装失败 4未安装
        var install_flag: Int = APP_INSTALL_STATUS_NOT_INSTALLED,
        val rec_desc: String? = null,
        val rec_level: Int = 0,
        val rec_phrase: String? = null,
        val soft_icon: String? = null,
        val soft_name: String = "",
        val type_name: String? = null
) : Parcelable {

    //状态 : 0 待接收 或 1 安装中 --》显示：安装中状态 : 2 已安装 --》显示：已安装状态 : 3 安装失败 或 4 未安装 --》显示：给孩子安装

    fun isInstalling() = (install_flag == 0 || install_flag == 1)
    fun isInstalled() = install_flag == 2

    fun setToInstalling() {
        install_flag = 1
    }

}
