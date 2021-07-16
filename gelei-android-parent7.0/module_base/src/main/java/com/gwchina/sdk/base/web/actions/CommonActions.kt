package com.gwchina.sdk.base.web.actions

import android.support.v4.app.Fragment
import android.util.Log
import com.android.base.utils.BaseUtils
import com.android.base.utils.android.compat.SystemBarCompat
import com.blankj.utilcode.util.AppUtils
import com.gwchina.sdk.base.AppContext
import com.gwchina.sdk.base.data.api.ApiParameter
import com.gwchina.sdk.base.data.models.Child
import com.gwchina.sdk.base.data.models.Device
import com.gwchina.sdk.base.data.models.logined
import com.gwchina.sdk.base.data.utils.JsonUtils
import com.gwchina.sdk.base.linker.SchemeJumper
import com.gwchina.sdk.base.router.RouterPath
import com.gwchina.sdk.base.web.BaseWebFragment
import com.gwchina.sdk.base.web.ResultReceiver
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.*

private const val JSCALL_SKIP_DIRTY_PAGE_METHOD = "skipDirtyPage"//跳到成长日记写日记1页面
const val JSCALL_SKIP_LOGIN_PAGE = "skipLoginPage"//登录
private const val JSCALL_SKIP_COMPLAINT_PAGE_METHOD = "skipDissPage"//吐槽一下页面

private const val JSCALL_SAVE_TO_NATIVE_METHOD = "saveToNative"//保存信息
private const val JSCALL_GET_FROM_NATIVE_METHOD = "getFromNative"//移除信息

private const val JSCALL_GO_BACK_METHOD = "goback"//退出web
private const val JSCALL_SHOW_HEADER_METHOD = "showHeader"//展示和隐藏头部

private const val JSCALL_SIGN_METHOD = "sign"//签名加密
const val JSCALL_GET_PUBLIC_PARAMS = "getPublicParams"//获取通用信息
const val JSCALL_GET_CHILD_INFORMATION = "getChildInformation"//获取(默认)孩子信息
const val JSCALL_GET_ABOUT_PAGE_PARAMS = "getAboutPageParams"//获取我的界面信息
const val JSCALL_SKIP_NEW_PAGE = "skipNewPage"//获取我的界面信息
const val JSCALL_WEBVIEW_BACKLIST = "getBackList" //获取webview回退栈信息
const val JSCALL_GOBACK_PAGE = "goBackItemAtIndex"//index:回退webview到上几个页面 url：重新加载的
const val JSCALL_SHOW_STATUS_BAR="showStatusBar"//显示

internal fun doAction(method: String, args: Array<String>?, resultReceiver: ResultReceiver?, fragment: BaseWebFragment) {
    when (method) {
        JSCALL_GO_BACK_METHOD -> fragment.exit()
        JSCALL_SKIP_LOGIN_PAGE -> jumpToLogin()
        JSCALL_SKIP_DIRTY_PAGE_METHOD -> jumpToDiary(fragment)
        JSCALL_SHOW_HEADER_METHOD -> showHeader(args, fragment)
        JSCALL_SAVE_TO_NATIVE_METHOD -> saveToNative(args)
        JSCALL_GET_FROM_NATIVE_METHOD -> getFromNative(args, resultReceiver)
        JSCALL_SIGN_METHOD -> sign(args, resultReceiver)
        JSCALL_GET_CHILD_INFORMATION -> getChildInformation(resultReceiver)
        JSCALL_GET_PUBLIC_PARAMS -> getPublicParams(fragment, resultReceiver)
        JSCALL_GET_ABOUT_PAGE_PARAMS -> getAboutPageParams(resultReceiver)
        JSCALL_SKIP_COMPLAINT_PAGE_METHOD -> toTucaoPage(args)
        JSCALL_SKIP_NEW_PAGE -> skipNewPage(args, fragment)
        JSCALL_WEBVIEW_BACKLIST -> getBackList(resultReceiver, fragment)
        JSCALL_GOBACK_PAGE -> goBackPage(args, fragment)
        JSCALL_SHOW_STATUS_BAR->showStatusBar(args,fragment)
    }
}

