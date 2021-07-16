package com.gwchina.parent.migration.injection

import android.arch.lifecycle.ViewModel
import com.android.base.app.dagger.ActivityScope
import com.android.base.app.dagger.FragmentScope
import com.android.base.app.dagger.ViewModelKey
import com.android.base.app.dagger.ViewModelModule
import com.android.sdk.net.service.ServiceFactory
import com.gwchina.parent.binding.common.NewChildProcessor
import com.gwchina.parent.migration.MigrationActivity
import com.gwchina.parent.migration.data.MigrationApi
import com.gwchina.parent.migration.presentation.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

/**
 *@author Ztiany
 *      Email: ztiany3@gmail.com
 *      Date : 2019-08-28 14:31
 */

@Module
abstract class MigrationModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = [ViewModelModule::class, MigrationInternalModule::class, InnerProvideModule::class])
    internal abstract fun contributeMigrationActivityInjector(): MigrationActivity

}

@Module
class InnerProvideModule {

    @Provides
    @ActivityScope
    fun provideNewChildProcessor(processor: MigrationNewChildProcessor): NewChildProcessor {
        return processor
    }

}

@Module
abstract class MigrationInternalModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        fun provideMigrationApi(serviceFactory: ServiceFactory): MigrationApi {
            return serviceFactory.create(MigrationApi::class.java)
        }
    }

    @Binds
    @IntoMap
    @ViewModelKey(MigrationViewModel::class)
    abstract fun provideBindingViewModel(migrationViewModel: MigrationViewModel): ViewModel

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeMigrationGuideFragmentInjector(): MigrationGuideFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeAddingChildFragmentInjector(): AddingChildFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeMigrationConfirmingFragmentInjector(): MigrationConfirmingFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeBelongingDeviceFragmentInjector(): BelongingDeviceFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeChildDeviceStateRefreshFragmentInjector(): ChildDeviceStateRefreshFragment

    @FragmentScope
    @ContributesAndroidInjector
    abstract fun contributeMigrationChildInfoCollectFragmentInjector(): MigrationChildInfoCollectFragment

}