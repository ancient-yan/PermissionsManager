package com.gwchina.parent.main.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.main.data.MainApi
import com.gwchina.parent.main.presentation.location.ChildLocationActivity
import com.gwchina.parent.main.presentation.location.ChildLocationFragment
import com.gwchina.parent.main.presentation.location.ChildLocationViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module
abstract class ChildLocationModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, ChildLocationInternalModule::class])
    internal abstract fun contributeChildLocationActivityInjector(): ChildLocationActivity
}

@Module
abstract class ChildLocationInternalModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun provideHomeApi(serviceFactory: ServiceFactory): MainApi {
            return serviceFactory.create(MainApi::class.java)
        }

    }

    @Binds
    @IntoMap
    @ViewModelKey(ChildLocationViewModel::class)
    abstract fun provideChildLocationViewModel(childLocationViewModel: ChildLocationViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeChildLocationFragmentInjector(): ChildLocationFragment

}