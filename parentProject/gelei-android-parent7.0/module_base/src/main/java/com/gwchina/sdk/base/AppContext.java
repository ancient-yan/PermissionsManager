package com.gwchina.sdk.base;

import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;

import com.android.base.app.BaseAppContext;
import com.android.base.app.BaseKit;
import com.android.base.permission.PermissionUIProviderFactory;
import com.android.base.rx.SchedulerProvider;
import com.android.sdk.net.exception.NetworkErrorException;
import com.android.sdk.net.exception.ServerErrorException;
import com.android.sdk.net.service.ServiceFactory;
import com.android.sdk.social.qq.QQManager;
import com.android.sdk.social.wechat.WeChatManager;
import com.app.base.R;
import com.bun.miitmdid.core.JLibrary;
import com.gwchina.sdk.base.app.AppSecurity;
import com.gwchina.sdk.base.app.ErrorHandler;
import com.gwchina.sdk.base.config.AppSettings;
import com.gwchina.sdk.base.config.GlobalConstants;
import com.gwchina.sdk.base.data.DataContext;
import com.gwchina.sdk.base.data.app.AppDataSource;
import com.gwchina.sdk.base.data.app.StorageManager;
import com.gwchina.sdk.base.data.services.ServiceManager;
import com.gwchina.sdk.base.debug.DebugTools;
import com.gwchina.sdk.base.router.AppRouter;
import com.gwchina.sdk.base.router.RouterManager;
import com.gwchina.sdk.base.third.bugly.TinkerTools;
import com.gwchina.sdk.base.third.push.PushManager;
import com.gwchina.sdk.base.third.umeng.StatisticalManager;
import com.gwchina.sdk.base.widget.dialog.AppLoadingView;
import com.gwchina.sdk.base.widget.dialog.PermissionDialogProvider;
import com.gwchina.sdk.base.widget.dialog.TipsManager;

import java.io.IOException;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.support.HasSupportFragmentInjector;
import okhttp3.OkHttpClient;
import retrofit2.HttpException;
import timber.log.Timber;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-01 10:20
 */
public abstract class AppContext extends BaseAppContext implements HasSupportFragmentInjector, HasActivityInjector {

    private static AppContext sAppContext;

    @Inject DispatchingAndroidInjector<Activity> dispatchingActivityInjector;
    @Inject DispatchingAndroidInjector<Fragment> dispatchingFragmentInjector;

    @Inject AppDataSource mAppDataSource;
    @Inject ServiceManager mServiceManager;
    @Inject ErrorHandler mErrorHandler;
    @Inject ServiceFactory mServiceFactory;
    @Inject AppRouter mAppRouter;
    @Inject StorageManager mStorageManager;
    @Inject SchedulerProvider mSchedulerProvider;
    @Inject OkHttpClient mOkHttpClient;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
        TinkerTools.installTinker();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;
        initApp();
    }

    //todo, initialization in multi thread.
    private void initApp() {
        //Bugly
        TinkerTools.initBuglyTinker(this);
        //安全层
        AppSecurity.init();
        //基础配置
        AppSettings.init(this);
        //路由
        RouterManager.init(this);
        //数据层
        DataContext.init(this);
        //调试
        DebugTools.init(this);

        //完成全局对象注入
        injectAppContext();

        //基础库配置
        BaseKit.get()
                .enableAutoInject()
                .setDefaultFragmentContainerId(R.id.common_container_id)//默认的Fragment容器id
                .registerLoadingFactory(AppLoadingView::new)//默认的通用的LoadingDialog和Toast实现
                .setDefaultPageStart(GlobalConstants.DEFAULT_PAGE_START)//分页开始页码
                .setDefaultPageSize(GlobalConstants.DEFAULT_PAGE_SIZE)//默认分页大小
                .setErrorClassifier(new BaseKit.ErrorClassifier() {
                    @Override
                    public boolean isNetworkError(Throwable throwable) {
                        Timber.d("isNetworkError " + throwable);
                        return throwable instanceof NetworkErrorException || throwable instanceof IOException;
                    }

                    @Override
                    public boolean isServerError(Throwable throwable) {
                        Timber.d("isServerError " + throwable);
                        return throwable instanceof ServerErrorException || (throwable instanceof HttpException && ((HttpException) throwable).code() >= 500);
                    }
                });
        PermissionUIProviderFactory.registerPermissionUIProvider(new PermissionDialogProvider());

        //给数据层设置全局数据源
        DataContext.getInstance().onAppDataSourcePrepared(appDataSource());
        //消息推送初始化
        PushManager.getInstance().init(this);
        //友盟数据统计初始化
        StatisticalManager.init(this);
        //微信SDK配置
        WeChatManager.initWeChatSDK(this, /*"wx04452aa7ecf98fb4"*/"wx32487a5d59ee1539", "");
        QQManager.initQQSDK("1101491530");
        try {
            JLibrary.InitEntry(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingFragmentInjector;
    }

    public static AppContext getContext() {
        return sAppContext;
    }

    public static StorageManager storageManager() {
        return getContext().mStorageManager;
    }

    public static ErrorHandler errorHandler() {
        return getContext().mErrorHandler;
    }

    public static ServiceFactory serviceFactory() {
        return getContext().mServiceFactory;
    }

    public static AppDataSource appDataSource() {
        return getContext().mAppDataSource;
    }

    public static AppRouter appRouter() {
        return getContext().mAppRouter;
    }

    public static SchedulerProvider schedulerProvider() {
        return getContext().mSchedulerProvider;
    }

    public static OkHttpClient httpClient() {
        return getContext().mOkHttpClient;
    }

    public static ServiceManager serviceManager() {
        return getContext().mServiceManager;
    }

    protected abstract void injectAppContext();

    public void restartApp(Activity activity) {
        TipsManager.showMessage("no implementation");
    }

}
