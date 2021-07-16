package com.gwchina.sdk.base.linker

import android.content.Context
import android.net.Uri
import com.android.base.app.BaseKit
import com.android.base.utils.android.ResourceUtils
import com.android.sdk.social.wechat.WeChatManager
import com.app.base.R
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.utils.JsonUtils
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.widget.dialog.TipsManager
import timber.log.Timber
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible


class SchemeJumper {

    companion object {

        private const val JUMP_APP = "01"
        private const val JUMP_H5 = "02"
        private const val JUMP_WX_MIN_PROGRAM = "03"

        @JvmStatic
        @JvmOverloads
        fun handleSchemeJumpUnParsed(context: Context, json: String, interceptor: JumpInterceptor? = null): Boolean {
            val jumpScheme = JsonUtils.fromJson(json, JumpScheme::class.java) ?: return false
            return handleSchemeJump(context, jumpScheme.jump_target, jumpScheme.jump_args, interceptor)
        }

        @JvmStatic
        @JvmOverloads
        fun handleSchemeJump(context: Context, jumpTarget: String, data: String, interceptor: JumpInterceptor? = null): Boolean {
            if (data.isEmpty() || jumpTarget.isEmpty()) {
                return false
            }
            return when (jumpTarget) {
                JUMP_APP -> jumpToAppPage(context, interceptor, data)
                JUMP_H5 -> jumpToH5(data)
                JUMP_WX_MIN_PROGRAM -> jumpToMinProgram(data)
                else -> false
            }
        }

        @JvmStatic
        fun createJumpable(id: String): Jumpable? {
            return jumperMap[id]?.primaryConstructor?.apply {
                isAccessible = true
            }?.call() as? Jumpable
        }

    }

}

interface JumpInterceptor {

    fun shouldIntercept(urlEntity: UrlEntity): Boolean

}

data class JumpScheme(
        val jump_args: String,
        val jump_target: String
)

/**gwdefender  代表url schemes 固定不变*/
private const val JUMP_SCHEME = "gwdefender"
/**jump 代表需要跳转页面*/
private const val JUMP_HOST = "jump"

/** 跳转H5页面*/
private fun jumpToH5(jumpArgs: String): Boolean {
    if (jumpArgs.isEmpty() || (!jumpArgs.startsWith("https://") && !jumpArgs.startsWith("http://"))) {
        Timber.e("H5跳转参数错误")
        return false
    }
    //com.gwchina.parent.main.presentation.mine.ShareJsCallInterceptor
    if (jumpArgs.contains(RouterPath.Browser.INVITATION_FRIENDS)) { //分享
        val clazz = Class.forName("com.gwchina.parent.main.presentation.mine.ShareJsCallInterceptor")
        AppContext.appRouter().build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, jumpArgs)
                .withString(RouterPath.Browser.JS_CALL_INTERCEPTOR_CLASS_KEY, clazz.name)
                .withBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false)
                .navigation()
    } else {
        AppContext.appRouter()
                .build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, jumpArgs)
                .navigation()
    }


    return true
}

/** 跳转微信小程序 */
private fun jumpToMinProgram(jumpArgs: String): Boolean {
    if (jumpArgs.isEmpty()) {
        return false
    }
    // 格式：path/to?app_id=1122&param=xxx
    val urlEntity = UrlEntity.from(jumpArgs) ?: return false

    var appId: String? = null
    if (urlEntity.params.containsKey("app_id")) {
        appId = urlEntity.params.getValue("app_id")
    }
    if (appId.isNullOrEmpty()) {
        Timber.e("小程序跳转参数错误:没有原始id")
        return false
    }
    if (!WeChatManager.getInstance().isInstalledWeChat) {
        TipsManager.showMessage(ResourceUtils.getText(R.string.uninstall_wechat))
        return false
    }
    WeChatManager.getInstance().navToMinProgram(appId, urlEntity.baseUrl, true)
    return true
}

/** 跳转app内部页面 */
private fun jumpToAppPage(context: Context, interceptor: JumpInterceptor?, jumpArgs: String): Boolean {
    if (jumpArgs.isEmpty()) {
        return false
    }

    // 格式：gwdefender://jump?ID=1101
    val uri = Uri.parse(jumpArgs)
    if (uri == null || JUMP_SCHEME != uri.scheme || JUMP_HOST != uri.host) {
        Timber.e("app跳转参数错误")
        return false
    }

    val urlEntity = UrlEntity.from(jumpArgs) ?: return false

    //是否拦截跳转
    if (interceptor != null && interceptor.shouldIntercept(urlEntity)) {
        return true
    }

    val id = urlEntity.id ?: return false

    val jumpable = SchemeJumper.createJumpable(id) ?: return false

    if (jumpable.requestLogin() && !AppContext.appDataSource().userLogined()) {
        val topActivity = BaseKit.get().topActivity
        AppContext.appRouter().build(RouterPath.Account.PATH).navigation(topActivity, RouterPath.Account.REQUEST_CODE)
    } else {
        jumpable.jump(context, urlEntity.params)
    }

    return true
}
