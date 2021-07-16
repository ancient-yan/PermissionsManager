package com.gwchina.parent.apps.presentation.approval

import android.content.Context
import com.android.base.app.dagger.ContextType
import com.android.base.kotlin.colorFromId
import com.blankj.utilcode.util.SpanUtils
import com.gwchina.lssw.parent.guard.R
import com.gwchina.parent.apps.data.App
import com.gwchina.sdk.base.utils.emptyElse
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class AppRecordWrapper(
        val app: App,
        val appName: CharSequence,
        val appCreatingTime: String
)

class AppApprovalRecordMapper @Inject constructor(
        @ContextType private val context: Context
) {

    fun transformToAppWrapperList(list: List<App>?): List<AppRecordWrapper> {
        val sdp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        return list?.map {
            AppRecordWrapper(
                    it,
                    SpanUtils()
                            .append(it.soft_name?:"")
                            .append("   ")
                            .append(it.type_name.emptyElse(context.getString(R.string.unknow)))
                            .setFontSize(12, true)
                            .setForegroundColor(context.colorFromId(R.color.gray_level2))
                            .create(),

                    sdp.format(Date(it.create_time))
            )
        } ?: emptyList()

    }

}