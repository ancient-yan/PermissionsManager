package com.gwchina.sdk.base.web;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.URLUtil;
import android.webkit.WebBackForwardList;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.base.app.fragment.BaseFragment;
import com.android.base.app.fragment.Fragments;
import com.android.base.utils.android.WebViewUtils;
import com.android.sdk.social.qq.QQManager;
import com.app.base.R;
import com.blankj.utilcode.util.NetworkUtils;
import com.gwchina.sdk.base.router.RouterPath;
import com.gwchina.sdk.base.web.actions.SaveImageAction;
import com.gwchina.sdk.base.widget.GwTitleLayout;
import com.gwchina.sdk.base.widget.dialog.TipsManager;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import timber.log.Timber;

/**
 * todo 拆分非公共逻辑
 *
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-09-04 17:02
 */
public class BaseWebFragment extends BaseFragment {

    protected View mLayout;
    public WebView mWebView;
    protected GwTitleLayout mGwTitleLayout;
    private View mInsetsView;
    private View mErrorLayout;
    private WebProgress mWebProgress;

    protected String mCurrentUrl;

    private AppWebChromeClient mWebChromeClient;
    private JsBridgeHandler mJsBridgeHandler;

    private boolean mTitleIsHidden = false;
    private String customTitle = null;
    private boolean mNeedRefresh = false;

    private QQManager qqManager;

    private static final String TITLE_IS_HIDDEN_KEY = "TITLE_IS_HIDDEN_KEY";

