package com.gwchina.sdk.base.data.api;

import com.android.base.utils.android.DevicesUtils;
import com.android.sdk.net.exception.ApiErrorException;
import com.android.sdk.net.exception.ServerErrorException;
import com.blankj.utilcode.util.AppUtils;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.utils.GwDevices;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import timber.log.Timber;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-12-21 15:05
 */
public class RetryWhenDeviceMissing {

    public static RetryWhenDeviceMissingFlowable newRetryWhenDeviceMissingFlowable(int maxRetries) {
        return new RetryWhenDeviceMissingFlowable(maxRetries);
    }

    public static RetryWhenDeviceMissingObservable newRetryWhenDeviceMissingObservable(int maxRetries) {
        return new RetryWhenDeviceMissingObservable(maxRetries);
    }

    static class RetryWhenDeviceMissingFlowable implements Function<Flowable<Throwable>, Publisher<?>> {

        private final int maxRetries;
        private int mRetryCount = 0;

        RetryWhenDeviceMissingFlowable(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public Publisher<?> apply(Flowable<Throwable> throwableFlowable) {
            return throwableFlowable.flatMap(throwable -> {

                mRetryCount++;

                if (mRetryCount > maxRetries) {
                    return Flowable.error(wrapError(throwable));
                }


                if (throwable instanceof ApiErrorException && isDeviceMiss(throwable)) {

                    Timber.i("Device id missing, then try to load device id, in " + mRetryCount + " times");

                    return AppContext.appDataSource()
                            .uploadDevice(GwDevices.getGwDeviceSN(), DevicesUtils.getModel(), AppUtils.getAppVersionName())
                            .toFlowable()
                            .defaultIfEmpty(1);
                }

                return Flowable.error(wrapError(throwable));
            });
        }
    }

    private static boolean isDeviceMiss(Throwable throwable) {
        String message = throwable.getMessage();
        return "获取设备ID失败".equals(message) || "设备ID不能为空".equals(message);
    }

    static class RetryWhenDeviceMissingObservable implements Function<Observable<Throwable>, ObservableSource<?>> {

        private final int maxRetries;
        private int mRetryCount = 0;

        RetryWhenDeviceMissingObservable(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public ObservableSource<?> apply(Observable<Throwable> throwableObservable) {
            return throwableObservable.flatMap(throwable -> {

                mRetryCount++;

                if (mRetryCount > maxRetries) {
                    return Observable.error(wrapError(throwable));
                }


                if (throwable instanceof ApiErrorException && isDeviceMiss(throwable)) {

                    Timber.i("Device id missing 自动重试 " + mRetryCount + " 次");

                    return AppContext.appDataSource()
                            .uploadDevice(GwDevices.getGwDeviceSN(), DevicesUtils.getModel(), AppUtils.getAppVersionName())
                            .toObservable()
                            .defaultIfEmpty(1);
                }

                return Observable.error(wrapError(throwable));
            });
        }

    }

    private static Throwable wrapError(Throwable it) {
        if (it instanceof ApiErrorException && isDeviceMiss(it)) {
            return new ServerErrorException(ServerErrorException.UNKNOW_ERROR);
        }
        return it;
    }

}