/**
 * 获取webview回退栈信息
 */
fun getBackList(resultReceiver: ResultReceiver?, fragment: BaseWebFragment) {
    val webBackForwardList = fragment.mWebView.copyBackForwardList()
    val size = webBackForwardList.size
    if (size > 0) {
        val jsonArray = JSONArray()
        for (i in 0 until size) {
            val jsonObject = JSONObject().apply {
                this.put("index", i)
                this.put("url", webBackForwardList.getItemAtIndex(i).url)
            }
            jsonArray.put(jsonObject)
        }
        resultReceiver?.result(jsonArray.toString())
    } else {
        resultReceiver?.result("[]")
    }
}

/**webview回退到第几个页面*/
fun goBackPage(args: Array<String>?, mBaseWebFragment: BaseWebFragment) {
    Timber.d(args?.toString())
    if (args.isNullOrEmpty()) return
    //{"index":1,"url":"xxxx"}
    val json = args[0]
    val jsonObject = JSONObject(json)
    val step = jsonObject.optInt("index")
    if (step == -1) {
        mBaseWebFragment.exit()
        return
    }
    val webBackForwardList = mBaseWebFragment.mWebView.copyBackForwardList()
    val size = webBackForwardList.size
    //判断是否可以前进或后退指定的次数(负数表示回退N次，正数表示前行N次) goBackOrForward(int steps)
    if (!mBaseWebFragment.mWebView.canGoBackOrForward(step - (size - 1))) return
    mBaseWebFragment.mWebView.goBackOrForward(step - (size - 1))
    if (json.contains("url")) {
        val url = jsonObject.optString("url")
        if (url.isEmpty()) return
        mBaseWebFragment.mWebView.loadUrl(url)
    }
}

fun skipNewPage(args: Array<String>?, fragment: BaseWebFragment) {
    if (args.isNullOrEmpty()) {
        return
    }
    SchemeJumper.handleSchemeJumpUnParsed(fragment.requireContext(), args[0])
}

fun getAboutPageParams(resultReceiver: ResultReceiver?) {
    val jsonObject = JSONObject()

    jsonObject.put("device_id", AppContext.appDataSource().deviceId())
    jsonObject.put("version", AppUtils.getAppVersionName())
    jsonObject.put("platform", "11")    //11 is Android Platform Code

    resultReceiver?.result(jsonObject.toString())
}

fun getPublicParams(fragment: Fragment, resultReceiver: ResultReceiver?) {
    val user = AppContext.appDataSource().user()

    val jsonObject = JSONObject()

    jsonObject.put("app_token", AppContext.appDataSource().appToken())
    jsonObject.put("expire_time", user.member_info?.end_time?.toString() ?: "")
    jsonObject.put("user_id", user.patriarch.user_id)
    jsonObject.put("nick_name", user.patriarch.nick_name)
    jsonObject.put("status_height", (SystemBarCompat.getStatusBarHeight(fragment.requireContext()) / BaseUtils.getResources().displayMetrics.density).toInt().toString())
    jsonObject.put("action_bar_height", (SystemBarCompat.getActionBarHeight(fragment.requireActivity()) / BaseUtils.getResources().displayMetrics.density).toInt().toString())

    val info = jsonObject.toString()

    Timber.d("getPublicParams: $info")

    resultReceiver?.result(info)
}

private fun getFromNative(args: Array<String>?, resultReceiver: ResultReceiver?) {
    Timber.d("js call getFromNative, with ${Arrays.toString(args)}")
    if (args == null || args.isEmpty()) {
        resultReceiver?.result("")
    } else {
        val result = AppContext.storageManager().stableStorage().getString(args[0], "")
        resultReceiver?.result(result)
    }
}

private fun saveToNative(args: Array<String>?) {
    Timber.d("js call saveToNative, with $args")
    if (args != null && args.size >= 2) {
        AppContext.storageManager().stableStorage().putString(args[0], args[1])
    }
}

