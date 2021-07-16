package com.gwchina.sdk.base.web;

import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import timber.log.Timber;

/**
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-12-20 15:43
 */
abstract class AppWebViewClient extends WebViewClient {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        return appShouldOverrideUrlLoading(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return appShouldOverrideUrlLoading(view, url);
    }

    abstract boolean appShouldOverrideUrlLoading(WebView view, String url);

    //http://www.barryzhang.com/archives/450
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);
        Timber.d("onReceivedError() called with: view = [" + view + "], request = [" + request.isForMainFrame() + ", " + request.getUrl() + "], error = [" + error.getErrorCode() + "]");
        if (request.isForMainFrame()) {
            onAppPageError(request.getUrl().toString(), error.getErrorCode());
        }
    }

    //http://www.barryzhang.com/archives/450
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        Timber.d("onReceivedError() called with: view = [" + view + "], errorCode = [" + errorCode + "], description = [" + description + "], failingUrl = [" + failingUrl + "]");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return;
        }
        onAppPageError(failingUrl, errorCode);
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
        super.onReceivedHttpError(view, request, errorResponse);
        Timber.d("onReceivedHttpError url = %s, isMain = %b, code = %d", view.getUrl(), request.isForMainFrame(), errorResponse.getStatusCode());
        if (request.isForMainFrame()) {
            onAppPageError(view.getUrl(), -1);
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        super.onReceivedSslError(view, handler, error);
        Timber.d("onReceivedSslError, url = ", view.getUrl());
    }

    abstract void onAppPageError(String url, int code);

}
