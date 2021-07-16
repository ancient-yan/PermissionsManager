package com.gwchina.sdk.base.data;

import android.app.Application;

import com.android.sdk.net.NetContext;
import com.app.base.BuildConfig;
import com.blankj.utilcode.util.NetworkUtils;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.data.api.ApiHelper;
import com.gwchina.sdk.base.data.api.ApiParameter;
import com.gwchina.sdk.base.data.app.AppDataSource;

import timber.log.Timber;

import static com.gwchina.sdk.base.data.URLProviderKt.addAllHost;
import static com.gwchina.sdk.base.data.URLProviderKt.addReleaseHost;
import static com.gwchina.sdk.base.data.URLProviderKt.getBaseUrl;
import static com.gwchina.sdk.base.data.URLProviderKt.getBaseWebUrl;
import static com.gwchina.sdk.base.data.URLProviderKt.getSaleBaseUrl;

/**
 * Data层配置，抽象为DataContext
 *
 * @author Ztiany
 * Email: 1169654504@qq.com
 * Date : 2017-04-12 11:16
 */
public class DataContext {

    private static DataContext sDataContext;

    public synchronized static void init(Application application) {
        if (sDataContext != null) {
            throw new IllegalStateException("DataContext was  initialized");
        }
        sDataContext = new DataContext(application);
    }

    public static DataContext getInstance() {
        if (sDataContext == null) {
            throw new IllegalStateException("DataContext has not initialized");
        }
        return sDataContext;
    }

    private AppDataSource mAppDataSource;

    @SuppressWarnings("unused")
    private DataContext(Application application) {
        //环境初始化
        initEnvironment();
        //初始化参数
        ApiParameter.init();
        //初始化网络库
        NetProviderImpl netProvider = new NetProviderImpl();
        NetContext.get()
                .newBuilder()
                .aipHandler(netProvider.mApiHandler)
                .httpConfig(netProvider.mHttpConfig)
                .networkChecker(NetworkUtils::isConnected)
                .postTransformer(netProvider.mPostTransformer)
                .errorDataAdapter(netProvider.mErrorDataAdapter)
                .errorMessage(netProvider.mErrorMessage)
                .exceptionFactory(netProvider.mExceptionFactory)
                .setup();
    }

    void publishLoginExpired(int code) {
        AppContext.errorHandler().handleGlobalError(code);
        Timber.d("用户登录过期：" + ((ApiHelper.isLoginExpired(code)) ? "token 过期。" : "其他设备登录。"));
    }

    private void initEnvironment() {
        if (BuildConfig.openDebug) {
            addAllHost();
        } else {
            addReleaseHost();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // public
    ///////////////////////////////////////////////////////////////////////////
    public void onAppDataSourcePrepared(AppDataSource appDataSource) {
        if (mAppDataSource != null) {
            throw new IllegalStateException("DataContext.onAppDataSourcePrepared already called");
        }
        mAppDataSource = appDataSource;
    }

    public String getAppToken() {
        if (mAppDataSource != null) {
            return mAppDataSource.appToken();
        }
        return "";
    }

    public String getDeviceId() {
        if (mAppDataSource != null) {
            return mAppDataSource.deviceId();
        }
        return "";
    }

    public String baseWebUrl() {
        return getBaseWebUrl();
    }

    static String baseUrl() {
        return getBaseUrl();
    }

    public static String saleBaseUrl(){
        return getSaleBaseUrl();
    }

}