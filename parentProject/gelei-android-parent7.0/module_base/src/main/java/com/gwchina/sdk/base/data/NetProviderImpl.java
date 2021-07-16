package com.gwchina.sdk.base.data;

import com.android.base.utils.android.ResourceUtils;
import com.android.sdk.net.core.ExceptionFactory;
import com.android.sdk.net.core.Result;
import com.android.sdk.net.exception.ApiErrorException;
import com.android.sdk.net.provider.ApiHandler;
import com.android.sdk.net.provider.ErrorDataAdapter;
import com.android.sdk.net.provider.ErrorMessage;
import com.android.sdk.net.provider.HttpConfig;
import com.android.sdk.net.provider.PostTransformer;
import com.app.base.BuildConfig;
import com.app.base.R;
import com.gwchina.sdk.base.data.api.ApiHelper;
import com.gwchina.sdk.base.data.api.ApiSpecification;
import com.gwchina.sdk.base.data.api.RetryWhenDeviceMissing;
import com.gwchina.sdk.base.data.exception.DeviceAlreadyBoundException;
import com.gwchina.sdk.base.data.exception.DeviceNotExistException;
import com.gwchina.sdk.base.data.exception.DeviceNotSupportException;
import com.gwchina.sdk.base.debug.DebugTools;

import org.reactivestreams.Publisher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import timber.log.Timber;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-08 16:16
 */
final class NetProviderImpl {

    ExceptionFactory mExceptionFactory = new ExceptionFactory() {

        final int CODE_DEVICE_ALREADY_BOUND = 2;
        final int CODE_DEVICE_NOT_EXIST = 20;
        final int CODE_DEVICE_NOT_SUPPORT = 22;

        @Override
        public Exception create(Result result) {
            if (result.getCode() == CODE_DEVICE_ALREADY_BOUND) {
                return new DeviceAlreadyBoundException(result.getCode(), result.getMessage());
            } else if (result.getCode() == CODE_DEVICE_NOT_EXIST) {
                return new DeviceNotExistException(result.getCode(), result.getMessage());
            } else if (result.getCode() == CODE_DEVICE_NOT_SUPPORT) {
                return new DeviceNotSupportException(result.getCode(), result.getMessage());
            }
            return null;
        }
    };

    ApiHandler mApiHandler = result -> {
        int code = result.getCode();
        //登录状态已过期，请重新登录、账号在其他设备登陆
        if (ApiHelper.isLoginExpired(code) || ApiHelper.isSSOLoginExpired(code)) {
            DataContext.getInstance().publishLoginExpired(code);
        }
    };

    HttpConfig mHttpConfig = new HttpConfig() {

        private static final int CONNECTION_TIME_OUT = 10;
        private static final int IO_TIME_OUT = 20;

        @Override
        public void configHttp(OkHttpClient.Builder builder) {
            //常规配置
            builder.connectTimeout(CONNECTION_TIME_OUT, TimeUnit.SECONDS)
                    .readTimeout(IO_TIME_OUT, TimeUnit.SECONDS)
                    .writeTimeout(IO_TIME_OUT, TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> {
                        Timber.d("hostnameVerifier called with: hostname 、session = [" + hostname + "、" + session.getProtocol() + "]");
                        return true;
                    });
            //调试配置
            configDebug(builder);
            //Api 规范
            ApiSpecification.configHttp(builder);
        }

        void configDebug(OkHttpClient.Builder builder) {
            if (BuildConfig.openDebug) {
                HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(
                        message -> Timber.tag("===OkHttp===").i(message)
                );
                httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(httpLoggingInterceptor);
                DebugTools.installStethoHttp(builder);
            }
        }

        @Override
        public boolean configRetrofit(Retrofit.Builder builder) {
            return false;
        }

        @Override
        public String baseUrl() {
            return DataContext.baseUrl();
        }
    };

    ErrorMessage mErrorMessage = new ErrorMessage() {
        @Override
        public CharSequence netErrorMessage(Throwable exception) {
            return ResourceUtils.getString(R.string.error_net_error);
        }

        @Override
        public CharSequence serverDataErrorMessage(Throwable exception) {
            return ResourceUtils.getString(R.string.error_service_data_error);
        }

        @Override
        public CharSequence serverErrorMessage(Throwable exception) {
            return ResourceUtils.getString(R.string.error_service_error);
        }

        @Override
        public CharSequence clientRequestErrorMessage(Throwable exception) {
            return ResourceUtils.getString(R.string.error_request_error);
        }

        @Override
        public CharSequence apiErrorMessage(ApiErrorException exception) {
            return ResourceUtils.getString(R.string.error_api_code_mask_tips, exception.getCode());
        }

        @Override
        public CharSequence unknowErrorMessage(Throwable exception) {
            return ResourceUtils.getString(R.string.error_unknow);
        }

    };

    ErrorDataAdapter mErrorDataAdapter = new ErrorDataAdapter() {
        @Override
        public Object createErrorDataStub(Type type, Annotation[] annotations, Retrofit retrofit, ResponseBody value) {
            return ApiHelper.newErrorDataStub();
        }

        @Override
        public boolean isErrorDataStub(Object object) {
            return ApiHelper.isDataError(object);
        }
    };

    PostTransformer mPostTransformer = new PostTransformer<Object>() {
        @Override
        public SingleSource<Object> apply(Single<Object> upstream) {
            return upstream.retryWhen(RetryWhenDeviceMissing.newRetryWhenDeviceMissingFlowable(3));
        }

        @Override
        public Publisher<Object> apply(Flowable<Object> upstream) {
            return upstream.retryWhen(RetryWhenDeviceMissing.newRetryWhenDeviceMissingFlowable(3));
        }

        @Override
        public ObservableSource<Object> apply(Observable<Object> upstream) {
            return upstream.retryWhen(RetryWhenDeviceMissing.newRetryWhenDeviceMissingObservable(3));
        }
    };

}