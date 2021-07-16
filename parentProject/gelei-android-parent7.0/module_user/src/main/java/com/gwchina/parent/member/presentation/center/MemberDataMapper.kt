package com.gwchina.parent.member.presentation.center

import android.content.Context
import com.android.base.app.dagger.ContextType
import com.gwchina.lssw.parent.user.R
import com.gwchina.sdk.base.utils.formatMilliseconds
import javax.inject.Inject

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-04-22 16:57
 */
class MemberDataMapper @Inject constructor(@ContextType private val context: Context) {

    fun formatMemberEndTime(endTime: Long): String {
        return context.getString(R.string.user_member_top_end_time, formatMilliseconds(endTime, "yyyy-MM-dd"))
    }

}