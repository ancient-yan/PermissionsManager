package com.gwchina.parent.screenshot.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.screenshot.ScreenshotActivity
import com.gwchina.parent.screenshot.data.ScreenshotApi
import com.gwchina.parent.screenshot.presentation.DeleteScreenshotFragment
import com.gwchina.parent.screenshot.presentation.RemoteScreenshotFragment
import com.gwchina.parent.screenshot.presentation.ScreenshotPicPreFragment
import com.gwchina.parent.screenshot.presentation.ScreenshotViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


@Module
abstract class ScreenshotModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, ScreenshotInternalModule::class])
    internal abstract fun contributeScreenshotActivityInjector(): ScreenshotActivity

}

@Module
abstract class ScreenshotInternalModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideScreenshotApi(serviceFactory: ServiceFactory): ScreenshotApi {
            return serviceFactory.create(ScreenshotApi::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(ScreenshotViewModel::class)
    abstract fun provideRecommendViewModel(screenshotViewModel: ScreenshotViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeRemoteScreenshotFragment(): RemoteScreenshotFragment


    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDeleteScreenshotFragment(): DeleteScreenshotFragment


    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeScreenshotPicPreFragment(): ScreenshotPicPreFragment

}