package com.gwchina.sdk.base.third.umeng;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.android.base.interfaces.adapter.ActivityLifecycleCallbacksAdapter;
import com.android.base.rx.RxKit;
import com.android.sdk.cache.Storage;
import com.app.base.BuildConfig;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.data.models.UserEx;
import com.gwchina.sdk.base.third.channel.ChannelUtils;
//import com.umeng.analytics.MobclickAgent;
//import com.umeng.commonsdk.UMConfigure;

import io.reactivex.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2019-02-25 12:39
 */
public class StatisticalManager {

    private static final String USER_IS_LOGIN = "user_is_login";
    private static AppContext sAppContext;

    /*前格雷守护的账号*/

    private static final String APP_KEY_DEV = "5c6bba76f1f556858300059f";
    //    private static final String APP_KEY_RELEASE = "5bdc099eb465f5b8d0000385";
    private static final String APP_KEY_RELEASE = "5d6e16474ca35711520002c3";

    public static void init(AppContext appContext) {
        sAppContext = appContext;
//        UMConfigure.setLogEnabled(BuildConfig.openDebug);
//        UMConfigure.init(appContext, BuildConfig.openDebug ? APP_KEY_DEV : APP_KEY_RELEASE, ChannelUtils.getChannel(), UMConfigure.DEVICE_TYPE_PHONE, null);
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.LEGACY_MANUAL);
        initStatisticsLogic(appContext);
        initUserStatusStatistics();
    }

    private static void initUserStatusStatistics() {
        Storage stableStorage = AppContext.storageManager().stableStorage();
        AppContext.appDataSource()
                .observableUser()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> {
                    if (stableStorage.getBoolean(USER_IS_LOGIN, false)) {
                        if (!UserEx.logined(user)) {
//                            MobclickAgent.onProfileSignOff();
                        }
                    } else {
                        if (UserEx.logined(user)) {
//                            MobclickAgent.onProfileSignIn(user.getPatriarch().getUser_id());
                        }
                    }
                    stableStorage.putBoolean(USER_IS_LOGIN, UserEx.logined(user));
                }, RxKit.logErrorHandler());
    }

    private static void initStatisticsLogic(AppContext appContext) {
        appContext.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacksAdapter() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                if (activity instanceof FragmentActivity) {
                    ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
                        @Override
                        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
//                            MobclickAgent.onPageStart(f.getClass().getName());
                        }

                        @Override
                        public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
//                            MobclickAgent.onPageEnd(f.getClass().getName());
                        }
                    }, true);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
//                MobclickAgent.onResume(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
//                MobclickAgent.onPause(activity);
            }
        });
    }

    public static void onEvent(String eventId) {
        if (!TextUtils.isEmpty(eventId)) {
            Timber.d("统计事件：" + eventId);
//            MobclickAgent.onEvent(sAppContext, eventId);
        }
    }

}
