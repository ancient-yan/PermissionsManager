package com.gwchina.parent.daily.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.daily.DailyActivity
import com.gwchina.parent.daily.DailyMessageListFragment
import com.gwchina.parent.daily.DailyPublishFragment
import com.gwchina.parent.daily.DailyStreamFragment
import com.gwchina.parent.daily.data.DailyApi
import com.gwchina.parent.daily.presentation.DailyViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 *@author hujl
 *      Email: hujlin@163.com
 *      Date : 2019-08-13 11:42
 */
@Module
abstract class DailyModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, DailyInternalModule::class])
    internal abstract fun contributeDailyActivityInjector(): DailyActivity
}

@Module
abstract class DailyInternalModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideHomeApi(serviceFactory: ServiceFactory): DailyApi {
            return serviceFactory.create(DailyApi::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(DailyViewModel::class)
    abstract fun provideDailyViewModel(dailyViewModule: DailyViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDailyStreamFragmentInjector(): DailyStreamFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDailyPublishFragmentInjector(): DailyPublishFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeDailyMessageListInjector(): DailyMessageListFragment

}
