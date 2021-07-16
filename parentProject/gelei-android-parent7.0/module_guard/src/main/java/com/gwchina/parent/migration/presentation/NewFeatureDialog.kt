package com.gwchina.parent.migration.presentation

import android.content.Context
import com.android.base.kotlin.colorFromId
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.sdk.base.widget.dialog.BaseDialog
import kotlinx.android.synthetic.main.migration_dialog_what_new.*

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-30 15:41
 */
class NewFeatureDialog(context: Context) : BaseDialog(context) {

    init {
        setContentView(R.layout.migration_dialog_what_new)
        setupViews()
    }

    private fun setupViews() {
        ivMigrationDialogClose.setOnClickListener {
            dismiss()
        }

        val color = context.colorFromId(R.color.gray_level2)
        tvMigrationDialogNewFeatures1.text = SpanUtils().append("守护升级").append("   ").append("更稳定的守护").setForegroundColor(color).create()
        tvMigrationDialogNewFeatures2.text = SpanUtils().append("视觉焕新").append("   ").append("更轻盈的视觉体验").setForegroundColor(color).create()
        tvMigrationDialogNewFeatures3.text = SpanUtils().append("成长日记").append("   ").append("留住孩子的成长瞬间").setForegroundColor(color).create()
        tvMigrationDialogNewFeatures4.text = SpanUtils().append("成长树").append("   ").append("用爱守护成长").setForegroundColor(color).create()
        tvMigrationDialogNewFeatures5.text = SpanUtils().append("守护升级").append("   ").append("对比TA人了解自己").setForegroundColor(color).create()
        tvMigrationDialogNewFeatures6.text = SpanUtils().append("同龄人排行").append("   ").append("助你了解孩子用机偏好").setForegroundColor(color).create()
    }

}