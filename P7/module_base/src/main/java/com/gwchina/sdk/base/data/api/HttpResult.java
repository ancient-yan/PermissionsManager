package com.gwchina.sdk.base.data.api;

import android.support.annotation.NonNull;

import com.android.sdk.net.core.Result;

@SuppressWarnings("unused")
public class HttpResult<T> implements Result<T> {

    public HttpResult() {
    }

    HttpResult(int status) {
        this.status = status;
    }

    private int status;
    private String message;
    private T data;

    public T getData() {
        return data;
    }

    @Override
    public int getCode() {
        return getStatus();
    }

    @SuppressWarnings("all")
    public int getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean isSuccess() {
        return ApiHelper.isSuccess(this);
    }

    public boolean hasData() {
        return data != null;
    }

    @NonNull
    @Override
    public String toString() {
        return "HttpResult{" +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

}
