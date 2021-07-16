package com.android.sdk.net;

import android.support.annotation.NonNull;

import com.android.sdk.net.core.ExceptionFactory;
import com.android.sdk.net.provider.ApiHandler;
import com.android.sdk.net.provider.ErrorDataAdapter;
import com.android.sdk.net.provider.ErrorMessage;
import com.android.sdk.net.provider.HttpConfig;
import com.android.sdk.net.provider.NetworkChecker;
import com.android.sdk.net.provider.PostTransformer;
import com.android.sdk.net.service.ServiceFactory;
import com.android.sdk.net.service.ServiceHelper;

import okhttp3.OkHttpClient;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-08 11:06
 */
public class NetContext {

    private static volatile NetContext CONTEXT;

    public static NetContext get() {
        if (CONTEXT == null) {
            synchronized (NetContext.class) {
                if (CONTEXT == null) {
                    CONTEXT = new NetContext();
                }
            }
        }
        return CONTEXT;
    }

    private NetContext() {
        mServiceHelper = new ServiceHelper();
    }

    private NetProvider mNetProvider;
    private ServiceHelper mServiceHelper;

    public Builder newBuilder() {
        return new Builder();
    }

    private void init(@NonNull NetProvider netProvider) {
        mNetProvider = netProvider;
    }

    public boolean connected() {
        return mNetProvider.isConnected();
    }

    public NetProvider netProvider() {
        NetProvider retProvider = mNetProvider;

        if (retProvider == null) {
            throw new RuntimeException("NetContext has not be initialized");
        }
        return retProvider;
    }

    public OkHttpClient httpClient() {
        return mServiceHelper.getOkHttpClient(netProvider().httpConfig());
    }

    public ServiceFactory serviceFactory() {
        return mServiceHelper.getServiceFactory(netProvider().httpConfig());
    }

    public class Builder {

        private NetProviderImpl mNetProvider = new NetProviderImpl();

        public Builder aipHandler(@NonNull ApiHandler apiHandler) {
            mNetProvider.mApiHandler = apiHandler;
            return this;
        }

        public Builder httpConfig(@NonNull HttpConfig httpConfig) {
            mNetProvider.mHttpConfig = httpConfig;
            return this;
        }

        public Builder errorMessage(@NonNull ErrorMessage errorMessage) {
            mNetProvider.mErrorMessage = errorMessage;
            return this;
        }

        public Builder errorDataAdapter(@NonNull ErrorDataAdapter errorDataAdapter) {
            mNetProvider.mErrorDataAdapter = errorDataAdapter;
            return this;
        }

        public Builder networkChecker(@NonNull NetworkChecker networkChecker) {
            mNetProvider.mNetworkChecker = networkChecker;
            return this;
        }

        public Builder postTransformer(@NonNull PostTransformer postTransformer) {
            mNetProvider.mPostTransformer = postTransformer;
            return this;
        }

        public Builder exceptionFactory(@NonNull ExceptionFactory exceptionFactory) {
            mNetProvider.mExceptionFactory = exceptionFactory;
            return this;
        }

        public void setup() {
            mNetProvider.checkRequired();
            NetContext.get().init(mNetProvider);
        }

    }

}
