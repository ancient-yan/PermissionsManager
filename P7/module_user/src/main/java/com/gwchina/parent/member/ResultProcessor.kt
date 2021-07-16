package com.gwchina.parent.member

import android.app.Activity
import android.content.Intent
import com.android.base.app.activity.ActivityDelegate
import com.gwchina.sdk.base.router.RouterPath

/**
 * @author Zhanghb
 * Email: 2573475062@qq.com
 * Date : 2019-05-16 16:36
 */
class ResultProcessor(private val memberActivity: MemberActivity) : ActivityDelegate<MemberActivity> {
    init {
        memberActivity.addDelegate(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == RouterPath.MemberCenter.REQUEST_CODE) {
            memberActivity.processIntent(memberActivity.intent)
        }
    }
}