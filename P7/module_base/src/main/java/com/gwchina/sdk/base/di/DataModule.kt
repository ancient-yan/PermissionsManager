package com.gwchina.sdk.base.di

import com.android.base.rx.SchedulerProvider
import com.android.sdk.net.NetContext
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.sdk.base.app.AppErrorHandler
import com.gwchina.sdk.base.app.ErrorHandler
import com.gwchina.sdk.base.data.app.AppDataSource
import com.gwchina.sdk.base.data.app.AppRepository
import com.gwchina.sdk.base.data.app.StorageManager
import com.gwchina.sdk.base.data.services.ServiceManager
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-01 11:06
 */
@Module
class DataModule {

    @Provides
    internal fun provideUserDataSource(appRepository: AppRepository): AppDataSource {
        return appRepository
    }

    @Singleton
    @Provides
    internal fun provideServiceFactory(): ServiceFactory {
        return NetContext.get().serviceFactory()
    }

    @Provides
    @Singleton
    internal fun provideErrorHandler(): ErrorHandler {
        return AppErrorHandler()
    }

    @Provides
    @Singleton
    internal fun provideOkHttpClient(): OkHttpClient {
        return NetContext.get().httpClient()
    }

    @Provides
    @Singleton
    internal fun provideStorageManager(appRepository: AppRepository): StorageManager {
        return appRepository.storageManager
    }

    @Provides
    @Singleton
    internal fun provideServiceManager(serviceFactory: ServiceFactory, schedulerProvider: SchedulerProvider): ServiceManager {
        return ServiceManager(serviceFactory, schedulerProvider)
    }

}
