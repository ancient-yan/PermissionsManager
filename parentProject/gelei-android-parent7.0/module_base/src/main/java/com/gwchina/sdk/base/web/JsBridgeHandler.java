package com.gwchina.sdk.base.web;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JsPromptResult;

import com.android.base.utils.android.compat.AndroidVersion;
import com.gwchina.sdk.base.web.actions.InternalJsAction;

import java.util.Arrays;

import timber.log.Timber;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-01-24 14:30
 */
public class JsBridgeHandler {

    private static final String JS_DIVIDER = "#a#a#a#a#";
    private static final String JS_FLAG = "callAndroid";

    private static final String TAG = JsBridgeHandler.class.getSimpleName();

    private final BaseWebFragment mFragment;
    private JsCallInterceptor mJsCallInterceptor;

    JsBridgeHandler(BaseWebFragment fragment) {
        mFragment = fragment;
    }

    boolean handleJsCall(String message, JsPromptResult jsPromptResult) {

        if (TextUtils.isEmpty(message)) {
            return false;
        }

        if (!message.startsWith(JS_FLAG)) {
            return false;
        }

        String[] split = message.split(JS_DIVIDER);

        Log.d(TAG, "split:" + Arrays.toString(split));

        if (split.length <= 1) {
            return false;
        }

        String method = split[1];
        String[] args = split.length == 2 ? null : Arrays.copyOfRange(split, 2, split.length);
        ResultReceiver resultReceiver = jsPromptResult == null ? null : jsPromptResult::confirm;

        Timber.d("handleJsCall, thread in " + Thread.currentThread());
        dispatchJsMethodCall(method, args, resultReceiver);
        return true;
    }

    private void dispatchJsMethodCall(String method, @Nullable String[] args, ResultReceiver resultReceiver) {
        Timber.d("dispatchJsMethodCall() called with: method = [" + method + "], args = [" + Arrays.toString(args) + "]");
        if (mJsCallInterceptor == null || !mJsCallInterceptor.intercept(method, args, resultReceiver)) {
            innerProcess(method, args, resultReceiver);
        }
    }

    @SuppressWarnings("unused")
    public void setJsCallInterceptor(JsCallInterceptor jsCallInterceptor) {
        mJsCallInterceptor = jsCallInterceptor;
    }

    private void innerProcess(String method, @Nullable String[] args, @Nullable ResultReceiver resultReceiver) {
        InternalJsAction.doAction(mFragment, method, args, resultReceiver);
    }

    public void callJs(String method, String... params) {
        String script = WebUtils.buildJavascript(method, params);
        Timber.d("javascript: %s", script);
        if (AndroidVersion.atLeast(19)) {
            mFragment.mWebView.evaluateJavascript(script, value -> {
            });
        } else {
            mFragment.mWebView.loadUrl(script);
        }
    }

}
