package com.gwchina.sdk.base.data.api;

import android.support.annotation.NonNull;

public class ApiHelper {

    /*Json解析错误*/
    private static final int DATA_ERROR = -8088;
    /*返回成功*/
    private static final int CODE_SUCCESS = 0;
    /*文件待上传*/
    public static final int CODE_PENDING_UPLOAD = -2;
    /*登录状态已过期，请重新登录*/
    private static final int LOGIN_EXPIRED = 1;
    /*账号在其他设备登陆*/
    private static final int LOGIN_EXPIRED_SSO = 11;
    /*通用异常，后续接口异常区分返回码*/
    private static final int COMMON_ERROR = -1;
    /*	未知异常*/
    private static final int UNKNOW_INVALID = -999;

    /**
     * 请求是否成功了
     */
    static boolean isSuccess(@NonNull HttpResult<?> httpResult) {
        return httpResult.getStatus() == CODE_SUCCESS;
    }

    /**
     * 登录状态已过期，请重新登录
     */
    public static boolean isLoginExpired(int code) {
        return code == LOGIN_EXPIRED;
    }

    /**
     * 登录过期，账号在其他设备登陆
     */
    public static boolean isSSOLoginExpired(int code) {
        return code == LOGIN_EXPIRED_SSO;
    }

    public static boolean isDataError(Object data) {
        if (data instanceof HttpResult) {
            return ((HttpResult) data).getStatus() == DATA_ERROR;
        }
        return false;
    }

    public static Object newErrorDataStub() {
        return new HttpResult<>(DATA_ERROR);
    }

}
