package com.gwchina.parent.migration.data

import com.gwchina.sdk.base.data.api.FLAG_NEGATIVE
import com.gwchina.sdk.base.data.api.isFlagPositive
import com.gwchina.sdk.base.data.api.isIOS


data class MigrationResponse(
        val migration_record_list: List<MigratingDevice>? = null
)

data class MigratingDevice(
        val device_id: String? = "",
        val device_name: String? = "",
        val device_sn: String? = "",
        val device_type: String? = "",
        //是否可升级 1 是 0 否
        val is_upgrade: Int = FLAG_NEGATIVE,
        /*孩子端是否已升级：0 未安装 、1 已安装 、2 未知*/
        val child_device_upgrade_flag: Int = FLAG_NEGATIVE,
        /*iOS孩子端描述文件是否安装：0 未安装 、1 已安装 、2 未知*/
        val ios_description_flag: Int = FLAG_NEGATIVE
) {
    /**
     * 完成升级的标准：
    （1）安卓：孩子端app升级至7.0版本；
    （2）iOS：孩子端app升级至7.0版本，同时孩子端的描述文件也安装好；
    （3）iOS未升级的提示文案如图，分开版本升级和描述文件安装的说明；
     */
    fun canDoMigrating() = isFlagPositive(child_device_upgrade_flag) && !isIOS(device_type) || (isIOS(device_type)) && isFlagPositive(ios_description_flag)

}