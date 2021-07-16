package com.gwchina.sdk.base.third.push;

import android.app.Application;

import com.android.base.rx.FlowableRetryDelay;
import com.android.base.rx.RxKit;
import com.android.sdk.push.PushCallBack;
import com.android.sdk.push.PushContext;
import com.app.base.BuildConfig;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.third.channel.ChannelUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public final class PushManager {

    private static volatile PushManager PUSH_MANAGER;
    private final MessageProcessor mMessageProcessor;
    private Disposable mDisposable;

    private PushManager() {
        mMessageProcessor = new MessageProcessor();
    }

    public static PushManager getInstance() {
        if (PUSH_MANAGER == null) {
            synchronized (PushManager.class) {
                if (PUSH_MANAGER == null) {
                    PUSH_MANAGER = new PushManager();
                }
            }
        }
        return PUSH_MANAGER;
    }

    public void init(Application application) {
        //初始化推送
        PushContext.init(application, BuildConfig.openDebug);
        //设置消息处理器
        PushContext.getPush().setMessageHandler(mMessageProcessor);
        PushContext.getPush().setChannel(ChannelUtils.getChannel());
        //注册推送
        registerPushLooped();
    }

    public void setPushProcessorEnable(boolean enable) {
        mMessageProcessor.setEnable(enable);
    }

    private void registerPushLooped() {
        mDisposable = Flowable.interval(5000, 60 * 1000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    if (BuildConfig.openDebug) {
                        Timber.d("开始注册推送，尝试第 %d 次", aLong);
                    }
                    doRegister();
                }, RxKit.logErrorHandler());
    }

    private void doRegister() {
        PushContext.getPush().registerPush(new PushCallBack() {
            @Override
            public void onRegisterPushSuccess(String registrationID) {
                mDisposable.dispose();
                Timber.d("推送注册成功，registrationID = " + registrationID + 1);
                AppContext.appDataSource().observableDeviceId()
                        .subscribe(
                                deviceId -> uploadPushIdLooped(deviceId, registrationID),
                                RxKit.logErrorHandler());
            }

            @Override
            public void onRegisterPushFail() {
                Timber.d("onRegisterPushFail() called");
            }
        });
    }

    private void uploadPushIdLooped(String deviceId, String registrationID) {
        Timber.d("开始上报推送 registrationID");
        AppContext.appDataSource()
                .uploadPushId(deviceId, registrationID)
                .retryWhen(new FlowableRetryDelay(Integer.MAX_VALUE, 60 * 1000, null))
                .subscribe(RxKit.logCompletedHandler());
    }

}