    @Override
    public void onAttach(Context context) {
        mJsBridgeHandler = new JsBridgeHandler(this);
        initArguments();
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mTitleIsHidden = savedInstanceState.getBoolean(TITLE_IS_HIDDEN_KEY, false);
        } else {
            Bundle arguments = getArguments();
            if (arguments != null) {
                mTitleIsHidden = !arguments.getBoolean(RouterPath.Browser.SHOW_HEADER_KEY, false);
            }
        }
        qqManager = new QQManager(requireActivity());
    }

    @Nullable
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mLayout == null) {
            mLayout = inflater.inflate(R.layout.app_base_web_fragment, container, false);
            setupViews();
        }
        return mLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String url = arguments.getString(RouterPath.Browser.URL_KEY);
            if (!TextUtils.isEmpty(url)) {
                startLoad(url);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mWebView.resumeTimers();
        mWebView.onResume();
        if (mNeedRefresh) {
            mNeedRefresh = false;
            mWebView.postDelayed(() -> mWebView.reload(), 100);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mWebView.onPause();
        mWebView.pauseTimers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WebViewUtils.destroy(mWebView);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void initArguments() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            customTitle = arguments.getString(RouterPath.Browser.WEB_TITLE, "");
            initCustomJsCallInterceptorIfNeed(arguments);
        }
    }

    private void initCustomJsCallInterceptorIfNeed(Bundle arguments) {
        String className = arguments.getString(RouterPath.Browser.JS_CALL_INTERCEPTOR_CLASS_KEY, "");
        Timber.d("initCustomJsCallInterceptorIfNeed = " + className);
        //防止h5未提前沟通加了需要调安卓分享的功能
        if (TextUtils.isEmpty(className)) {
            className = "com.gwchina.parent.main.presentation.mine.ShareJsCallInterceptor";
//            return;
        }
        try {
            Object newInstance = Class.forName(className).newInstance();
            if (newInstance instanceof JsCallInterceptor) {
                JsCallInterceptor jsCallInterceptor = (JsCallInterceptor) newInstance;
                mJsBridgeHandler.setJsCallInterceptor(jsCallInterceptor);
                if (newInstance instanceof BaseCustomJsCallInterceptor) {
                    ((BaseCustomJsCallInterceptor) newInstance).onInit(this, arguments.getBundle(RouterPath.Browser.ARGUMENTS_KEY));
                }
            }
        } catch (Exception error) {
            Timber.e("initCustomJsCallInterceptorIfNeed error", error);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(RouterPath.Browser.SHOW_HEADER_KEY, mTitleIsHidden);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected boolean handleBackPress() {
        WebBackForwardList webBackForwardList = mWebView.copyBackForwardList();
        int size = webBackForwardList.getSize();
//        for (int i = 0; i < size; i++) {
//            String url = webBackForwardList.getItemAtIndex(i).getUrl();
//            Log.e("url" + i + ":", url);
//        }
        if (size == 3 && webBackForwardList.getItemAtIndex(1).getUrl().contains("silent-login")) {
            return false;
        }
        if (isVisible() && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    public View getView() {
        View view = super.getView();
        assert view != null;
        return view;
    }

    protected void setupViews() {
        /*Find view*/
        mWebView = mLayout.findViewById(R.id.web_view);
        mWebProgress = new WebProgress(mLayout.findViewById(R.id.web_pb));
        mErrorLayout = mLayout.findViewById(R.id.layout_error);
        mGwTitleLayout = mLayout.findViewById(R.id.gtlWebRulesTitle);
        mInsetsView = mLayout.findViewById(R.id.ivWebRulesInsets);

        /*Title*/
        mGwTitleLayout.setOnNavigationOnClickListener(v -> Fragments.exitFragment(this));
        setTitleVisible(!mTitleIsHidden);
        if (!TextUtils.isEmpty(customTitle)) {
            mGwTitleLayout.setTitle(customTitle);
        }

        /*Error layout*/
        mErrorLayout.setBackgroundColor(Color.WHITE);

        /*WebView*/
        DefaultWebSetting defaultWebSetting = new DefaultWebSetting(mWebView);
        defaultWebSetting.setupBasic();
        defaultWebSetting.setupCache();
        Bundle arguments = getArguments();
        if (arguments != null && arguments.getBoolean(RouterPath.Browser.CACHE_ENABLE, false)) {
            defaultWebSetting.setCacheMode(true);
        }
        if (arguments != null && arguments.getBoolean(RouterPath.Browser.LOAD_NO_CACHE_ENABLE, false)) {
            defaultWebSetting.setNoCacheMode();
        }

        mWebChromeClient = new AppWebChromeClient(this);
        mWebChromeClient.setAppWebChromeClientCallback(appWebChromeClientCallback);
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mAppWebViewClient);

        //webview中下载
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                Log.e("tag", "url=" + url);
                Log.e("tag", "userAgent=" + userAgent);
                Log.e("tag", "contentDisposition=" + contentDisposition);
                Log.e("tag", "mimetype=" + mimetype);
                Log.e("tag", "contentLength=" + contentLength);
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        mWebView.addJavascriptInterface(new WebAppInterface(this), "geleiParent");

    }

    static class WebAppInterface {

        private SaveImageAction mSaveImageAction;

        WebAppInterface(BaseWebFragment fragment) {
            mSaveImageAction = new SaveImageAction(fragment, null);
        }

        @JavascriptInterface
        public void saveImageWithBase64(String base64) {
            Timber.d("saveImageWithBase64" + base64.length());
            mSaveImageAction.trySaveImageByDecodingBase64(base64);
        }
    }

    private void setTitleVisible(boolean visible) {
        if (visible) {
            mInsetsView.setVisibility(View.VISIBLE);
            mGwTitleLayout.setVisibility(View.VISIBLE);
        } else {
            mInsetsView.setVisibility(View.GONE);
            mGwTitleLayout.setVisibility(View.GONE);
        }
    }

    private AppWebChromeClient.AppWebChromeClientCallback appWebChromeClientCallback = new AppWebChromeClient.AppWebChromeClientCallback() {
        @Override
        public void onReceivedTitle(String title) {
            processReceivedTitle(title);
        }

        @Override
        public void onProgressChanged(int newProgress) {
            progressProgress(newProgress);
        }
    };


    private WebViewClient mAppWebViewClient = new AppWebViewClient() {

        @Override
        boolean appShouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("http://qm.qq.com/cgi-bin/qm/qr")) {
                if (!qqManager.isQQInstalled(requireContext())) {
                    TipsManager.showMessage("哎呀，您还未安装QQ");
                }
            }
            return processShouldOverrideUrlLoading(view, url);
        }

        @Override
        void onAppPageError(String url, int code) {
            onLoadError(url, code);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            onLoadFinished(url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            onLoadStart(url);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mWebChromeClient.onActivityResult(requestCode, resultCode, data);
    }

    protected void onLoadFinished(@SuppressWarnings("unused") String url) {
        Timber.d("onLoadFinished() called with: url = [" + url + "]");
    }

    protected void progressProgress(int newProgress) {
        mWebProgress.onProgress(newProgress);
    }

    protected void processReceivedTitle(String title) {
        if (TextUtils.isEmpty(customTitle) && autoHandleTitle() && WebUtils.isValidateTitle(title)) {
            mGwTitleLayout.setTitle(title);
        }
    }

    protected void onLoadStart(@SuppressWarnings("unused") String url) {
        Timber.d("onLoadStart() called with: url = [" + url + "]");
        mErrorLayout.setVisibility(View.GONE);
    }

    protected void onLoadError(@SuppressWarnings("unused") String url, int code) {
        Timber.d("onLoadError() called with: url = [" + url + "], code = [" + code + "]");

        mErrorLayout.setVisibility(View.VISIBLE);

        if (!NetworkUtils.isConnected()) {
            mErrorLayout.<ImageView>findViewById(R.id.base_retry_icon).setImageResource(R.drawable.img_page_net_error);
            mErrorLayout.<TextView>findViewById(R.id.base_retry_tv).setText(R.string.error_net_error);
        } else {
            mErrorLayout.<ImageView>findViewById(R.id.base_retry_icon).setImageResource(R.drawable.img_page_server_error);
            mErrorLayout.<TextView>findViewById(R.id.base_retry_tv).setText(R.string.error_service_error);
        }

        boolean titleIsHidden = mTitleIsHidden;
        setTitleVisible(true);

        mErrorLayout.findViewById(R.id.base_retry_btn).setOnClickListener(view1 -> {
            Timber.d("onLoadError() called retry: url = [" + mCurrentUrl + "]");
            if (titleIsHidden) {
                setTitleVisible(false);
            }
            mErrorLayout.setVisibility(View.GONE);
            mWebView.loadUrl(mCurrentUrl);
        });
    }

    public void startLoad(String url) {
        Timber.d("firstLoadUrl() called with: currentUrl = [" + url + "]");
        loadUrl(url);
    }

    @SuppressWarnings("unused")
    public void startLoad(String url, Map<String, String> header) {
        Timber.d("startLoadUrl() called with: url = [" + url + "], header = [" + header + "]");
        mCurrentUrl = url;
        mWebView.loadUrl(mCurrentUrl, header);
    }

    @SuppressWarnings("unused")
    public void startPostLoad(String url, String postData) {
        Timber.d("startLoadUrl() called with: url = [" + url + "], postData = [" + postData + "]");
        mCurrentUrl = url;
        mWebView.postUrl(url, postData.getBytes(StandardCharsets.UTF_8));
    }

    private void loadUrl(String url) {
        mCurrentUrl = url;
        mWebView.loadUrl(mCurrentUrl);
    }

    protected boolean autoHandleTitle() {
        return true;
    }

    protected boolean processShouldOverrideUrlLoading(@SuppressWarnings("unused") WebView webView, String url) {
        if (URLUtil.isNetworkUrl(url)) {
            loadUrl(url);
        } else {
            processAppUrl(url);
        }
        return true;
    }

    protected void processAppUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    boolean handleJsCall(@SuppressWarnings("unused") WebView view, String message, JsPromptResult jsPromptResult) {
        return mJsBridgeHandler.handleJsCall(message, jsPromptResult);
    }

    @SuppressWarnings("unused")
    public final JsBridgeHandler getJsBridgeHandler() {
        return mJsBridgeHandler;
    }

    protected final void refresh() {
        loadUrl(mCurrentUrl);
    }

    public void exit() {
        Fragments.exitFragment(this);
    }

    public final void setHeaderVisible(boolean showHeader) {
        mTitleIsHidden = !showHeader;
        if (mGwTitleLayout != null) {
            if (showHeader) {
                setTitleVisible(true);
            } else {
                setTitleVisible(false);
            }
        }
    }

    public final void showStatusBarVisible(boolean showStatusVisible) {
        mInsetsView.setVisibility(showStatusVisible ? View.VISIBLE : View.GONE);
    }

    public void refreshWhenResume() {
        mNeedRefresh = true;
    }

}