private data class ChildInformation(
        val child_user_id: String,
        val child_device_id: String?,
        val head_photo_path: String?,
        val nick_name: String,
        val sex: String,
        val grade: Int,
        val age: Int,
        val birthdate: String,
        val status: Int,
        val p_relationship_name: String = "",
        val p_relationship_code: Int = 0,
        val device_list: List<Device>?
)

fun jscallReturnQueryChildInfo(child: Child?, resultReceiver: ResultReceiver?, defaultDeviceId: String? = null) {
    val info = if (child != null) {
        val childInformation = ChildInformation(
                child.child_user_id,
                if (defaultDeviceId.isNullOrEmpty()) child.device_list?.firstOrNull()?.device_id else defaultDeviceId,
                child.head_photo_path,
                child.nick_name ?: "",
                child.sex.toString(),
                child.grade,
                child.age,
                child.birthday ?: "",
                child.status,
                child.p_relationship_name ?: "",
                child.p_relationship_code,
                child.device_list
        )
        JsonUtils.toJson(childInformation)
    } else {
        "{}"
    }
    Timber.d("jscallReturnQueryChildInfo: $info")
    resultReceiver?.result(info)
}

private fun getChildInformation(resultReceiver: ResultReceiver?) {
    val user = AppContext.appDataSource().user()
    if (user.logined()) {
        jscallReturnQueryChildInfo(user.currentChild, resultReceiver)
    } else {
        resultReceiver?.result("{}")
    }
}

private fun jumpToDiary(fragment: BaseWebFragment) {
    fragment.refreshWhenResume()
    AppContext.appRouter().build(RouterPath.Diary.PATH)
            .withInt(RouterPath.PAGE_KEY, RouterPath.Diary.PAGE_PUBLISH)
            .navigation()
}

fun jumpToLogin() {
    AppContext.appRouter().build(RouterPath.Account.PATH).navigation()
}

private fun showHeader(args: Array<String>?, fragment: BaseWebFragment) {
    if (args.isNullOrEmpty()) {
        return
    }

    val showHeader = try {
        args[0].toBoolean()
    } catch (e: Exception) {
        false
    }

    fragment.setHeaderVisible(showHeader)
}

private fun showStatusBar(args: Array<String>?, fragment: BaseWebFragment) {
    if (args.isNullOrEmpty()) {
        return
    }

    val showStatusBar = try {
        args[0].toBoolean()
    } catch (e: Exception) {
        false
    }

    fragment.showStatusBarVisible(showStatusBar)
}

private fun sign(args: Array<String>?, resultReceiver: ResultReceiver?) {
    try {
        val sign = if (args.isNullOrEmpty()) {
            ApiParameter.genSignAndGenerateRequestParams("", false, false, false)
        } else {
            ApiParameter.genSignAndGenerateRequestParams(args[0], args[1].toBoolean(), args[2].toBoolean(), args[3].toBoolean())
        }
        Timber.d("js call Sign, return $sign")
        resultReceiver?.result(sign)
    } catch (e: Exception) {
        Timber.e(e, "js call Sign error")
        resultReceiver?.result("arguments error")
    }
}

private fun toTucaoPage(args: Array<String>?) {
    val user = AppContext.appDataSource().user()
    val productId = args?.firstOrNull() ?: ""

    if (user.logined()) {
        val head = "https://tucao.qq.com/static/desktop/img/products/def-product-logo.png"
        val nickname = user.patriarch.nick_name
        val id = user.patriarch.user_id

        AppContext.appRouter()
                .build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, "https://support.qq.com/product/${productId}?nickname=$nickname&avatar=$head&openid=$id")
                .navigation()

    } else {

        AppContext.appRouter()
                .build(RouterPath.Browser.PATH)
                .withString(RouterPath.Browser.URL_KEY, "https://support.qq.com/product/${productId}")
                .navigation()

    }
}
