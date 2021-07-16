package com.gwchina.parent.binding.presentation

import android.content.Context
import android.content.DialogInterface
import com.android.base.permission.IPermissionUIProvider
import com.android.base.permission.PermissionUtils
import com.blankj.utilcode.util.AppUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.widget.dialog.showConfirmDialog

class ScanPermissionDialogProvider : IPermissionUIProvider {

    override fun showPermissionRationaleDialog(context: Context, permission: Array<out String>, onContinueListener: DialogInterface.OnClickListener, onCancelListener: DialogInterface.OnClickListener) {
        showConfirmDialog(context) {
            title = context.getString(R.string.gelei_need_to_access_permission_mask, PermissionUtils.createPermissionText(context, permission.toList()))
            message = "如果不允许，您将无法使用扫一扫功能喔。"
            positiveListener = { onContinueListener.onClick(it, 0) }
            negativeListener = { onCancelListener.onClick(it, 0) }
            negativeId = R.string.not_allow
            positiveId = R.string.okay
        }
    }

    override fun showAskAgainDialog(context: Context, permission: Array<out String>, onContinueListener: DialogInterface.OnClickListener, onCancelListener: DialogInterface.OnClickListener) {
        showConfirmDialog(context) {
            val permissionNames = PermissionUtils.createPermissionText(context, permission.toList())
            title = context.getString(R.string.we_need_permission_mask, permissionNames)
            message = context.getString(R.string.permission_to_set_mask, AppUtils.getAppName(), permissionNames)
            positiveListener = { onContinueListener.onClick(it, 0) }
            negativeListener = { onCancelListener.onClick(it, 0) }
            negativeId = R.string.cancel_
            positiveId = R.string.to_set
        }
    }

    override fun showPermissionDeniedTip(contexts: Context, permission: Array<out String>) = Unit

}