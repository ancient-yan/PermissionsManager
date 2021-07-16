package com.gwchina.parent.main.utils

import android.view.Gravity
import com.android.base.utils.android.ResourceUtils
import com.gwchina.lssw.parent.home.R
import com.gwchina.parent.main.MainActivity
import com.gwchina.parent.main.MainNavigator
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.PermissionDetail
import com.gwchina.sdk.base.utils.getGradeDescFromGrade
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-05 13:44
 */
internal fun createChildAgeGradeInfo(currentChild: Child?): String {
    return if (currentChild == null) {
        ""
    } else {
        ResourceUtils.getString(R.string.x_year_old, currentChild.age) + " " + getGradeDescFromGrade(currentChild.grade)
    }
}

/**
 * 孩子端权限弹窗标识
 */
fun buildFlag(childId: String? = null, deviceId: String? = null, isPermissionLose: Boolean = false): String {
    val cId = if (!childId.isNullOrEmpty()) childId else AppContext.appDataSource().user().currentChild?.child_user_id
    val dId = if (!deviceId.isNullOrEmpty()) deviceId else AppContext.appDataSource().user().currentDevice?.device_id
    return "${cId}_${dId}_permission_${if (isPermissionLose) "_lose" else "_no"}"
}

/**
 * [type] 0:未完成设置 1：点击触发权限丢失  2：权限丢失
 */
internal fun showPermissionTipDialog(activity: MainActivity, type: Int, mainNavigator: MainNavigator, permissionList: List<PermissionDetail>? = null, method: (() -> Unit)? = null) {
    val childName = AppContext.appDataSource().user().currentChild?.nick_name
    val currentDeviceName = AppContext.appDataSource().user().currentDevice?.device_name
    activity.showConfirmDialog {
        this.message = if (type == 0) activity.getString(R.string.home_no_permission_dialog_title) else if (type == 2) activity.getString(R.string.home_permission_dialog_title_mask, childName, currentDeviceName) else activity.getString(R.string.home_permission_dialog_title)
        this.positiveId = if (type == 0) R.string.home_no_permission_state3 else R.string.home_permission_look_detail
        this.negativeId = if (type == 1) R.string.home_permission_goon_setting else R.string.i_got_it
        this.messageGravity = if (type == 0) Gravity.CENTER else Gravity.START
        positiveListener = {
            if (type == 0) {
                mainNavigator.openChildDevicePermissionInfo()
            } else {
                permissionList?.let { it1 -> mainNavigator.openPermissionDetailPage(it1) }
            }
        }
        negativeListener = {
            when (type) {
                1 -> {
                    method?.invoke()
                }
                else -> it.dismiss()
            }
        }
    }.apply {
        setCanceledOnTouchOutside(false)
        setCancelable(false)
    }
}