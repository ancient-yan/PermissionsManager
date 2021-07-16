package com.gwchina.sdk.base.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.gwchina.sdk.base.data.api.*
import kotlinx.android.parcel.Parcelize


data class User(
        /**家长信息*/
        val patriarch: Patriarch = Patriarch(),
        /**该家长绑定的孩子列表*/
        val childList: List<Child>? = null,
        /**会员信息*/
        val member_info: Member? = null,
        /**当前默认的孩子，如果[currentChild]==null，则说明当前还没有绑定的孩子*/
        val currentChild: Child? = null,
        /**当前默认的孩子的默认设备，如果[currentDevice]==null，则说明当前的孩子没有绑定任何设备*/
        val currentDevice: Device? = null,
        /**会员等级权限信息*/
        var vipRule: VipRule?
        ) {

    companion object {
        val NOT_LOGIN = User(Patriarch(), mutableListOf(),vipRule=null)
    }


}

/**家长信息*/
data class Patriarch(
        val user_id: String = "",
        val nick_name: String? = "",
        val head_photo_path: String? = null,
        @SerializedName("birthdate")
        val birthday: String? = null,
        val email: String? = null,
        val phone: String? = null,
        val real_name: String? = null,
        val sex: Int = INVALIDATE,
        val reg_from: String? = null,
        val reg_ip: String? = null,
        val age: Int = 0,
        val area_code: String? = "",
        val province_area_code: String? = "",
        val province_area_name: String? = "",
        val city_area_code: String? = "",
        val city_area_name: String? = "",
        val district_area_code: String? = "",
        val district_area_name: String? = ""
)


/**孩子信息*/
data class Child(
        val age: Int = 0,
        @SerializedName("birthdate")
        val birthday: String? = "",
        val child_user_id: String = "",
        val device_list: List<Device>? = null,
        val grade: Int = 0,
        val nick_name: String? = "",
        val sex: Int = SEX_FEMALE,
        val head_photo_path: String? = null,
        val p_relationship_name: String? = "",
        val p_relationship_code: Int = 0,
        val status: Int = MEMBER_GUARD_STATUS_NORMAL
) {

    fun boundDevice(): Boolean {
        return device_list != null && device_list.isNotEmpty()
    }

    fun moreThanOneDevice(): Boolean {
        return device_list?.size ?: 0 > 1
    }

    fun reachMaxDeviceCount(): Boolean {
        return device_list?.size ?: 0 >= MAX_DEVICE_COUNT_PER_CHILD
    }

}

data class LoginResponse(
        /**Y：需去填写密码，设置密码后则表示注册成功*/
        val register_flag: String?,
        val app_token: String,
        val expire_time: Long,
        val login_info: LoginData,
        val subjoin_info: LoginAttachment?
)

@Parcelize
data class LoginAttachment(
        /**登陆后的提示*/
        val register_give_describe: String? = null,
        /**迁移标识*/
        val greed_box_init_setting: String? = MIGRATING_NONE,
        /**不迁移设备名称列表*/
        val not_upgrade_device_list: List<String>? = null
) : Parcelable {

    /**
     * 是否需要迁移
     */
    fun isNeedMigrating() = greed_box_init_setting != null && greed_box_init_setting != MIGRATING_NONE

}

data class Member(
        val user_id: String = "",
        val member_type: String? = "",//会员等级
        val member_level: String? = "",
        val status: String? = MEMBER_STATUS_INVALID,
        val expire_setting_retain_flag: Int = INVALIDATE,
        val record_id: String = "",
        val begin_time: Long = 0,
        val end_time: Long = 0,
        val head_photo_path: String? = "",
        val nick_name: String? = "",
        val member_level_name: String? = "",
        val member_level_desc: String? = ""
)

data class LoginData(
        val patriarch: Patriarch?,
        @SerializedName("child_list")
        val childList: MutableList<Child>?,
        @SerializedName("member_info")
        val memberInfo: Member?,
        @SerializedName("child_user_id")
        val childUserId: String?,
        @SerializedName("child_device_id")
        val childDeviceId: String?
)