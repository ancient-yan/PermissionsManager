package com.gwchina.sdk.base.linker

import android.content.Context
import android.content.Intent
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.widget.member.MemberExpiringTipsTask
import timber.log.Timber
import kotlin.reflect.KClass

/** ID：必填, 具体跳转到哪个页面*/
internal const val JUMP_PARAM_ID = "ID"

/** popType : 选填，此次跳转之后是否需要弹窗操作  为0则不需要(可不传) 其他值则参考popType值 */
internal const val JUMP_PARAM_POP_TYPE = "popType"

/**会员到期前7天提醒*/
const val POP_PARAM_MEMBER_TIPS = "1102"

const val JUMP_ID_MAIN_HOME = "1101"
const val JUMP_ID_MAIN_DIARY = "1201"
const val JUMP_ID_MAIN_MINE = "1301"
const val JUMP_ID_APP_RECOMMENDATION = "1102"
const val JUMP_ID_MEMBER_CENTER = "1401"
const val JUMP_ID_PURCHASE_MEMBER = "1402"

internal val jumperMap: Map<String, KClass<*>> by lazy {
    mutableMapOf(
            JUMP_ID_MAIN_HOME to JumpToHomePage::class,
            JUMP_ID_PURCHASE_MEMBER to JumpToPurchaseMemberPage::class,
            JUMP_ID_MEMBER_CENTER to JumpToMemberCenterPage::class,
            JUMP_ID_APP_RECOMMENDATION to JumpToAppRecommendationPage::class,
            JUMP_ID_MAIN_DIARY to JumpToHomeDiaryPage::class,
            JUMP_ID_MAIN_MINE to JumpToHomeMinePage::class
    )
}

interface Jumpable {
    /**
     * 是否需要登录
     */
    fun requestLogin(): Boolean = false

    /**
     * 跳转
     * @param context
     * @param params 参数集合，根据每个页面的需要从集合读取
     */
    fun jump(context: Context, params: Map<String, String>)
}

/** 首页 */
private class JumpToHomePage : Jumpable {

    override fun requestLogin() = false

    override fun jump(context: Context, params: Map<String, String>) {
        val popType = params[JUMP_PARAM_POP_TYPE]
        if (POP_PARAM_MEMBER_TIPS == popType) {
            
            val memberExpiringTimeConfirmed = MemberExpiringTipsTask.isMemberExpiringTimeConfirmed()

            Timber.d("memberExpiringTimeConfirmed $memberExpiringTimeConfirmed")
            if (!memberExpiringTimeConfirmed) {
                MemberExpiringTipsTask.setMemberExpiringTimeConfirmed()
                AppContext.appRouter().build(RouterPath.Main.PATH)
                        .withInt(RouterPath.PAGE_KEY, RouterPath.Main.PAGE_HOME)
                        .withInt(RouterPath.Main.ACTION_KEY, RouterPath.Main.ACTION_MEMBER_EXPIRING_TIPS)
                        .navigation()
                return
            }
        }

        AppContext.appRouter().build(RouterPath.Main.PATH).withInt(RouterPath.PAGE_KEY, RouterPath.Main.PAGE_HOME).navigation()
    }

}

/** 首页日记 */
private class JumpToHomeDiaryPage : Jumpable {

    override fun requestLogin() = true

    override fun jump(context: Context, params: Map<String, String>) {
        AppContext.appRouter().build(RouterPath.Diary.PATH).withInt(RouterPath.PAGE_KEY,RouterPath.Diary.PAGE_LIST).navigation()
    }

}

/** 首页我的 */
private class JumpToHomeMinePage : Jumpable {

    override fun requestLogin() = true

    override fun jump(context: Context, params: Map<String, String>) {
        AppContext.appRouter().build(RouterPath.Main.PATH)
                .withInt(RouterPath.PAGE_KEY, RouterPath.Main.PAGE_MINE)
                .navigation()
    }

}

/** 应用推荐 */
private class JumpToAppRecommendationPage : Jumpable {

    override fun requestLogin() = true

    override fun jump(context: Context, params: Map<String, String>) {
        params["recGroupId"]?.let {
            AppContext.appRouter().build(RouterPath.Recommend.PATH)
                    .withString(RouterPath.Recommend.RECOMMEND_ID_KEY, it)
                    .withFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    .navigation()
        }
    }

}

/** 购买会员 */
private class JumpToPurchaseMemberPage : Jumpable {

    override fun requestLogin() = true

    override fun jump(context: Context, params: Map<String, String>) {
        AppContext.appRouter().build(RouterPath.MemberCenter.PATH)
                .withInt(RouterPath.PAGE_KEY, RouterPath.MemberCenter.CENTER)
                .withFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                .navigation()
    }

}

/** 会员中心*/
private class JumpToMemberCenterPage : Jumpable {

    override fun requestLogin() = true

    override fun jump(context: Context, params: Map<String, String>) {
        AppContext.appRouter().build(RouterPath.MemberCenter.PATH)
                .withInt(RouterPath.PAGE_KEY, RouterPath.MemberCenter.CENTER)
                .withFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                .navigation()
    }

}